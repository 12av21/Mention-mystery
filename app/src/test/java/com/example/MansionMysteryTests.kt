package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SaveRepository
import com.example.model.ClueId
import com.example.model.CollectedClueRecord
import com.example.model.Difficulty
import com.example.model.GuardFsmState
import com.example.model.GuardRuntimeState
import com.example.model.RoomId
import com.example.model.SaveData
import com.example.model.Vec2
import com.example.mystery.MysteryCatalog
import com.example.mystery.PuzzleValidator
import com.example.world.MansionMapBuilder
import com.example.world.StealthAndCollision
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.File

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MansionMysteryTests {

    @Test
    fun `all five room puzzles validate correctly and reject wrong input`() {
        assertEquals(5, MysteryCatalog.puzzles.size)
        for (puzzle in MysteryCatalog.puzzles) {
            assertTrue(
                "Expected ${puzzle.id} to validate",
                PuzzleValidator.validate(puzzle, puzzle.expectedSequence),
            )
            assertFalse(
                "Expected ${puzzle.id} to reject wrong answer",
                PuzzleValidator.validate(puzzle, listOf("Wrong Option")),
            )
        }
    }

    @Test
    fun `secret room final deduction requires all 5 clues and exact solution`() {
        assertFalse(
            PuzzleValidator.validateSecretFinale(
                suspect = "Dr. Julian Ashford",
                method = "Moonbloom-laced honey tea",
                motive = "Hide ledger embezzlement & steal the Star-Compass",
                collectedCluesCount = 4,
            ),
        )
        assertTrue(
            PuzzleValidator.validateSecretFinale(
                suspect = "Dr. Julian Ashford",
                method = "Moonbloom-laced honey tea",
                motive = "Hide ledger embezzlement & steal the Star-Compass",
                collectedCluesCount = 5,
            ),
        )
    }

    @Test
    fun `mansion world contains all 9 rooms and reachable doors`() {
        val world = MansionMapBuilder.build()
        val roomIds = world.rooms.map { it.id }.toSet()
        assertEquals(RoomId.entries.toSet(), roomIds)
        assertEquals(6, world.doors.size)

        // Player spawn is walkable
        val spawn = world.checkpoints.first().spawnPos
        assertTrue(StealthAndCollision.canOccupy(spawn, StealthAndCollision.PLAYER_RADIUS, world, emptySet(), world.props))

        // Locked library door blocks entry until unlocked
        val libDoor = world.doors.first { it.id == "door_library" }
        assertFalse(StealthAndCollision.isTileWalkable(libDoor.tileX, libDoor.tileY, world, emptySet()))
        assertTrue(StealthAndCollision.isTileWalkable(libDoor.tileX, libDoor.tileY, world, setOf("door_library")))
    }

    @Test
    fun `guard flashlight respects wall occlusion and triggers chase`() {
        val world = MansionMapBuilder.build()
        val guard = GuardRuntimeState(
            id = "g1",
            name = "Barnaby",
            position = Vec2(15.0f, 12.5f),
            facingRad = 0f,
            fsmState = GuardFsmState.PATROL,
            waypointIndex = 1,
            stateTimer = 0f,
            suspicion = 0f,
            investigateTarget = null,
        )

        // Direct line of sight down main corridor
        assertTrue(
            StealthAndCollision.isPlayerInFlashlight(
                guard = guard,
                playerPos = Vec2(18.5f, 12.5f),
                difficulty = Difficulty.NORMAL,
                world = world,
                unlockedDoorIds = emptySet(),
            ),
        )

        // Wall at y=14 separates Foyer (y=16) from Main Corridor (y=12.5)
        assertFalse(
            StealthAndCollision.isPlayerInFlashlight(
                guard = guard,
                playerPos = Vec2(16.0f, 16.5f),
                difficulty = Difficulty.HARD,
                world = world,
                unlockedDoorIds = emptySet(),
            ),
        )

        // Guard FSM transitions to SUSPICIOUS / CHASE when player stays in beam
        var current = guard
        val waypoints = listOf(Vec2(15.0f, 12.5f), Vec2(22.0f, 12.5f))
        repeat(15) {
            current = StealthAndCollision.updateGuardFsm(
                guard = current,
                waypoints = waypoints,
                playerPos = Vec2(18.0f, 12.5f),
                dt = 0.08f,
                difficulty = Difficulty.NORMAL,
                world = world,
                unlockedDoorIds = emptySet(),
            ).updatedGuard
        }
        assertTrue(current.suspicion > 0.5f)
        assertTrue(current.fsmState == GuardFsmState.CHASE || current.fsmState == GuardFsmState.SUSPICIOUS)
    }

    @Test
    fun `save repository writes atomically and recovers from corrupted primary via backup`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val testDir = File(context.cacheDir, "save_test_${System.nanoTime()}")
        testDir.mkdirs()
        try {
            val repo = SaveRepository(testDir)
            assertNull(repo.loadSave())

            val save1 = SaveData(
                playerX = 19.5f,
                playerY = 17.5f,
                checkpointId = "cp_foyer",
                unlockedDoorIds = listOf("door_library"),
                solvedPuzzleIds = listOf("puzzle_library"),
                collectedClues = listOf(CollectedClueRecord(ClueId.CLUE_CIPHER_FOLIO.name, 1000L)),
                discoveredLinkIds = emptyList(),
                playtimeSeconds = 42,
                detectionCount = 1,
                difficultyName = Difficulty.NORMAL.name,
            )
            assertTrue(repo.saveGame(save1))

            // Second save rotates save1 into .bak
            val save2 = save1.copy(playtimeSeconds = 84, unlockedDoorIds = listOf("door_library", "door_study"))
            assertTrue(repo.saveGame(save2))

            // Corrupt primary file
            File(testDir, "mansion_save_v1.json").writeText("{ corrupted_json !!!")

            val recovered = repo.loadSave()
            assertNotNull("Expected recovery from .bak file", recovered)
            assertEquals(42, recovered?.playtimeSeconds)
            assertEquals("cp_foyer", recovered?.checkpointId)
        } finally {
            testDir.deleteRecursively()
        }
    }
}

package com.example.model

import androidx.annotation.DrawableRes
import com.squareup.moshi.JsonClass

enum class Difficulty(
    val displayName: String,
    val guardSpeed: Float,
    val visionRange: Float,
    val visionHalfAngleDeg: Float,
    val suspicionRate: Float,
) {
    EASY("Casual Sleuth", 2.05f, 5.2f, 24f, 0.75f),
    NORMAL("Inspector", 2.65f, 6.4f, 30f, 1.25f),
    HARD("Master Thief", 3.25f, 7.6f, 36f, 1.9f),
}

enum class RoomId(val displayName: String, val subtitle: String) {
    MAIN_ENTRANCE("Main Entrance", "South Porch & Threshold"),
    FOYER("Grand Foyer", "Twin Staircase Hall"),
    CORRIDORS("Mansion Corridors", "Shadowed Patrol Passage"),
    LIBRARY("The Library", "Dusty Folio Archive"),
    STUDY("Lord's Study", "Private Ledger Chamber"),
    CONSERVATORY("Conservatory", "Moonlit Glasshouse"),
    KITCHEN("The Kitchen", "Servants' Hearth & Pantry"),
    GALLERY("Portrait Gallery", "Ancestral Oil Wing"),
    SECRET_ROOM("Grand Secret Room", "The Ravenshollow Vault"),
}

enum class TileType(val walkable: Boolean, val blocksLight: Boolean) {
    VOID(false, true),
    WALL(false, true),
    FLOOR_STONE(true, false),
    FLOOR_WOOD(true, false),
    FLOOR_CARPET(true, false),
    FLOOR_GARDEN(true, false),
    FLOOR_MARBLE(true, false),
    DOOR_LOCKED(false, true),
    DOOR_UNLOCKED(true, false),
    SECRET_DOOR(false, true),
}

enum class PropType {
    BOOKSHELF,
    DESK,
    PIANO,
    TABLE,
    PLANT,
    PAINTING,
    STOVE,
    CANDELABRA,
    STATUE,
    SAFE_CHECKPOINT,
    CLUE_SPOT,
    EVIDENCE_BOARD_WALL,
    HEIRLOOM_PEDESTAL,
}

data class Vec2(val x: Float, val y: Float) {
    fun distanceTo(other: Vec2): Float {
        val dx = x - other.x
        val dy = y - other.y
        return kotlin.math.sqrt(dx * dx + dy * dy)
    }
}

data class RoomZone(
    val id: RoomId,
    val minX: Int,
    val minY: Int,
    val maxX: Int,
    val maxY: Int,
) {
    fun contains(x: Float, y: Float): Boolean =
        x >= minX && x <= maxX + 1f && y >= minY && y <= maxY + 1f
}

data class DoorObject(
    val id: String,
    val tileX: Int,
    val tileY: Int,
    val targetRoom: RoomId,
    val puzzleId: String?,
    val isSecretDoor: Boolean = false,
)

data class PropObject(
    val id: String,
    val tileX: Float,
    val tileY: Float,
    val width: Float,
    val height: Float,
    val type: PropType,
    val roomId: RoomId,
    val title: String,
    val inspectText: String,
    val blocksMovement: Boolean = true,
    val clueRewardId: ClueId? = null,
    @DrawableRes val portraitRes: Int? = null,
)

data class Checkpoint(
    val id: String,
    val roomId: RoomId,
    val spawnPos: Vec2,
    val label: String,
)

enum class ClueId {
    CLUE_CIPHER_FOLIO,
    CLUE_FORGED_LEDGER,
    CLUE_MOONBLOOM_VIAL,
    CLUE_BITTER_TEACUP,
    CLUE_CREST_MEDALLION,
}

enum class ClueCategory(val label: String) {
    DOCUMENT("Document"),
    FORENSIC("Forensic"),
    RELIC("Relic"),
}

data class ClueDefinition(
    val id: ClueId,
    val title: String,
    val roomId: RoomId,
    val category: ClueCategory,
    val shortSummary: String,
    val fullDescription: String,
    val boardSnippet: String,
    @DrawableRes val imageRes: Int,
)

@JsonClass(generateAdapter = true)
data class CollectedClueRecord(
    val clueId: String,
    val discoveredAtEpochMs: Long,
)

data class EvidenceLinkDefinition(
    val id: String,
    val first: ClueId,
    val second: ClueId,
    val insightTitle: String,
    val insightBody: String,
)

enum class PuzzleType {
    CIPHER_BOOKS,
    LOGIC_DEDUCTION,
    BOTANICAL_PATTERN,
    RECIPE_SEQUENCE,
    GALLERY_SYMBOLS,
}

data class RoomPuzzleDefinition(
    val id: String,
    val roomId: RoomId,
    val title: String,
    val subtitle: String,
    val type: PuzzleType,
    val prompt: String,
    val hints: List<String>,
    val rewardClueId: ClueId,
    val expectedSequence: List<String>,
    val options: List<String>,
    val extraNotes: List<String> = emptyList(),
)

enum class GuardFsmState(val label: String) {
    IDLE("Standing Watch"),
    PATROL("Patrolling"),
    INVESTIGATE("Investigating"),
    SUSPICIOUS("Alert!"),
    CHASE("Pursuing!"),
    RETURN_TO_PATROL("Returning"),
}

data class GuardConfig(
    val id: String,
    val name: String,
    val waypoints: List<Vec2>,
    val initialFacingRad: Float,
)

data class GuardRuntimeState(
    val id: String,
    val name: String,
    val position: Vec2,
    val facingRad: Float,
    val fsmState: GuardFsmState,
    val waypointIndex: Int,
    val stateTimer: Float,
    val suspicion: Float,
    val investigateTarget: Vec2?,
    val flashlightPolygon: List<Vec2> = emptyList(),
    val walkPhase: Float = 0f,
)

data class PlayerState(
    val position: Vec2,
    val facingRad: Float = -1.5708f,
    val isMoving: Boolean = false,
    val walkPhase: Float = 0f,
    val currentRoom: RoomId = RoomId.MAIN_ENTRANCE,
    val activeCheckpointId: String = "cp_entrance",
)

sealed class InteractTarget {
    abstract val label: String
    abstract val worldPos: Vec2

    data class Door(
        val door: DoorObject,
        val isUnlocked: Boolean,
        override val label: String,
        override val worldPos: Vec2,
    ) : InteractTarget()

    data class Prop(
        val prop: PropObject,
        override val label: String,
        override val worldPos: Vec2,
    ) : InteractTarget()
}

enum class GameScreen {
    SPLASH,
    MAIN_MENU,
    PLAYING,
    VICTORY,
}

enum class GameOverlay {
    NONE,
    PUZZLE,
    INVENTORY,
    MAP,
    PAUSE,
    SETTINGS,
    CAUGHT,
    INSPECT_OBJECT,
    SECRET_FINALE,
}

@JsonClass(generateAdapter = true)
data class GameSettings(
    val masterVolume: Float = 0.85f,
    val musicVolume: Float = 0.65f,
    val sfxVolume: Float = 0.85f,
    val muted: Boolean = false,
    val joystickSensitivity: Float = 1.0f,
    val difficultyName: String = Difficulty.NORMAL.name,
) {
    val difficulty: Difficulty
        get() = runCatching { Difficulty.valueOf(difficultyName) }.getOrDefault(Difficulty.NORMAL)
}

@JsonClass(generateAdapter = true)
data class SaveData(
    val version: Int = CURRENT_VERSION,
    val playerX: Float,
    val playerY: Float,
    val checkpointId: String,
    val unlockedDoorIds: List<String>,
    val solvedPuzzleIds: List<String>,
    val collectedClues: List<CollectedClueRecord>,
    val discoveredLinkIds: List<String>,
    val secretRoomSolved: Boolean = false,
    val playtimeSeconds: Int = 0,
    val detectionCount: Int = 0,
    val difficultyName: String = Difficulty.NORMAL.name,
    val updatedAtEpochMs: Long = 0L,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

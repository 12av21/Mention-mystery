package com.example.world

import com.example.R
import com.example.model.Checkpoint
import com.example.model.ClueId
import com.example.model.DoorObject
import com.example.model.GuardConfig
import com.example.model.PropObject
import com.example.model.PropType
import com.example.model.RoomId
import com.example.model.RoomZone
import com.example.model.TileType
import com.example.model.Vec2
import kotlin.math.PI

data class MansionWorldDefinition(
    val width: Int,
    val height: Int,
    val tiles: Array<Array<TileType>>,
    val rooms: List<RoomZone>,
    val doors: List<DoorObject>,
    val props: List<PropObject>,
    val checkpoints: List<Checkpoint>,
    val guardConfigs: List<GuardConfig>,
    val sconces: List<Vec2>,
) {
    fun tileAt(x: Int, y: Int): TileType {
        if (x !in 0 until width || y !in 0 until height) return TileType.WALL
        return tiles[y][x]
    }

    fun roomAt(x: Float, y: Float): RoomId {
        return rooms.firstOrNull { it.contains(x, y) }?.id ?: RoomId.CORRIDORS
    }
}

object MansionMapBuilder {

    const val MAP_WIDTH = 40
    const val MAP_HEIGHT = 26

    fun build(): MansionWorldDefinition {
        val tiles = Array(MAP_HEIGHT) { Array(MAP_WIDTH) { TileType.WALL } }

        fun fillRect(x1: Int, y1: Int, x2: Int, y2: Int, type: TileType) {
            for (y in y1..y2) {
                for (x in x1..x2) {
                    if (x in 0 until MAP_WIDTH && y in 0 until MAP_HEIGHT) {
                        tiles[y][x] = type
                    }
                }
            }
        }

        // 1. Main Entrance (South Porch)
        fillRect(16, 21, 23, 24, TileType.FLOOR_STONE)
        fillRect(18, 21, 21, 24, TileType.FLOOR_CARPET)

        // 2. Grand Foyer (Central Hub)
        fillRect(14, 15, 25, 20, TileType.FLOOR_WOOD)
        fillRect(17, 15, 22, 19, TileType.FLOOR_CARPET)
        // Open archways connecting Entrance -> Foyer and Foyer -> Corridors
        fillRect(19, 20, 20, 21, TileType.FLOOR_CARPET) // South arch
        fillRect(19, 14, 20, 14, TileType.FLOOR_CARPET) // North arch
        fillRect(13, 17, 13, 18, TileType.FLOOR_WOOD)   // West arch
        fillRect(26, 17, 26, 18, TileType.FLOOR_WOOD)   // East arch

        // 3. Corridors (Main E-W corridor + West & East patrol halls)
        fillRect(2, 11, 37, 13, TileType.FLOOR_WOOD)
        fillRect(11, 14, 12, 22, TileType.FLOOR_WOOD)
        fillRect(27, 14, 28, 22, TileType.FLOOR_WOOD)

        // 4. Library (South-West Wing)
        fillRect(1, 15, 9, 24, TileType.FLOOR_WOOD)
        fillRect(3, 17, 7, 22, TileType.FLOOR_CARPET)

        // 5. Study (North-West Wing)
        fillRect(1, 1, 10, 9, TileType.FLOOR_WOOD)
        fillRect(3, 3, 8, 7, TileType.FLOOR_CARPET)

        // 6. Conservatory (North-Center-West)
        fillRect(12, 1, 19, 9, TileType.FLOOR_GARDEN)

        // 7. Grand Secret Room (North-Center-East)
        fillRect(21, 1, 27, 9, TileType.FLOOR_MARBLE)

        // 8. Gallery (North-East)
        fillRect(29, 1, 38, 9, TileType.FLOOR_MARBLE)
        fillRect(31, 3, 36, 7, TileType.FLOOR_CARPET)

        // 9. Kitchen (South-East)
        fillRect(30, 15, 38, 24, TileType.FLOOR_STONE)

        // Doors connecting rooms to corridors
        val doors = listOf(
            DoorObject(
                id = "door_library",
                tileX = 10,
                tileY = 18,
                targetRoom = RoomId.LIBRARY,
                puzzleId = "puzzle_library",
            ),
            DoorObject(
                id = "door_study",
                tileX = 6,
                tileY = 10,
                targetRoom = RoomId.STUDY,
                puzzleId = "puzzle_study",
            ),
            DoorObject(
                id = "door_conservatory",
                tileX = 15,
                tileY = 10,
                targetRoom = RoomId.CONSERVATORY,
                puzzleId = "puzzle_conservatory",
            ),
            DoorObject(
                id = "door_kitchen",
                tileX = 29,
                tileY = 18,
                targetRoom = RoomId.KITCHEN,
                puzzleId = "puzzle_kitchen",
            ),
            DoorObject(
                id = "door_gallery",
                tileX = 33,
                tileY = 10,
                targetRoom = RoomId.GALLERY,
                puzzleId = "puzzle_gallery",
            ),
            DoorObject(
                id = "door_secret",
                tileX = 24,
                tileY = 10,
                targetRoom = RoomId.SECRET_ROOM,
                puzzleId = null,
                isSecretDoor = true,
            ),
        )

        doors.forEach { door ->
            tiles[door.tileY][door.tileX] =
                if (door.isSecretDoor) TileType.SECRET_DOOR else TileType.DOOR_LOCKED
        }

        val rooms = listOf(
            RoomZone(RoomId.MAIN_ENTRANCE, 16, 21, 23, 24),
            RoomZone(RoomId.FOYER, 14, 15, 25, 20),
            RoomZone(RoomId.LIBRARY, 1, 15, 9, 24),
            RoomZone(RoomId.STUDY, 1, 1, 10, 9),
            RoomZone(RoomId.CONSERVATORY, 12, 1, 19, 9),
            RoomZone(RoomId.SECRET_ROOM, 21, 1, 27, 9),
            RoomZone(RoomId.GALLERY, 29, 1, 38, 9),
            RoomZone(RoomId.KITCHEN, 30, 15, 38, 24),
            RoomZone(RoomId.CORRIDORS, 2, 11, 37, 13),
        )

        val checkpoints = listOf(
            Checkpoint(
                id = "cp_entrance",
                roomId = RoomId.MAIN_ENTRANCE,
                spawnPos = Vec2(19.5f, 22.8f),
                label = "Entrance Threshold",
            ),
            Checkpoint(
                id = "cp_foyer",
                roomId = RoomId.FOYER,
                spawnPos = Vec2(19.5f, 17.5f),
                label = "Grand Foyer Brazier",
            ),
        )

        val props = listOf(
            // Main Entrance
            PropObject(
                id = "prop_entrance_crest",
                tileX = 16.5f,
                tileY = 22.0f,
                width = 1.0f,
                height = 1.0f,
                type = PropType.STATUE,
                roomId = RoomId.MAIN_ENTRANCE,
                title = "Stone Raven Gargoyle",
                inspectText = "Fresh muddy bootprints lead north into the Grand Foyer. Someone arrived in haste tonight.",
            ),
            PropObject(
                id = "prop_entrance_lantern",
                tileX = 22.5f,
                tileY = 22.0f,
                width = 1.0f,
                height = 1.0f,
                type = PropType.CANDELABRA,
                roomId = RoomId.MAIN_ENTRANCE,
                title = "Porch Lantern",
                inspectText = "The iron gate clangs shut behind you. Two patrolling guards sweep the upper corridors.",
            ),
            // Grand Foyer
            PropObject(
                id = "prop_foyer_piano",
                tileX = 15.0f,
                tileY = 16.0f,
                width = 1.8f,
                height = 1.3f,
                type = PropType.PIANO,
                roomId = RoomId.FOYER,
                title = "Ebony Grand Piano",
                inspectText = "Dust coats every key except middle C—pressed recently to muffle a secret latch.",
            ),
            PropObject(
                id = "prop_foyer_checkpoint",
                tileX = 23.0f,
                tileY = 18.0f,
                width = 1.1f,
                height = 1.1f,
                type = PropType.SAFE_CHECKPOINT,
                roomId = RoomId.FOYER,
                title = "Safe Sanctuary Alcove",
                inspectText = "A quiet velvet alcove outside guard patrol routes. Stepping here sets your safe checkpoint.",
                blocksMovement = false,
            ),
            // Library
            PropObject(
                id = "prop_lib_shelf_1",
                tileX = 2.0f,
                tileY = 15.5f,
                width = 3.2f,
                height = 0.9f,
                type = PropType.BOOKSHELF,
                roomId = RoomId.LIBRARY,
                title = "Occult & Cipher Shelves",
                inspectText = "Rows of leather-bound histories. One slot is noticeably warmer than the rest.",
            ),
            PropObject(
                id = "prop_lib_table",
                tileX = 5.0f,
                tileY = 19.5f,
                width = 2.0f,
                height = 1.2f,
                type = PropType.TABLE,
                roomId = RoomId.LIBRARY,
                title = "Oak Reading Table",
                inspectText = "A brass magnifying loupe rests beside an empty book cover.",
            ),
            PropObject(
                id = "clue_library",
                tileX = 3.5f,
                tileY = 17.5f,
                width = 0.9f,
                height = 0.9f,
                type = PropType.CLUE_SPOT,
                roomId = RoomId.LIBRARY,
                title = "Torn Cipher Folio",
                inspectText = "You recovered the Torn Cipher Folio tucked inside Astral Mechanics!",
                blocksMovement = false,
                clueRewardId = ClueId.CLUE_CIPHER_FOLIO,
                portraitRes = R.drawable.img_detective_portrait,
            ),
            // Study
            PropObject(
                id = "prop_study_desk",
                tileX = 3.5f,
                tileY = 3.5f,
                width = 2.2f,
                height = 1.2f,
                type = PropType.DESK,
                roomId = RoomId.STUDY,
                title = "Mahogany Writing Desk",
                inspectText = "Crimson sealing wax drips across the blotter; a quill still smells of fresh ink.",
            ),
            PropObject(
                id = "prop_study_clock",
                tileX = 8.2f,
                tileY = 2.0f,
                width = 1.0f,
                height = 1.0f,
                type = PropType.CANDELABRA,
                roomId = RoomId.STUDY,
                title = "Stopped Grandfather Clock",
                inspectText = "Its pendulum was halted deliberately at 11:30 PM.",
            ),
            PropObject(
                id = "clue_study",
                tileX = 6.0f,
                tileY = 4.2f,
                width = 0.9f,
                height = 0.9f,
                type = PropType.CLUE_SPOT,
                roomId = RoomId.STUDY,
                title = "Forged Estate Ledger",
                inspectText = "You uncovered Dr. Ashford's Forged Estate Ledger hidden in the desk drawer!",
                blocksMovement = false,
                clueRewardId = ClueId.CLUE_FORGED_LEDGER,
                portraitRes = R.drawable.img_lord_portrait,
            ),
            // Conservatory
            PropObject(
                id = "prop_cons_planter_1",
                tileX = 13.0f,
                tileY = 3.0f,
                width = 1.4f,
                height = 1.4f,
                type = PropType.PLANT,
                roomId = RoomId.CONSERVATORY,
                title = "Lunar Orchid Bed",
                inspectText = "Several purple blossoms were freshly clipped with silver shears.",
            ),
            PropObject(
                id = "prop_cons_planter_2",
                tileX = 17.5f,
                tileY = 6.5f,
                width = 1.4f,
                height = 1.4f,
                type = PropType.PLANT,
                roomId = RoomId.CONSERVATORY,
                title = "Glasshouse Fern Fountain",
                inspectText = "Condensation beads along the stone basin under pale moonlight.",
            ),
            PropObject(
                id = "clue_conservatory",
                tileX = 16.0f,
                tileY = 3.5f,
                width = 0.9f,
                height = 0.9f,
                type = PropType.CLUE_SPOT,
                roomId = RoomId.CONSERVATORY,
                title = "Moonbloom Vial",
                inspectText = "You collected the glowing Moonbloom Vial beside the orchid basin!",
                blocksMovement = false,
                clueRewardId = ClueId.CLUE_MOONBLOOM_VIAL,
                portraitRes = R.drawable.img_detective_portrait,
            ),
            // Kitchen
            PropObject(
                id = "prop_kitchen_stove",
                tileX = 36.2f,
                tileY = 16.2f,
                width = 2.0f,
                height = 1.2f,
                type = PropType.STOVE,
                roomId = RoomId.KITCHEN,
                title = "Cast-Iron Hearth",
                inspectText = "Embers still glow under an abandoned copper kettle.",
            ),
            PropObject(
                id = "prop_kitchen_table",
                tileX = 33.0f,
                tileY = 19.5f,
                width = 2.2f,
                height = 1.2f,
                type = PropType.TABLE,
                roomId = RoomId.KITCHEN,
                title = "Servants' Prep Counter",
                inspectText = "An uncapped jar of wildflower honey sits next to a damp cloth.",
            ),
            PropObject(
                id = "clue_kitchen",
                tileX = 35.5f,
                tileY = 21.5f,
                width = 0.9f,
                height = 0.9f,
                type = PropType.CLUE_SPOT,
                roomId = RoomId.KITCHEN,
                title = "Bitter Almond Teacup",
                inspectText = "You bagged the Bitter Almond Teacup containing Moonbloom residue!",
                blocksMovement = false,
                clueRewardId = ClueId.CLUE_BITTER_TEACUP,
                portraitRes = R.drawable.img_royal_heirloom,
            ),
            // Gallery
            PropObject(
                id = "prop_gallery_painting",
                tileX = 31.5f,
                tileY = 2.0f,
                width = 1.8f,
                height = 0.8f,
                type = PropType.PAINTING,
                roomId = RoomId.GALLERY,
                title = "Portrait of Lord Ravenshollow",
                inspectText = "Painted in 1884. The gilt frame swings outward slightly on oiled hinges.",
                portraitRes = R.drawable.img_lord_portrait,
            ),
            PropObject(
                id = "prop_gallery_statue",
                tileX = 33.5f,
                tileY = 5.5f,
                width = 1.2f,
                height = 1.2f,
                type = PropType.STATUE,
                roomId = RoomId.GALLERY,
                title = "Marble Astronomer Bust",
                inspectText = "Its eyes point squarely toward the hidden northern wall.",
            ),
            PropObject(
                id = "clue_gallery",
                tileX = 36.2f,
                tileY = 3.2f,
                width = 0.9f,
                height = 0.9f,
                type = PropType.CLUE_SPOT,
                roomId = RoomId.GALLERY,
                title = "Ravenshollow Crest Key",
                inspectText = "You pried the Ravenshollow Crest Key from behind the gilt portrait!",
                blocksMovement = false,
                clueRewardId = ClueId.CLUE_CREST_MEDALLION,
                portraitRes = R.drawable.img_lord_portrait,
            ),
            // Grand Secret Room
            PropObject(
                id = "prop_secret_board",
                tileX = 22.5f,
                tileY = 2.5f,
                width = 1.6f,
                height = 0.9f,
                type = PropType.EVIDENCE_BOARD_WALL,
                roomId = RoomId.SECRET_ROOM,
                title = "Master Conspiracy Board",
                inspectText = "Maps, shipping ledgers, and midnight timelines pinned by the culprit.",
            ),
            PropObject(
                id = "prop_secret_heirloom",
                tileX = 24.5f,
                tileY = 4.8f,
                width = 1.3f,
                height = 1.3f,
                type = PropType.HEIRLOOM_PEDESTAL,
                roomId = RoomId.SECRET_ROOM,
                title = "Star-Compass Vault Pedestal",
                inspectText = "The missing Ravenshollow Star-Compass rests behind a crystalline cipher lock!",
                portraitRes = R.drawable.img_royal_heirloom,
            ),
        )

        val guardConfigs = listOf(
            GuardConfig(
                id = "guard_barnaby",
                name = "Warden Barnaby",
                waypoints = listOf(
                    Vec2(4.5f, 12.5f),
                    Vec2(20.0f, 12.5f),
                    Vec2(12.0f, 12.5f),
                    Vec2(12.0f, 20.5f),
                    Vec2(12.0f, 12.5f),
                ),
                initialFacingRad = 0f,
            ),
            GuardConfig(
                id = "guard_vance",
                name = "Constable Vance",
                waypoints = listOf(
                    Vec2(35.5f, 12.5f),
                    Vec2(20.5f, 12.5f),
                    Vec2(28.0f, 12.5f),
                    Vec2(28.0f, 20.5f),
                    Vec2(28.0f, 12.5f),
                ),
                initialFacingRad = PI.toFloat(),
            ),
        )

        val sconces = listOf(
            Vec2(18.0f, 21.5f),
            Vec2(21.5f, 21.5f),
            Vec2(15.5f, 15.5f),
            Vec2(24.0f, 15.5f),
            Vec2(6.0f, 16.0f),
            Vec2(5.0f, 2.5f),
            Vec2(15.5f, 2.5f),
            Vec2(24.5f, 2.5f),
            Vec2(34.0f, 2.5f),
            Vec2(35.0f, 16.5f),
            Vec2(8.0f, 11.5f),
            Vec2(31.0f, 11.5f),
        )

        return MansionWorldDefinition(
            width = MAP_WIDTH,
            height = MAP_HEIGHT,
            tiles = tiles,
            rooms = rooms,
            doors = doors,
            props = props,
            checkpoints = checkpoints,
            guardConfigs = guardConfigs,
            sconces = sconces,
        )
    }
}

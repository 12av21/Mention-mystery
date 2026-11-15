package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundEffect
import com.example.audio.SoundEngine
import com.example.data.SaveRepository
import com.example.model.ClueId
import com.example.model.CollectedClueRecord
import com.example.model.Difficulty
import com.example.model.GameOverlay
import com.example.model.GameScreen
import com.example.model.GameSettings
import com.example.model.GuardFsmState
import com.example.model.GuardRuntimeState
import com.example.model.InteractTarget
import com.example.model.PlayerState
import com.example.model.PropObject
import com.example.model.RoomId
import com.example.model.RoomPuzzleDefinition
import com.example.model.SaveData
import com.example.model.Vec2
import com.example.mystery.MysteryCatalog
import com.example.mystery.PuzzleValidator
import com.example.world.MansionMapBuilder
import com.example.world.MansionWorldDefinition
import com.example.world.StealthAndCollision
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt

data class MansionUiState(
    val screen: GameScreen = GameScreen.SPLASH,
    val overlay: GameOverlay = GameOverlay.NONE,
    val returnOverlayAfterSettings: GameOverlay = GameOverlay.NONE,
    val hasSavedGame: Boolean = false,
    val settings: GameSettings = GameSettings(),
    val world: MansionWorldDefinition = MansionMapBuilder.build(),
    val player: PlayerState = PlayerState(position = Vec2(19.5f, 22.8f)),
    val guards: List<GuardRuntimeState> = emptyList(),
    val unlockedDoorIds: Set<String> = emptySet(),
    val solvedPuzzleIds: Set<String> = emptySet(),
    val collectedClues: List<CollectedClueRecord> = emptyList(),
    val discoveredLinkIds: Set<String> = emptySet(),
    val secretRoomSolved: Boolean = false,
    val playtimeSeconds: Int = 0,
    val detectionCount: Int = 0,
    val activePuzzle: RoomPuzzleDefinition? = null,
    val puzzleSelection: List<String> = emptyList(),
    val puzzleHintsRevealed: Int = 0,
    val puzzleFeedback: String? = null,
    val puzzleFeedbackIsError: Boolean = false,
    val inspectedProp: PropObject? = null,
    val nearbyTarget: InteractTarget? = null,
    val highlightHiddenPulse: Float = 0f,
    val bannerMessage: String? = null,
    val caughtByGuardName: String = "",
    val selectedClueId: ClueId? = null,
    val boardSelectedClue: ClueId? = null,
    val boardFeedback: String? = null,
    val finaleSuspect: String? = null,
    val finaleMethod: String? = null,
    val finaleMotive: String? = null,
    val finaleFeedback: String? = null,
) {
    val maxGuardSuspicion: Float
        get() = guards.maxOfOrNull { it.suspicion } ?: 0f

    val collectedClueSet: Set<ClueId>
        get() = collectedClues.mapNotNull { runCatching { ClueId.valueOf(it.clueId) }.getOrNull() }.toSet()

    val isSecretRoomReady: Boolean
        get() = solvedPuzzleIds.size >= MysteryCatalog.puzzles.size &&
            collectedClueSet.size >= ClueId.entries.size
}

class MansionGameViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val saveRepository = SaveRepository(application)
    private val soundEngine = SoundEngine()
    private val world = MansionMapBuilder.build()

    private val _uiState = MutableStateFlow(
        MansionUiState(
            hasSavedGame = saveRepository.hasValidSave(),
            settings = saveRepository.loadSettings(),
            world = world,
            guards = initialGuards(Difficulty.NORMAL),
        ),
    )
    val uiState: StateFlow<MansionUiState> = _uiState.asStateFlow()

    private var joystickInput: Vec2 = Vec2(0f, 0f)
    private var keyboardInput: Vec2 = Vec2(0f, 0f)
    private var footstepCooldown = 0f
    private var playtimeAccumulator = 0f
    private var bannerTimer = 0f

    init {
        soundEngine.updateSettings(_uiState.value.settings)
        viewModelScope.launch {
            delay(1750)
            _uiState.update { state ->
                if (state.screen == GameScreen.SPLASH) {
                    state.copy(screen = GameScreen.MAIN_MENU)
                } else state
            }
        }
    }

    private fun initialGuards(difficulty: Difficulty): List<GuardRuntimeState> {
        return world.guardConfigs.map { cfg ->
            val start = cfg.waypoints.first()
            GuardRuntimeState(
                id = cfg.id,
                name = cfg.name,
                position = start,
                facingRad = cfg.initialFacingRad,
                fsmState = GuardFsmState.PATROL,
                waypointIndex = 1 % cfg.waypoints.size,
                stateTimer = 0f,
                suspicion = 0f,
                investigateTarget = null,
                flashlightPolygon = StealthAndCollision.computeFlashlightPolygon(
                    start,
                    cfg.initialFacingRad,
                    difficulty.visionHalfAngleDeg,
                    difficulty.visionRange,
                    world,
                    emptySet(),
                ),
            )
        }
    }

    fun startNewGame() {
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        val settings = _uiState.value.settings
        val startCp = world.checkpoints.first()
        _uiState.update {
            it.copy(
                screen = GameScreen.PLAYING,
                overlay = GameOverlay.NONE,
                player = PlayerState(
                    position = startCp.spawnPos,
                    currentRoom = startCp.roomId,
                    activeCheckpointId = startCp.id,
                ),
                guards = initialGuards(settings.difficulty),
                unlockedDoorIds = emptySet(),
                solvedPuzzleIds = emptySet(),
                collectedClues = emptyList(),
                discoveredLinkIds = emptySet(),
                secretRoomSolved = false,
                playtimeSeconds = 0,
                detectionCount = 0,
                activePuzzle = null,
                bannerMessage = "Infiltrate the 5 locked wings and gather all 5 clues.",
            )
        }
        bannerTimer = 4.5f
        persistCurrentSave()
        soundEngine.startGameplayAmbience()
    }

    fun continueGame() {
        val saved = saveRepository.loadSave() ?: return
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        val settings = _uiState.value.settings.copy(difficultyName = saved.difficultyName)
        saveRepository.saveSettings(settings)
        soundEngine.updateSettings(settings)

        val restoredPos = Vec2(saved.playerX, saved.playerY)
        val unlocked = saved.unlockedDoorIds.toSet()
        _uiState.update {
            it.copy(
                screen = GameScreen.PLAYING,
                overlay = GameOverlay.NONE,
                settings = settings,
                player = PlayerState(
                    position = restoredPos,
                    currentRoom = world.roomAt(restoredPos.x, restoredPos.y),
                    activeCheckpointId = saved.checkpointId,
                ),
                guards = initialGuards(settings.difficulty),
                unlockedDoorIds = unlocked,
                solvedPuzzleIds = saved.solvedPuzzleIds.toSet(),
                collectedClues = saved.collectedClues,
                discoveredLinkIds = saved.discoveredLinkIds.toSet(),
                secretRoomSolved = saved.secretRoomSolved,
                playtimeSeconds = saved.playtimeSeconds,
                detectionCount = saved.detectionCount,
                bannerMessage = "Case resumed at ${world.roomAt(restoredPos.x, restoredPos.y).displayName}.",
            )
        }
        bannerTimer = 3.5f
        soundEngine.startGameplayAmbience()
    }

    fun resetProgress() {
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        saveRepository.deleteSave()
        _uiState.update { it.copy(hasSavedGame = false) }
    }

    fun setJoystickInput(dx: Float, dy: Float) {
        joystickInput = Vec2(dx.coerceIn(-1f, 1f), dy.coerceIn(-1f, 1f))
    }

    fun setKeyboardInput(dx: Float, dy: Float) {
        keyboardInput = Vec2(dx.coerceIn(-1f, 1f), dy.coerceIn(-1f, 1f))
    }

    fun tickSimulation(dtSeconds: Float) {
        val state = _uiState.value
        if (state.screen != GameScreen.PLAYING || state.overlay != GameOverlay.NONE) return
        val dt = dtSeconds.coerceIn(0.001f, 0.05f)

        playtimeAccumulator += dt
        var playtime = state.playtimeSeconds
        if (playtimeAccumulator >= 1f) {
            playtime += 1
            playtimeAccumulator -= 1f
        }

        if (bannerTimer > 0f) {
            bannerTimer -= dt
        }
        val nextPulse = (state.highlightHiddenPulse - dt).coerceAtLeast(0f)

        // Combine joystick + keyboard
        val rawX = (joystickInput.x + keyboardInput.x).coerceIn(-1f, 1f)
        val rawY = (joystickInput.y + keyboardInput.y).coerceIn(-1f, 1f)
        val mag = sqrt(rawX * rawX + rawY * rawY)
        val sens = state.settings.joystickSensitivity.coerceIn(0.5f, 1.5f)
        val moveSpeed = 4.45f * (0.78f + 0.22f * sens)

        var player = state.player
        if (mag > 0.08f) {
            val nx = rawX / mag.coerceAtLeast(1f)
            val ny = rawY / mag.coerceAtLeast(1f)
            val dx = nx * moveSpeed * dt * mag.coerceAtMost(1f)
            val dy = ny * moveSpeed * dt * mag.coerceAtMost(1f)
            val newPos = StealthAndCollision.resolveMovement(
                player.position,
                dx,
                dy,
                world,
                state.unlockedDoorIds,
                world.props,
            )
            val moved = newPos.distanceTo(player.position) > 0.001f
            val facing = atan2(ny, nx)
            player = player.copy(
                position = newPos,
                facingRad = facing,
                isMoving = moved,
                walkPhase = if (moved) player.walkPhase + dt * 11f else player.walkPhase,
                currentRoom = world.roomAt(newPos.x, newPos.y),
            )
            if (moved) {
                footstepCooldown -= dt
                if (footstepCooldown <= 0f) {
                    soundEngine.playSfx(SoundEffect.FOOTSTEP)
                    footstepCooldown = 0.34f
                }
            }
        } else {
            player = player.copy(isMoving = false)
        }

        // Update safe checkpoint if player enters Foyer sanctuary alcove
        val foyerAlcove = world.props.firstOrNull { it.id == "prop_foyer_checkpoint" }
        var bannerMsg = if (bannerTimer > 0f) state.bannerMessage else null
        if (foyerAlcove != null &&
            player.activeCheckpointId != "cp_foyer" &&
            player.position.distanceTo(Vec2(foyerAlcove.tileX, foyerAlcove.tileY)) < 1.3f
        ) {
            player = player.copy(activeCheckpointId = "cp_foyer")
            bannerMsg = "Safe Checkpoint updated: Grand Foyer Brazier"
            bannerTimer = 3.0f
        }

        // Update Guards
        val updatedGuards = ArrayList<GuardRuntimeState>(state.guards.size)
        var caughtBy: String? = null
        state.guards.forEachIndexed { idx, guard ->
            val cfg = world.guardConfigs[idx]
            val result = StealthAndCollision.updateGuardFsm(
                guard = guard,
                waypoints = cfg.waypoints,
                playerPos = player.position,
                dt = dt,
                difficulty = state.settings.difficulty,
                world = world,
                unlockedDoorIds = state.unlockedDoorIds,
            )
            updatedGuards.add(result.updatedGuard)
            if (result.caughtPlayer && caughtBy == null) {
                caughtBy = guard.name
            }
        }

        if (caughtBy != null) {
            soundEngine.playSfx(SoundEffect.GUARD_ALERT)
            val newDetections = state.detectionCount + 1
            _uiState.update {
                it.copy(
                    player = player,
                    guards = updatedGuards,
                    detectionCount = newDetections,
                    playtimeSeconds = playtime,
                    overlay = GameOverlay.CAUGHT,
                    caughtByGuardName = caughtBy ?: "Mansion Guard",
                )
            }
            persistCurrentSave()
            return
        }

        val nearby = findNearestInteractTarget(player.position, state.unlockedDoorIds, state.collectedClueSet)

        _uiState.update {
            it.copy(
                player = player,
                guards = updatedGuards,
                playtimeSeconds = playtime,
                nearbyTarget = nearby,
                highlightHiddenPulse = nextPulse,
                bannerMessage = bannerMsg,
            )
        }
    }

    private fun findNearestInteractTarget(
        playerPos: Vec2,
        unlockedDoorIds: Set<String>,
        collectedClues: Set<ClueId>,
    ): InteractTarget? {
        var bestTarget: InteractTarget? = null
        var bestDist = 1.85f

        for (door in world.doors) {
            val center = StealthAndCollision.doorCenter(door)
            val d = playerPos.distanceTo(center)
            val unlocked = unlockedDoorIds.contains(door.id)
            if (!unlocked && d < bestDist) {
                bestDist = d
                val label = if (door.isSecretDoor) {
                    "Unlock Secret Vault"
                } else {
                    "Unlock ${door.targetRoom.displayName}"
                }
                bestTarget = InteractTarget.Door(door, unlocked, label, center)
            }
        }

        for (prop in world.props) {
            val pos = Vec2(prop.tileX, prop.tileY)
            val d = playerPos.distanceTo(pos)
            if (d < bestDist) {
                val alreadyCollected = prop.clueRewardId != null && collectedClues.contains(prop.clueRewardId)
                if (!alreadyCollected) {
                    bestDist = d
                    val label = if (prop.clueRewardId != null) {
                        "Collect ${prop.title}"
                    } else {
                        "Inspect ${prop.title}"
                    }
                    bestTarget = InteractTarget.Prop(prop, label, pos)
                }
            }
        }
        return bestTarget
    }

    fun onInteractPressed() {
        val state = _uiState.value
        if (state.screen != GameScreen.PLAYING || state.overlay != GameOverlay.NONE) return
        val target = state.nearbyTarget ?: run {
            showBanner("Move closer to a locked door, clue, or prop to interact.")
            return
        }

        when (target) {
            is InteractTarget.Door -> handleDoorInteract(target)
            is InteractTarget.Prop -> handlePropInteract(target.prop)
        }
    }

    private fun handleDoorInteract(target: InteractTarget.Door) {
        val state = _uiState.value
        val door = target.door
        if (door.isSecretDoor) {
            if (state.isSecretRoomReady) {
                soundEngine.playSfx(SoundEffect.SECRET_DISCOVERY)
                _uiState.update {
                    it.copy(
                        unlockedDoorIds = it.unlockedDoorIds + door.id,
                        bannerMessage = "The Grand Secret Room unseals! Step inside to face the final truth.",
                    )
                }
                bannerTimer = 4.5f
                persistCurrentSave()
            } else {
                soundEngine.playSfx(SoundEffect.PUZZLE_FAIL)
                val missingPuzzles = MysteryCatalog.puzzles.size - state.solvedPuzzleIds.size
                val missingClues = ClueId.entries.size - state.collectedClueSet.size
                showBanner("Vault Sealed: Solve $missingPuzzles more lock(s) & collect $missingClues more clue(s).")
            }
            return
        }

        val puzzle = door.puzzleId?.let { MysteryCatalog.puzzleById(it) } ?: return
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            it.copy(
                overlay = GameOverlay.PUZZLE,
                activePuzzle = puzzle,
                puzzleSelection = emptyList(),
                puzzleHintsRevealed = 0,
                puzzleFeedback = null,
                puzzleFeedbackIsError = false,
            )
        }
    }

    private fun handlePropInteract(prop: PropObject) {
        val state = _uiState.value
        if (prop.clueRewardId != null) {
            if (!state.collectedClueSet.contains(prop.clueRewardId)) {
                val record = CollectedClueRecord(
                    clueId = prop.clueRewardId.name,
                    discoveredAtEpochMs = System.currentTimeMillis(),
                )
                soundEngine.playSfx(SoundEffect.CLUE_FOUND)
                _uiState.update {
                    it.copy(
                        collectedClues = it.collectedClues + record,
                        inspectedProp = prop,
                        overlay = GameOverlay.INSPECT_OBJECT,
                    )
                }
                persistCurrentSave()
            }
            return
        }

        if (prop.id == "prop_secret_heirloom" || prop.id == "prop_secret_board") {
            soundEngine.playSfx(SoundEffect.SECRET_DISCOVERY)
            _uiState.update {
                it.copy(
                    overlay = GameOverlay.SECRET_FINALE,
                    finaleFeedback = null,
                )
            }
            return
        }

        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            it.copy(
                inspectedProp = prop,
                overlay = GameOverlay.INSPECT_OBJECT,
            )
        }
    }

    fun onInspectSweepPressed() {
        val state = _uiState.value
        if (state.screen != GameScreen.PLAYING || state.overlay != GameOverlay.NONE) return
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        val nearby = state.nearbyTarget
        if (nearby is InteractTarget.Prop) {
            handlePropInteract(nearby.prop)
            return
        }
        val uncollectedInRoom = world.props.firstOrNull {
            it.roomId == state.player.currentRoom &&
                it.clueRewardId != null &&
                !state.collectedClueSet.contains(it.clueRewardId)
        }
        val msg = if (uncollectedInRoom != null) {
            "Sense Clue Nearby: Search the ${state.player.currentRoom.displayName} for '${uncollectedInRoom.title}'."
        } else {
            "Inspecting ${state.player.currentRoom.displayName} — glowing highlights reveal hidden mechanisms."
        }
        _uiState.update { it.copy(highlightHiddenPulse = 3.0f) }
        showBanner(msg)
    }

    fun togglePuzzleOption(option: String) {
        val state = _uiState.value
        val puzzle = state.activePuzzle ?: return
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        val current = state.puzzleSelection.toMutableList()
        if (current.contains(option)) {
            current.remove(option)
        } else {
            if (current.size < puzzle.expectedSequence.size) {
                current.add(option)
            } else {
                current.removeAt(0)
                current.add(option)
            }
        }
        _uiState.update {
            it.copy(
                puzzleSelection = current,
                puzzleFeedback = null,
                puzzleFeedbackIsError = false,
            )
        }
    }

    fun resetPuzzleSelection() {
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            it.copy(puzzleSelection = emptyList(), puzzleFeedback = null, puzzleFeedbackIsError = false)
        }
    }

    fun revealNextPuzzleHint() {
        val state = _uiState.value
        val puzzle = state.activePuzzle ?: return
        if (state.puzzleHintsRevealed < puzzle.hints.size) {
            soundEngine.playSfx(SoundEffect.UI_CLICK)
            _uiState.update { it.copy(puzzleHintsRevealed = it.puzzleHintsRevealed + 1) }
        }
    }

    fun submitPuzzleAnswer() {
        val state = _uiState.value
        val puzzle = state.activePuzzle ?: return
        val valid = PuzzleValidator.validate(puzzle, state.puzzleSelection)
        if (!valid) {
            soundEngine.playSfx(SoundEffect.PUZZLE_FAIL)
            _uiState.update {
                it.copy(
                    puzzleFeedback = "The brass tumblers click back into place. Review the clues or consult your notes.",
                    puzzleFeedbackIsError = true,
                )
            }
            return
        }

        soundEngine.playSfx(SoundEffect.DOOR_UNLOCK)
        soundEngine.playSfx(SoundEffect.PUZZLE_SUCCESS)
        val matchingDoor = world.doors.firstOrNull { it.puzzleId == puzzle.id }
        val newUnlocked = if (matchingDoor != null) {
            state.unlockedDoorIds + matchingDoor.id
        } else state.unlockedDoorIds

        _uiState.update {
            it.copy(
                unlockedDoorIds = newUnlocked,
                solvedPuzzleIds = it.solvedPuzzleIds + puzzle.id,
                overlay = GameOverlay.NONE,
                activePuzzle = null,
                bannerMessage = "${puzzle.roomId.displayName} unlocked! Search inside for the hidden clue.",
            )
        }
        bannerTimer = 4.2f
        persistCurrentSave()
    }

    fun selectClueInInventory(clueId: ClueId) {
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update { it.copy(selectedClueId = clueId) }
    }

    fun onEvidenceBoardClueTap(clueId: ClueId) {
        val state = _uiState.value
        if (!state.collectedClueSet.contains(clueId)) return
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        val first = state.boardSelectedClue
        if (first == null) {
            _uiState.update {
                it.copy(boardSelectedClue = clueId, boardFeedback = "Select a second clue to test a connection.")
            }
            return
        }
        if (first == clueId) {
            _uiState.update { it.copy(boardSelectedClue = null, boardFeedback = null) }
            return
        }

        val match = MysteryCatalog.evidenceLinks.firstOrNull { link ->
            (link.first == first && link.second == clueId) ||
                (link.second == first && link.first == clueId)
        }
        if (match != null) {
            soundEngine.playSfx(SoundEffect.CLUE_FOUND)
            _uiState.update {
                it.copy(
                    discoveredLinkIds = it.discoveredLinkIds + match.id,
                    boardSelectedClue = null,
                    boardFeedback = "Linked: ${match.insightTitle} — ${match.insightBody}",
                )
            }
            persistCurrentSave()
        } else {
            soundEngine.playSfx(SoundEffect.PUZZLE_FAIL)
            _uiState.update {
                it.copy(
                    boardSelectedClue = null,
                    boardFeedback = "No direct connection between those two items. Try pairing related forensic or timeline clues.",
                )
            }
        }
    }

    fun updateFinaleChoice(suspect: String? = null, method: String? = null, motive: String? = null) {
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            it.copy(
                finaleSuspect = suspect ?: it.finaleSuspect,
                finaleMethod = method ?: it.finaleMethod,
                finaleMotive = motive ?: it.finaleMotive,
                finaleFeedback = null,
            )
        }
    }

    fun submitSecretFinale() {
        val state = _uiState.value
        val valid = PuzzleValidator.validateSecretFinale(
            suspect = state.finaleSuspect,
            method = state.finaleMethod,
            motive = state.finaleMotive,
            collectedCluesCount = state.collectedClueSet.size,
        )
        if (!valid) {
            soundEngine.playSfx(SoundEffect.PUZZLE_FAIL)
            _uiState.update {
                it.copy(finaleFeedback = "That deduction contradicts your evidence. Check Dr. Ashford's ledger, the Moonbloom tea, and the Crest Key.")
            }
            return
        }

        soundEngine.stopGameplayAmbience()
        soundEngine.playSfx(SoundEffect.VICTORY)
        _uiState.update {
            it.copy(
                secretRoomSolved = true,
                overlay = GameOverlay.NONE,
                screen = GameScreen.VICTORY,
            )
        }
        persistCurrentSave()
    }

    fun recoverAtCheckpoint() {
        val state = _uiState.value
        val cp = world.checkpoints.firstOrNull { it.id == state.player.activeCheckpointId }
            ?: world.checkpoints.first()
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            it.copy(
                overlay = GameOverlay.NONE,
                player = it.player.copy(
                    position = cp.spawnPos,
                    currentRoom = cp.roomId,
                    isMoving = false,
                ),
                guards = initialGuards(it.settings.difficulty),
                bannerMessage = "Returned safely to ${cp.label}. Clues & unlocked rooms preserved.",
            )
        }
        bannerTimer = 3.8f
        persistCurrentSave()
    }

    fun openOverlay(overlay: GameOverlay) {
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            val defaultClue = if (overlay == GameOverlay.INVENTORY && it.selectedClueId == null) {
                it.collectedClueSet.firstOrNull()
            } else it.selectedClueId
            it.copy(overlay = overlay, selectedClueId = defaultClue)
        }
    }

    fun openSettings(fromOverlay: GameOverlay) {
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            it.copy(
                overlay = GameOverlay.SETTINGS,
                returnOverlayAfterSettings = fromOverlay,
            )
        }
    }

    fun closeOverlay() {
        val state = _uiState.value
        if (state.overlay == GameOverlay.CAUGHT) {
            recoverAtCheckpoint()
            return
        }
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        val nextOverlay = if (state.overlay == GameOverlay.SETTINGS) {
            state.returnOverlayAfterSettings
        } else GameOverlay.NONE
        _uiState.update { it.copy(overlay = nextOverlay) }
    }

    fun handleBackPress(): Boolean {
        val state = _uiState.value
        return when {
            state.overlay == GameOverlay.CAUGHT -> {
                recoverAtCheckpoint()
                true
            }
            state.overlay != GameOverlay.NONE -> {
                closeOverlay()
                true
            }
            state.screen == GameScreen.PLAYING -> {
                openOverlay(GameOverlay.PAUSE)
                true
            }
            state.screen == GameScreen.VICTORY -> {
                returnToMainMenu()
                true
            }
            else -> false
        }
    }

    fun manualSave() {
        val ok = persistCurrentSave()
        soundEngine.playSfx(if (ok) SoundEffect.PUZZLE_SUCCESS else SoundEffect.PUZZLE_FAIL)
        showBanner(if (ok) "Case file saved safely." else "Could not write save file.")
    }

    fun returnToMainMenu() {
        if (_uiState.value.screen == GameScreen.PLAYING) {
            persistCurrentSave()
        }
        soundEngine.stopGameplayAmbience()
        soundEngine.playSfx(SoundEffect.UI_CLICK)
        _uiState.update {
            it.copy(
                screen = GameScreen.MAIN_MENU,
                overlay = GameOverlay.NONE,
                hasSavedGame = saveRepository.hasValidSave(),
            )
        }
    }

    fun updateSettings(transform: (GameSettings) -> GameSettings) {
        val updated = transform(_uiState.value.settings)
        saveRepository.saveSettings(updated)
        soundEngine.updateSettings(updated)
        _uiState.update { it.copy(settings = updated) }
    }

    fun onAppBackgrounded() {
        val state = _uiState.value
        if (state.screen == GameScreen.PLAYING) {
            persistCurrentSave()
            if (state.overlay == GameOverlay.NONE) {
                _uiState.update { it.copy(overlay = GameOverlay.PAUSE) }
            }
        }
        soundEngine.stopGameplayAmbience()
    }

    fun onAppResumed() {
        val state = _uiState.value
        if (state.screen == GameScreen.PLAYING) {
            soundEngine.startGameplayAmbience()
        }
    }

    private fun showBanner(message: String) {
        bannerTimer = 3.2f
        _uiState.update { it.copy(bannerMessage = message) }
    }

    private fun persistCurrentSave(): Boolean {
        val state = _uiState.value
        val data = SaveData(
            playerX = state.player.position.x,
            playerY = state.player.position.y,
            checkpointId = state.player.activeCheckpointId,
            unlockedDoorIds = state.unlockedDoorIds.toList(),
            solvedPuzzleIds = state.solvedPuzzleIds.toList(),
            collectedClues = state.collectedClues,
            discoveredLinkIds = state.discoveredLinkIds.toList(),
            secretRoomSolved = state.secretRoomSolved,
            playtimeSeconds = state.playtimeSeconds,
            detectionCount = state.detectionCount,
            difficultyName = state.settings.difficultyName,
        )
        val ok = saveRepository.saveGame(data)
        if (ok) {
            _uiState.update { it.copy(hasSavedGame = true) }
        }
        return ok
    }

    override fun onCleared() {
        super.onCleared()
        soundEngine.release()
    }
}

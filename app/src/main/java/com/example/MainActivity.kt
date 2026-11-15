package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.GameOverlay
import com.example.model.GameScreen
import com.example.ui.canvas.MansionCanvas
import com.example.ui.hud.GameplayHud
import com.example.ui.modals.CaughtModal
import com.example.ui.modals.InspectObjectModal
import com.example.ui.modals.InventoryAndEvidenceModal
import com.example.ui.modals.MansionMapModal
import com.example.ui.modals.PauseMenuModal
import com.example.ui.modals.PuzzleModal
import com.example.ui.modals.SecretFinaleModal
import com.example.ui.modals.SettingsModal
import com.example.ui.screens.MainMenuScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VictoryScreen
import com.example.ui.theme.MansionMysteryTheme
import com.example.viewmodel.MansionGameViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        configureImmersiveBars()
        setContent {
            MansionMysteryTheme {
                MansionMysteryApp()
            }
        }
    }

    private fun configureImmersiveBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())
    }
}

@Composable
fun MansionMysteryApp(
    viewModel: MansionGameViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    var renderTimeSeconds by remember { mutableFloatStateOf(0f) }

    // Pause and save automatically when backgrounded
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> viewModel.onAppBackgrounded()
                Lifecycle.Event.ON_RESUME -> viewModel.onAppResumed()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Android back-button handling across all secondary screens & modals
    BackHandler(enabled = state.screen != GameScreen.MAIN_MENU && state.screen != GameScreen.SPLASH || state.overlay != GameOverlay.NONE) {
        viewModel.handleBackPress()
    }

    // 60 FPS frame-timed game loop during active gameplay
    LaunchedEffect(state.screen, state.overlay) {
        if (state.screen == GameScreen.PLAYING && state.overlay == GameOverlay.NONE) {
            var lastNanos = 0L
            while (true) {
                withFrameNanos { frameNanos ->
                    if (lastNanos != 0L) {
                        val dt = (frameNanos - lastNanos) / 1_000_000_000f
                        renderTimeSeconds += dt
                        viewModel.tickSimulation(dt)
                    }
                    lastNanos = frameNanos
                }
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    var heldKeys by remember { mutableStateOf(emptySet<Key>()) }

    LaunchedEffect(heldKeys) {
        var kx = 0f
        var ky = 0f
        if (heldKeys.contains(Key.A) || heldKeys.contains(Key.DirectionLeft)) kx -= 1f
        if (heldKeys.contains(Key.D) || heldKeys.contains(Key.DirectionRight)) kx += 1f
        if (heldKeys.contains(Key.W) || heldKeys.contains(Key.DirectionUp)) ky -= 1f
        if (heldKeys.contains(Key.S) || heldKeys.contains(Key.DirectionDown)) ky += 1f
        viewModel.setKeyboardInput(kx, ky)
    }

    LaunchedEffect(state.screen) {
        if (state.screen == GameScreen.PLAYING) {
            runCatching { focusRequester.requestFocus() }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onKeyEvent { event ->
                val isMoveKey = event.key in setOf(
                    Key.W, Key.A, Key.S, Key.D,
                    Key.DirectionUp, Key.DirectionDown, Key.DirectionLeft, Key.DirectionRight,
                )
                if (isMoveKey) {
                    heldKeys = if (event.type == KeyEventType.KeyDown) {
                        heldKeys + event.key
                    } else if (event.type == KeyEventType.KeyUp) {
                        heldKeys - event.key
                    } else heldKeys
                    true
                } else if (event.type == KeyEventType.KeyDown && (event.key == Key.E || event.key == Key.Spacebar)) {
                    viewModel.onInteractPressed()
                    true
                } else {
                    false
                }
            },
    ) {
        when (state.screen) {
            GameScreen.SPLASH -> SplashScreen()

            GameScreen.MAIN_MENU -> MainMenuScreen(
                hasSavedGame = state.hasSavedGame,
                difficulty = state.settings.difficulty,
                onNewGame = viewModel::startNewGame,
                onContinueGame = viewModel::continueGame,
                onOpenSettings = { viewModel.openSettings(GameOverlay.NONE) },
                onResetSave = viewModel::resetProgress,
                onSelectDifficulty = { diff ->
                    viewModel.updateSettings { it.copy(difficultyName = diff.name) }
                },
            )

            GameScreen.PLAYING -> {
                MansionCanvas(
                    world = state.world,
                    player = state.player,
                    guards = state.guards,
                    unlockedDoorIds = state.unlockedDoorIds,
                    collectedClues = state.collectedClueSet,
                    nearbyTarget = state.nearbyTarget,
                    highlightPulse = state.highlightHiddenPulse,
                    timeSeconds = renderTimeSeconds,
                )
                GameplayHud(
                    currentRoom = state.player.currentRoom,
                    cluesCollected = state.collectedClueSet.size,
                    detectionCount = state.detectionCount,
                    maxSuspicion = state.maxGuardSuspicion,
                    nearbyTarget = state.nearbyTarget,
                    bannerMessage = state.bannerMessage,
                    onJoystickMove = viewModel::setJoystickInput,
                    onInteract = viewModel::onInteractPressed,
                    onInspect = viewModel::onInspectSweepPressed,
                    onOpenInventory = { viewModel.openOverlay(GameOverlay.INVENTORY) },
                    onOpenMap = { viewModel.openOverlay(GameOverlay.MAP) },
                    onOpenPause = { viewModel.openOverlay(GameOverlay.PAUSE) },
                )
            }

            GameScreen.VICTORY -> VictoryScreen(
                playtimeSeconds = state.playtimeSeconds,
                detectionCount = state.detectionCount,
                cluesCollected = state.collectedClueSet.size,
                difficulty = state.settings.difficulty,
                onPlayAgain = viewModel::startNewGame,
                onMainMenu = viewModel::returnToMainMenu,
            )
        }

        // Modal overlays
        when (state.overlay) {
            GameOverlay.NONE -> Unit

            GameOverlay.PUZZLE -> {
                state.activePuzzle?.let { puzzle ->
                    PuzzleModal(
                        puzzle = puzzle,
                        selectedSequence = state.puzzleSelection,
                        hintsRevealed = state.puzzleHintsRevealed,
                        feedback = state.puzzleFeedback,
                        feedbackIsError = state.puzzleFeedbackIsError,
                        onToggleOption = viewModel::togglePuzzleOption,
                        onReset = viewModel::resetPuzzleSelection,
                        onRevealHint = viewModel::revealNextPuzzleHint,
                        onSubmit = viewModel::submitPuzzleAnswer,
                        onClose = viewModel::closeOverlay,
                    )
                }
            }

            GameOverlay.INVENTORY -> InventoryAndEvidenceModal(
                collectedClues = state.collectedClues,
                discoveredLinkIds = state.discoveredLinkIds,
                selectedClueId = state.selectedClueId,
                boardSelectedClue = state.boardSelectedClue,
                boardFeedback = state.boardFeedback,
                onSelectClue = viewModel::selectClueInInventory,
                onTapBoardClue = viewModel::onEvidenceBoardClueTap,
                onClose = viewModel::closeOverlay,
            )

            GameOverlay.MAP -> MansionMapModal(
                world = state.world,
                player = state.player,
                unlockedDoorIds = state.unlockedDoorIds,
                collectedClues = state.collectedClueSet,
                onClose = viewModel::closeOverlay,
            )

            GameOverlay.PAUSE -> PauseMenuModal(
                playtimeSeconds = state.playtimeSeconds,
                cluesCollected = state.collectedClueSet.size,
                detectionCount = state.detectionCount,
                onResume = viewModel::closeOverlay,
                onSave = viewModel::manualSave,
                onSettings = { viewModel.openSettings(GameOverlay.PAUSE) },
                onMainMenu = viewModel::returnToMainMenu,
            )

            GameOverlay.SETTINGS -> SettingsModal(
                settings = state.settings,
                onUpdateSettings = viewModel::updateSettings,
                onClose = viewModel::closeOverlay,
            )

            GameOverlay.CAUGHT -> CaughtModal(
                guardName = state.caughtByGuardName,
                detectionCount = state.detectionCount,
                onRecoverCheckpoint = viewModel::recoverAtCheckpoint,
            )

            GameOverlay.INSPECT_OBJECT -> {
                state.inspectedProp?.let { prop ->
                    InspectObjectModal(
                        prop = prop,
                        onClose = viewModel::closeOverlay,
                    )
                }
            }

            GameOverlay.SECRET_FINALE -> SecretFinaleModal(
                suspect = state.finaleSuspect,
                method = state.finaleMethod,
                motive = state.finaleMotive,
                feedback = state.finaleFeedback,
                onSelect = viewModel::updateFinaleChoice,
                onSubmit = viewModel::submitSecretFinale,
                onClose = viewModel::closeOverlay,
            )
        }
    }
}

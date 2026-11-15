package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Difficulty
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.CrimsonWax
import com.example.ui.theme.EmeraldPatina
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.MutedPewter
import com.example.ui.theme.PaleGold
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.VelvetIndigo

@Composable
fun SplashScreen() {
    val alpha = remember { Animatable(0f) }
    val scale = remember { Animatable(0.88f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(750, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = tween(900, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(VelvetIndigo, MidnightVoid),
                ),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .alpha(alpha.value)
                .scale(scale.value)
                .padding(24.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.mansion_icon_fg),
                contentDescription = "Mansion Mystery Crest",
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .border(2.dp, AntiqueGold, CircleShape),
                contentScale = ContentScale.Crop,
            )
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "MANSION MYSTERY",
                style = MaterialTheme.typography.displayLarge,
                color = PaleGold,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(id = R.string.subtitle_last_secret),
                style = MaterialTheme.typography.titleMedium,
                color = AntiqueGold,
                letterSpacing = androidx.compose.ui.unit.TextUnit(4f, androidx.compose.ui.unit.TextUnitType.Sp),
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "Ravenshollow Manor • Case File 1894",
                style = MaterialTheme.typography.bodyMedium,
                color = MutedPewter,
            )
        }
    }
}

@Composable
fun MainMenuScreen(
    hasSavedGame: Boolean,
    difficulty: Difficulty,
    onNewGame: () -> Unit,
    onContinueGame: () -> Unit,
    onOpenSettings: () -> Unit,
    onResetSave: () -> Unit,
    onSelectDifficulty: (Difficulty) -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(MidnightVoid)) {
        Image(
            painter = painterResource(id = R.drawable.img_manor_hero),
            contentDescription = "Ravenshollow Manor at midnight",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MidnightVoid.copy(alpha = 0.92f),
                            MidnightVoid.copy(alpha = 0.72f),
                            MidnightVoid.copy(alpha = 0.90f),
                        ),
                    ),
                ),
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left column: Title, lore & detective badge
            Column(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_detective_portrait),
                        contentDescription = "Detective portrait",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .border(2.dp, AntiqueGold, CircleShape),
                        contentScale = ContentScale.Crop,
                    )
                    Column {
                        Text(
                            text = "CASE DOSSIER NO. 1894",
                            style = MaterialTheme.typography.labelSmall,
                            color = AntiqueGold,
                        )
                        Text(
                            text = stringResource(id = R.string.manor_name),
                            style = MaterialTheme.typography.titleMedium,
                            color = ParchmentIvory,
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "MANSION MYSTERY",
                    style = MaterialTheme.typography.displayLarge,
                    color = PaleGold,
                )
                Text(
                    text = stringResource(id = R.string.subtitle_last_secret),
                    style = MaterialTheme.typography.headlineMedium,
                    color = AntiqueGold,
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Sneak past lantern patrols, unlock five sealed Victorian wings, link the hidden evidence, and recover the stolen Ravenshollow Star-Compass.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentIvory.copy(alpha = 0.88f),
                )
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = stringResource(id = R.string.difficulty_label),
                    style = MaterialTheme.typography.labelSmall,
                    color = MutedPewter,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Difficulty.entries.forEach { level ->
                        FilterChip(
                            selected = difficulty == level,
                            onClick = { onSelectDifficulty(level) },
                            label = {
                                Text(
                                    text = level.displayName,
                                    style = MaterialTheme.typography.labelSmall,
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AntiqueGold,
                                selectedLabelColor = MidnightVoid,
                                containerColor = CharcoalSlate.copy(alpha = 0.85f),
                                labelColor = ParchmentIvory,
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = difficulty == level,
                                borderColor = BorderSteel,
                                selectedBorderColor = PaleGold,
                            ),
                            modifier = Modifier.testTag("difficulty_${level.name.lowercase()}"),
                        )
                    }
                }
            }

            // Right column: Action card
            Card(
                modifier = Modifier
                    .weight(0.85f)
                    .fillMaxHeight(),
                colors = CardDefaults.cardColors(containerColor = CharcoalSlate.copy(alpha = 0.92f)),
                border = BorderStroke(1.5.dp, AntiqueGold.copy(alpha = 0.55f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (hasSavedGame) {
                        Button(
                            onClick = onContinueGame,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_continue_game"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AntiqueGold,
                                contentColor = MidnightVoid,
                            ),
                        ) {
                            Icon(Icons.Outlined.Restore, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.btn_continue_game),
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Button(
                        onClick = onNewGame,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("btn_new_game"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (hasSavedGame) VelvetIndigo else AntiqueGold,
                            contentColor = if (hasSavedGame) PaleGold else MidnightVoid,
                        ),
                    ) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.btn_new_game),
                            fontWeight = FontWeight.Bold,
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("btn_settings"),
                        border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.7f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentIvory),
                    ) {
                        Icon(Icons.Filled.Settings, contentDescription = null, tint = AntiqueGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = stringResource(id = R.string.btn_settings))
                    }

                    if (hasSavedGame) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = onResetSave,
                            modifier = Modifier.testTag("btn_reset_save"),
                            colors = ButtonDefaults.textButtonColors(contentColor = CrimsonWax),
                        ) {
                            Icon(Icons.Outlined.DeleteOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = stringResource(id = R.string.btn_reset_save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VictoryScreen(
    playtimeSeconds: Int,
    detectionCount: Int,
    cluesCollected: Int,
    difficulty: Difficulty,
    onPlayAgain: () -> Unit,
    onMainMenu: () -> Unit,
) {
    val mins = playtimeSeconds / 60
    val secs = playtimeSeconds % 60
    val formattedTime = String.format("%02d:%02d", mins, secs)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(colors = listOf(VelvetIndigo, MidnightVoid)),
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate.copy(alpha = 0.96f)),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_royal_heirloom),
                    contentDescription = "Ravenshollow Star-Compass Heirloom",
                    modifier = Modifier
                        .size(210.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(2.dp, AntiqueGold, RoundedCornerShape(14.dp)),
                    contentScale = ContentScale.Crop,
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = null, tint = AntiqueGold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(id = R.string.victory_title).uppercase(),
                            style = MaterialTheme.typography.displayMedium,
                            color = PaleGold,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(id = R.string.victory_subtitle),
                        style = MaterialTheme.typography.bodyLarge,
                        color = ParchmentIvory,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Confronted with the forged ledger, the Moonbloom tea residue, and the Crest Key, Dr. Julian Ashford confessed to orchestrating the midnight lock-down.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MutedPewter,
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatBadge("Time", formattedTime, AntiqueGold)
                        StatBadge("Clues", "$cluesCollected / 5", EmeraldPatina)
                        StatBadge("Alerts", "$detectionCount", if (detectionCount == 0) EmeraldPatina else Color(0xFFF59E0B))
                        StatBadge("Mode", difficulty.displayName, PaleGold)
                    }

                    Spacer(modifier = Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = onPlayAgain,
                            modifier = Modifier.height(48.dp).testTag("btn_play_again"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AntiqueGold,
                                contentColor = MidnightVoid,
                            ),
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(stringResource(id = R.string.btn_play_again), fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = onMainMenu,
                            modifier = Modifier.height(48.dp).testTag("btn_victory_menu"),
                            border = BorderStroke(1.dp, AntiqueGold),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentIvory),
                        ) {
                            Text(stringResource(id = R.string.btn_main_menu))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, valueColor: Color) {
    Column(
        modifier = Modifier
            .background(VelvetIndigo, RoundedCornerShape(8.dp))
            .border(1.dp, BorderSteel, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MutedPewter)
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = valueColor)
    }
}

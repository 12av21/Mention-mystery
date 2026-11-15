package com.example.ui.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.Difficulty
import com.example.model.GameSettings
import com.example.model.PropObject
import com.example.mystery.MysteryCatalog
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.CrimsonWax
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.MutedPewter
import com.example.ui.theme.PaleGold
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.VelvetIndigo

@Composable
fun PauseMenuModal(
    playtimeSeconds: Int,
    cluesCollected: Int,
    detectionCount: Int,
    onResume: () -> Unit,
    onSave: () -> Unit,
    onSettings: () -> Unit,
    onMainMenu: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightVoid.copy(alpha = 0.84f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 460.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Column(
                modifier = Modifier.padding(22.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("INVESTIGATION PAUSED", style = MaterialTheme.typography.headlineMedium, color = PaleGold)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Clues: $cluesCollected/5  •  Alerts: $detectionCount  •  Time: ${playtimeSeconds / 60}m ${playtimeSeconds % 60}s",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MutedPewter,
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_pause_resume"),
                    colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold, contentColor = MidnightVoid),
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_resume), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_pause_save"),
                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.7f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentIvory),
                ) {
                    Icon(Icons.Filled.Save, contentDescription = null, tint = AntiqueGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_save_progress))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_pause_settings"),
                    border = BorderStroke(1.dp, BorderSteel),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ParchmentIvory),
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null, tint = AntiqueGold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_settings))
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onMainMenu,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_pause_menu"),
                    border = BorderStroke(1.dp, CrimsonWax.copy(alpha = 0.8f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonGlow),
                ) {
                    Icon(Icons.Filled.Home, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_main_menu))
                }
            }
        }
    }
}

@Composable
fun SettingsModal(
    settings: GameSettings,
    onUpdateSettings: ((GameSettings) -> GameSettings) -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightVoid.copy(alpha = 0.86f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth().fillMaxHeight(0.92f),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(id = R.string.settings_title).uppercase(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = PaleGold,
                    )
                    IconButton(onClick = onClose, modifier = Modifier.testTag("btn_settings_close")) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.btn_close), tint = ParchmentIvory)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(stringResource(id = R.string.mute_all_label), style = MaterialTheme.typography.bodyLarge, color = ParchmentIvory)
                        Switch(
                            checked = settings.muted,
                            onCheckedChange = { m -> onUpdateSettings { it.copy(muted = m) } },
                            colors = SwitchDefaults.colors(checkedThumbColor = MidnightVoid, checkedTrackColor = AntiqueGold),
                            modifier = Modifier.testTag("switch_mute_audio"),
                        )
                    }

                    VolumeSliderRow(
                        label = stringResource(id = R.string.master_volume_label),
                        value = settings.masterVolume,
                        onChange = { v -> onUpdateSettings { it.copy(masterVolume = v) } },
                    )
                    VolumeSliderRow(
                        label = stringResource(id = R.string.music_volume_label),
                        value = settings.musicVolume,
                        onChange = { v -> onUpdateSettings { it.copy(musicVolume = v) } },
                    )
                    VolumeSliderRow(
                        label = stringResource(id = R.string.sfx_volume_label),
                        value = settings.sfxVolume,
                        onChange = { v -> onUpdateSettings { it.copy(sfxVolume = v) } },
                    )
                    VolumeSliderRow(
                        label = stringResource(id = R.string.joystick_sensitivity_label),
                        value = (settings.joystickSensitivity - 0.5f),
                        displayPercent = "${(settings.joystickSensitivity * 100).toInt()}%",
                        onChange = { v -> onUpdateSettings { it.copy(joystickSensitivity = 0.5f + v) } },
                    )

                    Text(stringResource(id = R.string.difficulty_label), style = MaterialTheme.typography.labelLarge, color = AntiqueGold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Difficulty.entries.forEach { diff ->
                            FilterChip(
                                selected = settings.difficulty == diff,
                                onClick = { onUpdateSettings { it.copy(difficultyName = diff.name) } },
                                label = { Text(diff.displayName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = AntiqueGold,
                                    selectedLabelColor = MidnightVoid,
                                    containerColor = VelvetIndigo,
                                    labelColor = ParchmentIvory,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumeSliderRow(
    label: String,
    value: Float,
    displayPercent: String = "${(value * 100).toInt()}%",
    onChange: (Float) -> Unit,
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, color = ParchmentIvory)
            Text(displayPercent, style = MaterialTheme.typography.labelLarge, color = AntiqueGold)
        }
        Slider(
            value = value.coerceIn(0f, 1f),
            onValueChange = onChange,
            colors = SliderDefaults.colors(
                thumbColor = AntiqueGold,
                activeTrackColor = AntiqueGold,
                inactiveTrackColor = VelvetIndigo,
            ),
        )
    }
}

@Composable
fun CaughtModal(
    guardName: String,
    detectionCount: Int,
    onRecoverCheckpoint: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CrimsonWax.copy(alpha = 0.35f))
            .background(MidnightVoid.copy(alpha = 0.72f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 500.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(2.dp, CrimsonGlow),
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(Icons.Filled.Warning, contentDescription = null, tint = CrimsonGlow, modifier = Modifier.size(44.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(id = R.string.caught_title), style = MaterialTheme.typography.headlineMedium, color = CrimsonGlow)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$guardName caught you in his lantern beam! (Total Alerts: $detectionCount)",
                    style = MaterialTheme.typography.labelLarge,
                    color = PaleGold,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = R.string.caught_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentIvory,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onRecoverCheckpoint,
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("btn_recover_checkpoint"),
                    colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold, contentColor = MidnightVoid),
                ) {
                    Icon(Icons.Filled.Shield, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(id = R.string.btn_return_checkpoint), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun InspectObjectModal(
    prop: PropObject,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightVoid.copy(alpha = 0.82f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.widthIn(max = 580.dp).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (prop.portraitRes != null) {
                    Image(
                        painter = painterResource(id = prop.portraitRes),
                        contentDescription = prop.title,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.5.dp, AntiqueGold, RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (prop.clueRewardId != null) "EVIDENCE LOGGED" else prop.roomId.displayName.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = AntiqueGold,
                    )
                    Text(prop.title, style = MaterialTheme.typography.headlineMedium, color = PaleGold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(prop.inspectText, style = MaterialTheme.typography.bodyLarge, color = ParchmentIvory)
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier.height(48.dp).testTag("btn_inspect_close"),
                        colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold, contentColor = MidnightVoid),
                    ) {
                        Text(stringResource(id = R.string.btn_close), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SecretFinaleModal(
    suspect: String?,
    method: String?,
    motive: String?,
    feedback: String?,
    onSelect: (suspect: String?, method: String?, motive: String?) -> Unit,
    onSubmit: () -> Unit,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightVoid.copy(alpha = 0.88f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("GRAND SECRET ROOM: FINAL DEDUCTION", style = MaterialTheme.typography.headlineMedium, color = PaleGold)
                    IconButton(onClick = onClose, modifier = Modifier.testTag("btn_finale_close")) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.btn_close), tint = ParchmentIvory)
                    }
                }

                Text(MysteryCatalog.finalDeductionPrompt, style = MaterialTheme.typography.bodyMedium, color = ParchmentIvory)
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    DeductionColumn(
                        title = "1. The Culprit",
                        options = MysteryCatalog.finalSuspectOptions,
                        selected = suspect,
                        onPick = { onSelect(it, null, null) },
                        modifier = Modifier.weight(1f),
                    )
                    DeductionColumn(
                        title = "2. The Method",
                        options = MysteryCatalog.finalMethodOptions,
                        selected = method,
                        onPick = { onSelect(null, it, null) },
                        modifier = Modifier.weight(1f),
                    )
                    DeductionColumn(
                        title = "3. The Motive",
                        options = MysteryCatalog.finalMotiveOptions,
                        selected = motive,
                        onPick = { onSelect(null, null, it) },
                        modifier = Modifier.weight(1f),
                    )
                }

                if (!feedback.isNullOrBlank()) {
                    Text(feedback, style = MaterialTheme.typography.bodyMedium, color = CrimsonGlow, modifier = Modifier.padding(vertical = 4.dp))
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onSubmit,
                        enabled = suspect != null && method != null && motive != null,
                        modifier = Modifier.height(48.dp).testTag("btn_finale_submit"),
                        colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold, contentColor = MidnightVoid),
                    ) {
                        Text("Unseal Heirloom Vault", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun DeductionColumn(
    title: String,
    options: List<String>,
    selected: String?,
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxHeight()
            .background(MidnightVoid.copy(alpha = 0.6f), RoundedCornerShape(10.dp))
            .border(1.dp, BorderSteel, RoundedCornerShape(10.dp))
            .padding(10.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = AntiqueGold)
        options.forEach { opt ->
            val active = opt == selected
            Surface(
                color = if (active) AntiqueGold else VelvetIndigo,
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, if (active) PaleGold else BorderSteel),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPick(opt) }
                    .testTag("finale_opt_$opt"),
            ) {
                Text(
                    text = opt,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (active) MidnightVoid else ParchmentIvory,
                    fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(10.dp),
                )
            }
        }
    }
}

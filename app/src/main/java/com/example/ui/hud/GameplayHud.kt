package com.example.ui.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.InteractTarget
import com.example.model.RoomId
import com.example.ui.theme.AmberLantern
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.CrimsonGlow
import com.example.ui.theme.EmeraldPatina
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.MutedPewter
import com.example.ui.theme.PaleGold
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.VelvetIndigo
import kotlin.math.sqrt

@Composable
fun GameplayHud(
    currentRoom: RoomId,
    cluesCollected: Int,
    detectionCount: Int,
    maxSuspicion: Float,
    nearbyTarget: InteractTarget?,
    bannerMessage: String?,
    onJoystickMove: (Float, Float) -> Unit,
    onInteract: () -> Unit,
    onInspect: () -> Unit,
    onOpenInventory: () -> Unit,
    onOpenMap: () -> Unit,
    onOpenPause: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        // Top HUD Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            // Room & Clue Pill
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Surface(
                    color = CharcoalSlate.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.55f)),
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Column {
                            Text(
                                text = currentRoom.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = PaleGold,
                            )
                            Text(
                                text = currentRoom.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MutedPewter,
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(VelvetIndigo, RoundedCornerShape(6.dp))
                                .border(1.dp, BorderSteel, RoundedCornerShape(6.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp),
                        ) {
                            Text(
                                text = "$cluesCollected / 5 Clues",
                                style = MaterialTheme.typography.labelLarge,
                                color = if (cluesCollected == 5) EmeraldPatina else AntiqueGold,
                            )
                        }
                        if (detectionCount > 0) {
                            Text(
                                text = "Alerts: $detectionCount",
                                style = MaterialTheme.typography.labelSmall,
                                color = AmberLantern,
                            )
                        }
                    }
                }

                // Guard suspicion warning bar
                AnimatedVisibility(visible = maxSuspicion > 0.04f, enter = fadeIn(), exit = fadeOut()) {
                    Surface(
                        color = CharcoalSlate.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (maxSuspicion > 0.65f) CrimsonGlow else AmberLantern),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Visibility,
                                contentDescription = "Guard vision warning",
                                tint = if (maxSuspicion > 0.65f) CrimsonGlow else AmberLantern,
                                modifier = Modifier.size(16.dp),
                            )
                            LinearProgressIndicator(
                                progress = { maxSuspicion.coerceIn(0f, 1f) },
                                modifier = Modifier.width(120.dp).height(6.dp),
                                color = if (maxSuspicion > 0.65f) CrimsonGlow else AmberLantern,
                                trackColor = MidnightVoid,
                            )
                        }
                    }
                }
            }

            // Top-right quick utility buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilledTonalIconButton(
                    onClick = onOpenInventory,
                    modifier = Modifier.size(48.dp).testTag("btn_inventory"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = CharcoalSlate.copy(alpha = 0.9f),
                        contentColor = AntiqueGold,
                    ),
                ) {
                    Icon(Icons.Filled.Inventory2, contentDescription = stringResource(id = R.string.btn_inventory))
                }
                FilledTonalIconButton(
                    onClick = onOpenMap,
                    modifier = Modifier.size(48.dp).testTag("btn_map"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = CharcoalSlate.copy(alpha = 0.9f),
                        contentColor = AntiqueGold,
                    ),
                ) {
                    Icon(Icons.Filled.Explore, contentDescription = stringResource(id = R.string.btn_map))
                }
                FilledTonalIconButton(
                    onClick = onOpenPause,
                    modifier = Modifier.size(48.dp).testTag("btn_pause"),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = CharcoalSlate.copy(alpha = 0.9f),
                        contentColor = ParchmentIvory,
                    ),
                ) {
                    Icon(Icons.Filled.Pause, contentDescription = stringResource(id = R.string.btn_pause))
                }
            }
        }

        // Bottom-left: Virtual Analog Joystick
        VirtualJoystick(
            onMove = onJoystickMove,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 6.dp, bottom = 6.dp)
                .testTag("virtual_joystick"),
        )

        // Bottom-center: Toast / Discovery Banner
        AnimatedVisibility(
            visible = !bannerMessage.isNullOrBlank(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 160.dp, vertical = 10.dp),
        ) {
            Surface(
                color = CharcoalSlate.copy(alpha = 0.92f),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.7f)),
            ) {
                Text(
                    text = bannerMessage.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ParchmentIvory,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                )
            }
        }

        // Bottom-right: Interact & Inspect Action Buttons
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 6.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (nearbyTarget != null) {
                Surface(
                    color = MidnightVoid.copy(alpha = 0.88f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, AntiqueGold),
                ) {
                    Text(
                        text = nearbyTarget.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = PaleGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = onInspect,
                    modifier = Modifier.height(52.dp).testTag("btn_inspect"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VelvetIndigo.copy(alpha = 0.92f),
                        contentColor = ParchmentIvory,
                    ),
                    border = BorderStroke(1.dp, BorderSteel),
                ) {
                    Icon(Icons.Filled.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = stringResource(id = R.string.btn_inspect))
                }

                Button(
                    onClick = onInteract,
                    modifier = Modifier.height(58.dp).testTag("btn_interact"),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (nearbyTarget != null) AntiqueGold else CharcoalSlate.copy(alpha = 0.9f),
                        contentColor = if (nearbyTarget != null) MidnightVoid else MutedPewter,
                    ),
                    border = BorderStroke(1.5.dp, if (nearbyTarget != null) PaleGold else BorderSteel),
                ) {
                    Icon(Icons.Filled.Key, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = R.string.btn_interact),
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun VirtualJoystick(
    onMove: (Float, Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    var knobOffset by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = modifier
            .size(126.dp)
            .pointerInput(Unit) {
                val maxRadius = size.width * 0.36f
                val center = Offset(size.width * 0.5f, size.height * 0.5f)
                detectDragGestures(
                    onDragStart = { startPos ->
                        val delta = startPos - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y).coerceAtLeast(0.001f)
                        val clamped = if (dist > maxRadius) delta * (maxRadius / dist) else delta
                        knobOffset = clamped
                        onMove(clamped.x / maxRadius, clamped.y / maxRadius)
                    },
                    onDragEnd = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDragCancel = {
                        knobOffset = Offset.Zero
                        onMove(0f, 0f)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val delta = change.position - center
                        val dist = sqrt(delta.x * delta.x + delta.y * delta.y).coerceAtLeast(0.001f)
                        val clamped = if (dist > maxRadius) delta * (maxRadius / dist) else delta
                        knobOffset = clamped
                        onMove(clamped.x / maxRadius, clamped.y / maxRadius)
                    },
                )
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val c = Offset(size.width * 0.5f, size.height * 0.5f)
            val outerR = size.minDimension * 0.44f
            drawCircle(
                color = CharcoalSlate.copy(alpha = 0.65f),
                radius = outerR,
                center = c,
            )
            drawCircle(
                color = AntiqueGold.copy(alpha = 0.5f),
                radius = outerR,
                center = c,
                style = Stroke(width = 2.dp.toPx()),
            )
            // Subtle compass crosshairs
            drawLine(BorderSteel.copy(alpha = 0.6f), Offset(c.x - outerR * 0.65f, c.y), Offset(c.x + outerR * 0.65f, c.y), 1.5f)
            drawLine(BorderSteel.copy(alpha = 0.6f), Offset(c.x, c.y - outerR * 0.65f), Offset(c.x, c.y + outerR * 0.65f), 1.5f)

            val knobCenter = c + knobOffset
            drawCircle(
                color = AntiqueGold.copy(alpha = 0.92f),
                radius = size.minDimension * 0.19f,
                center = knobCenter,
            )
            drawCircle(
                color = MidnightVoid,
                radius = size.minDimension * 0.08f,
                center = knobCenter,
            )
        }
    }
}

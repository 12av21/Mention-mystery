package com.example.ui.modals

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.model.ClueId
import com.example.model.PlayerState
import com.example.model.RoomId
import com.example.model.TileType
import com.example.mystery.MysteryCatalog
import com.example.ui.theme.AntiqueGold
import com.example.ui.theme.BorderSteel
import com.example.ui.theme.CharcoalSlate
import com.example.ui.theme.EmeraldPatina
import com.example.ui.theme.MidnightVoid
import com.example.ui.theme.MutedPewter
import com.example.ui.theme.PaleGold
import com.example.ui.theme.ParchmentIvory
import com.example.ui.theme.VelvetIndigo
import com.example.world.MansionWorldDefinition

@Composable
fun MansionMapModal(
    world: MansionWorldDefinition,
    player: PlayerState,
    unlockedDoorIds: Set<String>,
    collectedClues: Set<ClueId>,
    onClose: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MidnightVoid.copy(alpha = 0.85f))
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            colors = CardDefaults.cardColors(containerColor = CharcoalSlate),
            border = BorderStroke(1.5.dp, AntiqueGold),
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "RAVENSHALLOW MANOR BLUEPRINT",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PaleGold,
                    )
                    IconButton(onClick = onClose, modifier = Modifier.testTag("btn_map_close")) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(id = R.string.btn_close), tint = ParchmentIvory)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    // Architectural Canvas Map
                    Box(
                        modifier = Modifier
                            .weight(1.35f)
                            .fillMaxHeight()
                            .background(MidnightVoid, RoundedCornerShape(10.dp))
                            .border(1.dp, BorderSteel, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val cellW = size.width / world.width
                            val cellH = size.height / world.height
                            val cell = minOf(cellW, cellH)
                            val ox = (size.width - cell * world.width) * 0.5f
                            val oy = (size.height - cell * world.height) * 0.5f

                            for (y in 0 until world.height) {
                                for (x in 0 until world.width) {
                                    val tile = world.tileAt(x, y)
                                    val color = when (tile) {
                                        TileType.WALL -> Color(0xFF28354F)
                                        TileType.DOOR_LOCKED -> Color(0xFFF59E0B)
                                        TileType.SECRET_DOOR -> Color(0xFFD4AF37)
                                        TileType.DOOR_UNLOCKED -> Color(0xFF10B981)
                                        else -> Color(0xFF131B2E)
                                    }
                                    val door = world.doors.firstOrNull { it.tileX == x && it.tileY == y }
                                    val finalColor = if (door != null && unlockedDoorIds.contains(door.id)) {
                                        Color(0xFF10B981)
                                    } else color
                                    drawRect(
                                        color = finalColor,
                                        topLeft = Offset(ox + x * cell, oy + y * cell),
                                        size = Size(cell + 0.5f, cell + 0.5f),
                                    )
                                }
                            }

                            // Room Borders
                            world.rooms.forEach { zone ->
                                val isHere = zone.id == player.currentRoom
                                drawRect(
                                    color = if (isHere) AntiqueGold else Color(0x4494A3B8),
                                    topLeft = Offset(ox + zone.minX * cell, oy + zone.minY * cell),
                                    size = Size((zone.maxX - zone.minX + 1) * cell, (zone.maxY - zone.minY + 1) * cell),
                                    style = Stroke(width = if (isHere) 2.5f else 1f),
                                )
                            }

                            // Player dot
                            val px = ox + player.position.x * cell
                            val py = oy + player.position.y * cell
                            drawCircle(Color(0xFF38BDF8), cell * 0.55f, Offset(px, py))
                            drawCircle(Color(0xFFFFFFFF), cell * 0.25f, Offset(px, py))
                        }
                    }

                    // Wing Progression Legend
                    LazyColumn(
                        modifier = Modifier.weight(0.75f).fillMaxHeight(),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        items(RoomId.entries) { room ->
                            val door = world.doors.firstOrNull { it.targetRoom == room }
                            val clue = MysteryCatalog.clues.firstOrNull { it.roomId == room }
                            val unlocked = door == null || unlockedDoorIds.contains(door.id)
                            val clueDone = clue != null && collectedClues.contains(clue.id)
                            val statusText = when {
                                room == RoomId.SECRET_ROOM && unlocked -> "Secret Vault Open"
                                room == RoomId.SECRET_ROOM -> "Requires 5 Clues"
                                clueDone -> "Clue Secured"
                                unlocked && clue != null -> "Unlocked • Search Clue"
                                door != null -> "Locked Puzzle"
                                else -> "Open Hall"
                            }
                            val statusColor = when {
                                clueDone -> EmeraldPatina
                                unlocked -> PaleGold
                                else -> MutedPewter
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        if (player.currentRoom == room) VelvetIndigo else MidnightVoid.copy(alpha = 0.65f),
                                        RoundedCornerShape(8.dp),
                                    )
                                    .border(
                                        1.dp,
                                        if (player.currentRoom == room) AntiqueGold else BorderSteel,
                                        RoundedCornerShape(8.dp),
                                    )
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                            ) {
                                Text(room.displayName, style = MaterialTheme.typography.labelLarge, color = ParchmentIvory)
                                Text(statusText, style = MaterialTheme.typography.labelSmall, color = statusColor)
                            }
                        }
                    }
                }
            }
        }
    }
}

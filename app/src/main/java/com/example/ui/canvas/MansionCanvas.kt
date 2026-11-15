package com.example.ui.canvas

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.model.ClueId
import com.example.model.DoorObject
import com.example.model.GuardFsmState
import com.example.model.GuardRuntimeState
import com.example.model.InteractTarget
import com.example.model.PlayerState
import com.example.model.PropObject
import com.example.model.PropType
import com.example.model.TileType
import com.example.world.MansionWorldDefinition
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun MansionCanvas(
    world: MansionWorldDefinition,
    player: PlayerState,
    guards: List<GuardRuntimeState>,
    unlockedDoorIds: Set<String>,
    collectedClues: Set<ClueId>,
    nearbyTarget: InteractTarget?,
    highlightPulse: Float,
    timeSeconds: Float,
    modifier: Modifier = Modifier,
) {
    var camX by remember { mutableFloatStateOf(player.position.x) }
    var camY by remember { mutableFloatStateOf(player.position.y) }

    Canvas(modifier = modifier.fillMaxSize()) {
        // Smooth camera interpolation toward detective
        camX += (player.position.x - camX) * 0.16f
        camY += (player.position.y - camY) * 0.16f

        val tileSize = (size.minDimension / 10.5f).coerceIn(38.dp.toPx(), 64.dp.toPx())
        val originX = size.width * 0.5f - camX * tileSize
        val originY = size.height * 0.5f - camY * tileSize

        drawRect(Color(0xFF060911))

        val minTx = ((-originX) / tileSize).toInt().coerceIn(0, world.width - 1)
        val maxTx = ((size.width - originX) / tileSize).toInt().coerceIn(0, world.width - 1)
        val minTy = ((-originY) / tileSize).toInt().coerceIn(0, world.height - 1)
        val maxTy = ((size.height - originY) / tileSize).toInt().coerceIn(0, world.height - 1)

        // 1. Tiles
        for (ty in minTy..maxTy) {
            for (tx in minTx..maxTx) {
                val tile = world.tileAt(tx, ty)
                val sx = originX + tx * tileSize
                val sy = originY + ty * tileSize
                drawTile(tile, tx, ty, sx, sy, tileSize, world.doors, unlockedDoorIds)
            }
        }

        // 2. Warm flickering sconce glows on floors
        world.sconces.forEachIndexed { idx, sconce ->
            val sx = originX + sconce.x * tileSize
            val sy = originY + sconce.y * tileSize
            if (sx in -tileSize * 4..size.width + tileSize * 4 && sy in -tileSize * 4..size.height + tileSize * 4) {
                val flicker = 0.88f + 0.12f * sin(timeSeconds * 5.2f + idx * 1.7f)
                val rad = tileSize * 2.5f * flicker
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0x3CF59E0B), Color(0x14D4AF37), Color.Transparent),
                        center = Offset(sx, sy),
                        radius = rad,
                    ),
                    radius = rad,
                    center = Offset(sx, sy),
                )
            }
        }

        // 3. Props & Hidden Clues
        for (prop in world.props) {
            if (prop.clueRewardId != null && collectedClues.contains(prop.clueRewardId)) continue
            val px = originX + prop.tileX * tileSize
            val py = originY + prop.tileY * tileSize
            if (px in -tileSize * 4..size.width + tileSize * 4 && py in -tileSize * 4..size.height + tileSize * 4) {
                drawProp(prop, px, py, tileSize, highlightPulse, timeSeconds)
            }
        }

        // 4. Raycast Guard Flashlight Cones
        for (guard in guards) {
            drawFlashlightCone(guard, originX, originY, tileSize)
        }

        // 5. Interact Target Highlight Ring
        nearbyTarget?.let { target ->
            val tx = originX + target.worldPos.x * tileSize
            val ty = originY + target.worldPos.y * tileSize
            val ringRadius = tileSize * (0.58f + 0.08f * sin(timeSeconds * 5f))
            drawCircle(
                color = Color(0xFFD4AF37),
                radius = ringRadius,
                center = Offset(tx, ty),
                style = Stroke(width = 2.5.dp.toPx()),
            )
        }

        // 6. Guards
        for (guard in guards) {
            drawGuard(guard, originX, originY, tileSize)
        }

        // 7. Player Detective
        drawDetective(player, originX, originY, tileSize)

        // 8. Atmospheric ambient darkness vignette around edges
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0x5504070E), Color(0xAA04070E)),
                center = Offset(size.width * 0.5f, size.height * 0.5f),
                radius = size.maxDimension * 0.65f,
            ),
        )
    }
}

private fun DrawScope.drawTile(
    tile: TileType,
    tx: Int,
    ty: Int,
    sx: Float,
    sy: Float,
    tileSize: Float,
    doors: List<DoorObject>,
    unlockedDoorIds: Set<String>,
) {
    when (tile) {
        TileType.VOID -> Unit
        TileType.WALL -> {
            drawRect(Color(0xFF151C2C), Offset(sx, sy), Size(tileSize + 0.5f, tileSize + 0.5f))
            drawRect(Color(0xFF202B42), Offset(sx, sy), Size(tileSize, tileSize * 0.22f))
            drawRect(
                color = Color(0xFF0A0E17),
                topLeft = Offset(sx, sy),
                size = Size(tileSize, tileSize),
                style = Stroke(width = 1f),
            )
        }
        TileType.FLOOR_WOOD -> {
            val base = if ((tx + ty) % 2 == 0) Color(0xFF2B1E17) else Color(0xFF261A14)
            drawRect(base, Offset(sx, sy), Size(tileSize + 0.5f, tileSize + 0.5f))
            drawLine(
                Color(0xFF1B120D),
                Offset(sx, sy + tileSize * 0.5f),
                Offset(sx + tileSize, sy + tileSize * 0.5f),
                strokeWidth = 1f,
            )
        }
        TileType.FLOOR_CARPET -> {
            drawRect(Color(0xFF471622), Offset(sx, sy), Size(tileSize + 0.5f, tileSize + 0.5f))
            drawRect(
                Color(0x33D4AF37),
                Offset(sx + 2f, sy + 2f),
                Size(tileSize - 4f, tileSize - 4f),
                style = Stroke(width = 1f),
            )
        }
        TileType.FLOOR_STONE -> {
            val stone = if ((tx + ty) % 2 == 0) Color(0xFF252D3D) else Color(0xFF202736)
            drawRect(stone, Offset(sx, sy), Size(tileSize + 0.5f, tileSize + 0.5f))
            drawRect(Color(0xFF161B26), Offset(sx, sy), Size(tileSize, tileSize), style = Stroke(1f))
        }
        TileType.FLOOR_GARDEN -> {
            val moss = if ((tx * 3 + ty) % 3 == 0) Color(0xFF172C26) else Color(0xFF132520)
            drawRect(moss, Offset(sx, sy), Size(tileSize + 0.5f, tileSize + 0.5f))
            drawCircle(Color(0x2234D399), tileSize * 0.12f, Offset(sx + tileSize * 0.3f, sy + tileSize * 0.7f))
        }
        TileType.FLOOR_MARBLE -> {
            val marble = if ((tx + ty) % 2 == 0) Color(0xFF2B3245) else Color(0xFF1E2434)
            drawRect(marble, Offset(sx, sy), Size(tileSize + 0.5f, tileSize + 0.5f))
        }
        TileType.DOOR_LOCKED, TileType.DOOR_UNLOCKED, TileType.SECRET_DOOR -> {
            val door = doors.firstOrNull { it.tileX == tx && it.tileY == ty }
            val unlocked = door != null && unlockedDoorIds.contains(door.id)
            drawRect(Color(0xFF251A14), Offset(sx, sy), Size(tileSize + 0.5f, tileSize + 0.5f))
            if (unlocked) {
                // Swung open door leaf against jamb
                drawRect(Color(0xFF8C6239), Offset(sx + 2f, sy + 2f), Size(tileSize * 0.18f, tileSize * 0.9f))
                drawCircle(Color(0x6610B981), tileSize * 0.18f, Offset(sx + tileSize * 0.5f, sy + tileSize * 0.5f))
            } else {
                val panelColor = if (tile == TileType.SECRET_DOOR) Color(0xFF3E2F12) else Color(0xFF5A3921)
                val accent = if (tile == TileType.SECRET_DOOR) Color(0xFFF3DFA2) else Color(0xFFD4AF37)
                drawRoundRect(
                    color = panelColor,
                    topLeft = Offset(sx + 3f, sy + 4f),
                    size = Size(tileSize - 6f, tileSize - 8f),
                    cornerRadius = CornerRadius(4f, 4f),
                )
                drawCircle(accent, tileSize * 0.12f, Offset(sx + tileSize * 0.5f, sy + tileSize * 0.5f))
            }
        }
    }
}

private fun DrawScope.drawProp(
    prop: PropObject,
    cx: Float,
    cy: Float,
    tileSize: Float,
    highlightPulse: Float,
    timeSeconds: Float,
) {
    val w = prop.width * tileSize
    val h = prop.height * tileSize
    val tl = Offset(cx - w * 0.5f, cy - h * 0.5f)

    when (prop.type) {
        PropType.BOOKSHELF -> {
            drawRoundRect(Color(0xFF432818), tl, Size(w, h), CornerRadius(5f, 5f))
            drawRoundRect(Color(0xFF6F4E37), Offset(tl.x + 3f, tl.y + 3f), Size(w - 6f, h * 0.45f), CornerRadius(3f, 3f))
        }
        PropType.DESK, PropType.TABLE -> {
            drawRoundRect(Color(0xFF58311B), tl, Size(w, h), CornerRadius(6f, 6f))
            drawRoundRect(Color(0xFF8B5A2B), tl, Size(w, h), CornerRadius(6f, 6f), style = Stroke(2f))
        }
        PropType.PIANO -> {
            drawRoundRect(Color(0xFF14161F), tl, Size(w, h), CornerRadius(8f, 8f))
            drawRect(Color(0xFFF0E6D2), Offset(tl.x + 6f, tl.y + h - 8f), Size(w - 12f, 5f))
        }
        PropType.PLANT -> {
            drawCircle(Color(0xFF164E3F), w * 0.48f, Offset(cx, cy))
            drawCircle(Color(0xFF34D399), w * 0.25f, Offset(cx - w * 0.1f, cy - h * 0.1f))
            drawCircle(Color(0xFFA78BFA), w * 0.12f, Offset(cx + w * 0.15f, cy + h * 0.08f))
        }
        PropType.PAINTING -> {
            drawRect(Color(0xFFD4AF37), tl, Size(w, h))
            drawRect(Color(0xFF2B213A), Offset(tl.x + 4f, tl.y + 4f), Size(w - 8f, h - 8f))
        }
        PropType.STOVE -> {
            drawRoundRect(Color(0xFF262B38), tl, Size(w, h), CornerRadius(6f, 6f))
            drawCircle(Color(0xFFEA580C), w * 0.16f, Offset(cx - w * 0.2f, cy))
            drawCircle(Color(0xFFF59E0B), w * 0.12f, Offset(cx + w * 0.2f, cy))
        }
        PropType.CANDELABRA -> {
            drawCircle(Color(0xFFD4AF37), w * 0.28f, Offset(cx, cy))
            drawCircle(Color(0xFFFDE68A), w * 0.14f, Offset(cx, cy))
        }
        PropType.STATUE -> {
            drawCircle(Color(0xFF64748B), w * 0.36f, Offset(cx, cy))
            drawCircle(Color(0xFFCBD5E1), w * 0.22f, Offset(cx, cy - h * 0.05f))
        }
        PropType.SAFE_CHECKPOINT -> {
            val pulse = 0.75f + 0.25f * sin(timeSeconds * 3f)
            drawCircle(Color(0x3310B981), w * 0.65f * pulse, Offset(cx, cy))
            drawCircle(Color(0xFF10B981), w * 0.24f, Offset(cx, cy))
        }
        PropType.CLUE_SPOT -> {
            val extra = if (highlightPulse > 0f) 1.45f else 1.0f
            val pulse = (0.82f + 0.18f * sin(timeSeconds * 4.5f)) * extra
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0x99F3DFA2), Color(0x44D4AF37), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = tileSize * 0.7f * pulse,
                ),
                radius = tileSize * 0.7f * pulse,
                center = Offset(cx, cy),
            )
            drawCircle(Color(0xFFD4AF37), tileSize * 0.22f, Offset(cx, cy))
            drawCircle(Color(0xFFFFFBEB), tileSize * 0.10f, Offset(cx, cy))
        }
        PropType.EVIDENCE_BOARD_WALL -> {
            drawRoundRect(Color(0xFF3F2316), tl, Size(w, h), CornerRadius(4f, 4f))
            drawRect(Color(0xFFF0E6D2), Offset(tl.x + 6f, tl.y + 6f), Size(w * 0.28f, h * 0.45f))
            drawRect(Color(0xFFF0E6D2), Offset(tl.x + w * 0.55f, tl.y + 8f), Size(w * 0.3f, h * 0.45f))
            drawLine(Color(0xFFEF4444), Offset(tl.x + w * 0.2f, tl.y + h * 0.3f), Offset(tl.x + w * 0.7f, tl.y + h * 0.3f), 2f)
        }
        PropType.HEIRLOOM_PEDESTAL -> {
            drawCircle(Color(0x55D4AF37), w * 0.65f, Offset(cx, cy))
            drawRoundRect(Color(0xFF1F2940), tl, Size(w, h), CornerRadius(8f, 8f))
            drawCircle(Color(0xFFD4AF37), w * 0.26f, Offset(cx, cy))
            drawCircle(Color(0xFFEF4444), w * 0.11f, Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawFlashlightCone(
    guard: GuardRuntimeState,
    originX: Float,
    originY: Float,
    tileSize: Float,
) {
    val poly = guard.flashlightPolygon
    if (poly.size < 3) return
    val path = Path().apply {
        val first = poly.first()
        moveTo(originX + first.x * tileSize, originY + first.y * tileSize)
        for (i in 1 until poly.size) {
            val pt = poly[i]
            lineTo(originX + pt.x * tileSize, originY + pt.y * tileSize)
        }
        close()
    }
    val gx = originX + guard.position.x * tileSize
    val gy = originY + guard.position.y * tileSize
    val innerColor = when (guard.fsmState) {
        GuardFsmState.CHASE -> Color(0x88EF4444)
        GuardFsmState.SUSPICIOUS -> Color(0x88F59E0B)
        else -> Color(0x66FDE68A)
    }
    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(innerColor, Color(0x18F59E0B), Color.Transparent),
            center = Offset(gx, gy),
            radius = tileSize * 6.5f,
        ),
    )
}

private fun DrawScope.drawGuard(
    guard: GuardRuntimeState,
    originX: Float,
    originY: Float,
    tileSize: Float,
) {
    val cx = originX + guard.position.x * tileSize
    val cy = originY + guard.position.y * tileSize
    val deg = (guard.facingRad * 180f / PI.toFloat())
    val stride = sin(guard.walkPhase) * tileSize * 0.08f

    rotate(degrees = deg, pivot = Offset(cx, cy)) {
        // Coat shoulders
        drawRoundRect(
            color = Color(0xFF2B354B),
            topLeft = Offset(cx - tileSize * 0.22f, cy - tileSize * 0.28f),
            size = Size(tileSize * 0.44f, tileSize * 0.56f),
            cornerRadius = CornerRadius(tileSize * 0.14f, tileSize * 0.14f),
        )
        // Animated footsteps
        drawCircle(Color(0xFF111827), tileSize * 0.08f, Offset(cx + stride, cy - tileSize * 0.2f))
        drawCircle(Color(0xFF111827), tileSize * 0.08f, Offset(cx - stride, cy + tileSize * 0.2f))
        // Guard cap + brass badge
        drawCircle(Color(0xFF1E293B), tileSize * 0.18f, Offset(cx, cy))
        drawCircle(Color(0xFFD4AF37), tileSize * 0.06f, Offset(cx + tileSize * 0.1f, cy))
        // Flashlight barrel
        drawRect(Color(0xFF94A3B8), Offset(cx + tileSize * 0.16f, cy + tileSize * 0.12f), Size(tileSize * 0.18f, tileSize * 0.08f))
    }

    // Suspicion ring above guard when alert
    if (guard.suspicion > 0.05f) {
        val barW = tileSize * 0.6f
        val barH = 4.dp.toPx()
        val bx = cx - barW * 0.5f
        val by = cy - tileSize * 0.48f
        drawRect(Color(0xAA0B101D), Offset(bx, by), Size(barW, barH))
        val fillColor = if (guard.fsmState == GuardFsmState.CHASE) Color(0xFFEF4444) else Color(0xFFF59E0B)
        drawRect(fillColor, Offset(bx, by), Size(barW * guard.suspicion.coerceIn(0f, 1f), barH))
    }
}

private fun DrawScope.drawDetective(
    player: PlayerState,
    originX: Float,
    originY: Float,
    tileSize: Float,
) {
    val cx = originX + player.position.x * tileSize
    val cy = originY + player.position.y * tileSize
    val deg = (player.facingRad * 180f / PI.toFloat())
    val stride = if (player.isMoving) sin(player.walkPhase) * tileSize * 0.09f else 0f

    // Soft detective lantern aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0x44F3DFA2), Color(0x16D4AF37), Color.Transparent),
            center = Offset(cx, cy),
            radius = tileSize * 1.9f,
        ),
        radius = tileSize * 1.9f,
        center = Offset(cx, cy),
    )

    rotate(degrees = deg, pivot = Offset(cx, cy)) {
        // Striding boots
        drawCircle(Color(0xFF0F172A), tileSize * 0.08f, Offset(cx + stride, cy - tileSize * 0.19f))
        drawCircle(Color(0xFF0F172A), tileSize * 0.08f, Offset(cx - stride, cy + tileSize * 0.19f))
        // Victorian trench coat
        drawRoundRect(
            color = Color(0xFF334155),
            topLeft = Offset(cx - tileSize * 0.22f, cy - tileSize * 0.27f),
            size = Size(tileSize * 0.44f, tileSize * 0.54f),
            cornerRadius = CornerRadius(tileSize * 0.14f, tileSize * 0.14f),
        )
        // Detective hat & gold trim
        drawCircle(Color(0xFF1E293B), tileSize * 0.19f, Offset(cx, cy))
        drawCircle(
            color = Color(0xFFD4AF37),
            radius = tileSize * 0.18f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.6.dp.toPx()),
        )
        // Held brass lantern
        drawCircle(Color(0xFFFDE68A), tileSize * 0.09f, Offset(cx + tileSize * 0.24f, cy - tileSize * 0.14f))
    }
}

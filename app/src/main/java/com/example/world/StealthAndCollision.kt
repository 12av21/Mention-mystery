package com.example.world

import com.example.model.Difficulty
import com.example.model.DoorObject
import com.example.model.GuardFsmState
import com.example.model.GuardRuntimeState
import com.example.model.PropObject
import com.example.model.TileType
import com.example.model.Vec2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

object StealthAndCollision {

    const val PLAYER_RADIUS = 0.28f
    private const val RAY_STEP = 0.14f
    private const val CONE_RAYS = 26

    fun isTileWalkable(
        tileX: Int,
        tileY: Int,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
    ): Boolean {
        val tile = world.tileAt(tileX, tileY)
        if (tile.walkable) return true
        if (tile == TileType.DOOR_LOCKED || tile == TileType.SECRET_DOOR) {
            val door = world.doors.firstOrNull { it.tileX == tileX && it.tileY == tileY }
            return door != null && unlockedDoorIds.contains(door.id)
        }
        return false
    }

    fun isTileBlockingLight(
        tileX: Int,
        tileY: Int,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
    ): Boolean {
        val tile = world.tileAt(tileX, tileY)
        if (!tile.blocksLight) return false
        if (tile == TileType.DOOR_LOCKED || tile == TileType.SECRET_DOOR) {
            val door = world.doors.firstOrNull { it.tileX == tileX && it.tileY == tileY }
            return door == null || !unlockedDoorIds.contains(door.id)
        }
        return true
    }

    fun canOccupy(
        pos: Vec2,
        radius: Float,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
        props: List<PropObject>,
    ): Boolean {
        val minTx = (pos.x - radius).toInt()
        val maxTx = (pos.x + radius).toInt()
        val minTy = (pos.y - radius).toInt()
        val maxTy = (pos.y + radius).toInt()

        for (ty in minTy..maxTy) {
            for (tx in minTx..maxTx) {
                if (!isTileWalkable(tx, ty, world, unlockedDoorIds)) {
                    val closestX = pos.x.coerceIn(tx.toFloat(), tx + 1f)
                    val closestY = pos.y.coerceIn(ty.toFloat(), ty + 1f)
                    val dx = pos.x - closestX
                    val dy = pos.y - closestY
                    if (dx * dx + dy * dy < radius * radius) {
                        return false
                    }
                }
            }
        }

        for (prop in props) {
            if (!prop.blocksMovement) continue
            val halfW = prop.width * 0.5f
            val halfH = prop.height * 0.5f
            val left = prop.tileX - halfW
            val right = prop.tileX + halfW
            val top = prop.tileY - halfH
            val bottom = prop.tileY + halfH
            val closestX = pos.x.coerceIn(left, right)
            val closestY = pos.y.coerceIn(top, bottom)
            val dx = pos.x - closestX
            val dy = pos.y - closestY
            if (dx * dx + dy * dy < radius * radius) {
                return false
            }
        }
        return true
    }

    fun resolveMovement(
        current: Vec2,
        deltaX: Float,
        deltaY: Float,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
        props: List<PropObject>,
    ): Vec2 {
        val tryBoth = Vec2(current.x + deltaX, current.y + deltaY)
        if (canOccupy(tryBoth, PLAYER_RADIUS, world, unlockedDoorIds, props)) {
            return tryBoth
        }
        val tryX = Vec2(current.x + deltaX, current.y)
        val xOk = canOccupy(tryX, PLAYER_RADIUS, world, unlockedDoorIds, props)
        val tryY = Vec2(current.x, current.y + deltaY)
        val yOk = canOccupy(tryY, PLAYER_RADIUS, world, unlockedDoorIds, props)

        return when {
            xOk && !yOk -> tryX
            yOk && !xOk -> tryY
            xOk && yOk -> if (abs(deltaX) >= abs(deltaY)) tryX else tryY
            else -> current
        }
    }

    fun castRay(
        origin: Vec2,
        angleRad: Float,
        maxDist: Float,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
    ): Vec2 {
        val cosA = cos(angleRad)
        val sinA = sin(angleRad)
        var d = 0f
        while (d < maxDist) {
            d = min(d + RAY_STEP, maxDist)
            val px = origin.x + cosA * d
            val py = origin.y + sinA * d
            if (isTileBlockingLight(px.toInt(), py.toInt(), world, unlockedDoorIds)) {
                return Vec2(px, py)
            }
        }
        return Vec2(origin.x + cosA * maxDist, origin.y + sinA * maxDist)
    }

    fun computeFlashlightPolygon(
        origin: Vec2,
        facingRad: Float,
        halfAngleDeg: Float,
        range: Float,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
    ): List<Vec2> {
        val halfRad = Math.toRadians(halfAngleDeg.toDouble()).toFloat()
        val points = ArrayList<Vec2>(CONE_RAYS + 2)
        points.add(origin)
        for (i in 0..CONE_RAYS) {
            val t = i.toFloat() / CONE_RAYS.toFloat()
            val angle = facingRad - halfRad + t * (2f * halfRad)
            points.add(castRay(origin, angle, range, world, unlockedDoorIds))
        }
        return points
    }

    fun hasLineOfSight(
        from: Vec2,
        to: Vec2,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
    ): Boolean {
        val dx = to.x - from.x
        val dy = to.y - from.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist < 0.05f) return true
        val steps = (dist / RAY_STEP).toInt().coerceAtLeast(1)
        for (i in 1 until steps) {
            val t = i.toFloat() / steps.toFloat()
            val px = from.x + dx * t
            val py = from.y + dy * t
            if (isTileBlockingLight(px.toInt(), py.toInt(), world, unlockedDoorIds)) {
                return false
            }
        }
        return true
    }

    fun isPlayerInFlashlight(
        guard: GuardRuntimeState,
        playerPos: Vec2,
        difficulty: Difficulty,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
    ): Boolean {
        val dx = playerPos.x - guard.position.x
        val dy = playerPos.y - guard.position.y
        val dist = sqrt(dx * dx + dy * dy)
        if (dist > difficulty.visionRange) return false
        if (dist < 0.65f) return true // Body bump awareness

        val angleToPlayer = atan2(dy, dx)
        val deltaAngle = abs(normalizeAngle(angleToPlayer - guard.facingRad))
        val halfFovRad = Math.toRadians(difficulty.visionHalfAngleDeg.toDouble()).toFloat()
        if (deltaAngle > halfFovRad) return false

        return hasLineOfSight(guard.position, playerPos, world, unlockedDoorIds)
    }

    data class GuardTickResult(
        val updatedGuard: GuardRuntimeState,
        val caughtPlayer: Boolean,
    )

    fun updateGuardFsm(
        guard: GuardRuntimeState,
        waypoints: List<Vec2>,
        playerPos: Vec2,
        dt: Float,
        difficulty: Difficulty,
        world: MansionWorldDefinition,
        unlockedDoorIds: Set<String>,
    ): GuardTickResult {
        val seesPlayer = isPlayerInFlashlight(guard, playerPos, difficulty, world, unlockedDoorIds)
        val distToPlayer = guard.position.distanceTo(playerPos)

        if (seesPlayer && distToPlayer < 0.85f) {
            val cone = computeFlashlightPolygon(
                guard.position,
                guard.facingRad,
                difficulty.visionHalfAngleDeg,
                difficulty.visionRange,
                world,
                unlockedDoorIds,
            )
            return GuardTickResult(
                guard.copy(
                    fsmState = GuardFsmState.CHASE,
                    suspicion = 1f,
                    investigateTarget = playerPos,
                    flashlightPolygon = cone,
                ),
                caughtPlayer = true,
            )
        }

        var nextState = guard.fsmState
        var suspicion = guard.suspicion
        var timer = guard.stateTimer
        var investigateTarget = guard.investigateTarget
        var wpIdx = guard.waypointIndex
        var pos = guard.position
        var facing = guard.facingRad
        var walkPhase = guard.walkPhase

        if (seesPlayer) {
            val closenessBoost = (1.35f - (distToPlayer / difficulty.visionRange)).coerceIn(0.6f, 2.0f)
            suspicion = (suspicion + dt * difficulty.suspicionRate * closenessBoost).coerceIn(0f, 1f)
            investigateTarget = playerPos
            nextState = if (suspicion >= 0.65f) GuardFsmState.CHASE else GuardFsmState.SUSPICIOUS
            timer = 2.6f
        } else {
            suspicion = (suspicion - dt * 0.45f).coerceIn(0f, 1f)
            when (guard.fsmState) {
                GuardFsmState.IDLE -> {
                    timer -= dt
                    if (timer <= 0f) {
                        nextState = GuardFsmState.PATROL
                    }
                }
                GuardFsmState.PATROL -> {
                    // Stay patrolling
                }
                GuardFsmState.SUSPICIOUS, GuardFsmState.CHASE -> {
                    nextState = GuardFsmState.INVESTIGATE
                    timer = 2.5f
                }
                GuardFsmState.INVESTIGATE -> {
                    timer -= dt
                    if (timer <= 0f) {
                        nextState = GuardFsmState.RETURN_TO_PATROL
                        investigateTarget = null
                    }
                }
                GuardFsmState.RETURN_TO_PATROL -> {
                    val targetWp = waypoints[wpIdx]
                    if (pos.distanceTo(targetWp) < 0.35f) {
                        nextState = GuardFsmState.PATROL
                    }
                }
            }
        }

        // Steering toward active target
        val moveTarget: Vec2? = when (nextState) {
            GuardFsmState.IDLE -> null
            GuardFsmState.PATROL, GuardFsmState.RETURN_TO_PATROL -> waypoints[wpIdx]
            GuardFsmState.SUSPICIOUS, GuardFsmState.CHASE, GuardFsmState.INVESTIGATE -> investigateTarget
        }

        val speedMultiplier = when (nextState) {
            GuardFsmState.CHASE -> 1.38f
            GuardFsmState.SUSPICIOUS -> 0.55f
            GuardFsmState.INVESTIGATE -> 0.85f
            GuardFsmState.RETURN_TO_PATROL -> 0.95f
            else -> 1.0f
        }

        if (moveTarget != null) {
            val dx = moveTarget.x - pos.x
            val dy = moveTarget.y - pos.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist > 0.16f) {
                val targetAngle = atan2(dy, dx)
                facing = lerpAngle(facing, targetAngle, (dt * 7.5f).coerceIn(0f, 1f))
                val step = difficulty.guardSpeed * speedMultiplier * dt
                val nx = (dx / dist) * min(step, dist)
                val ny = (dy / dist) * min(step, dist)
                val moved = resolveMovement(pos, nx, ny, world, unlockedDoorIds, emptyList())
                if (moved.distanceTo(pos) > 0.001f) {
                    walkPhase += dt * 7f
                    pos = moved
                } else if (nextState == GuardFsmState.INVESTIGATE) {
                    nextState = GuardFsmState.RETURN_TO_PATROL
                }
            } else if (nextState == GuardFsmState.PATROL) {
                wpIdx = (wpIdx + 1) % waypoints.size
                nextState = GuardFsmState.IDLE
                timer = 0.85f
            } else if (nextState == GuardFsmState.RETURN_TO_PATROL) {
                nextState = GuardFsmState.PATROL
            } else if (nextState == GuardFsmState.INVESTIGATE) {
                facing += dt * 1.4f
            }
        }

        val caught = nextState == GuardFsmState.CHASE && pos.distanceTo(playerPos) < 0.92f
        val cone = computeFlashlightPolygon(
            pos,
            facing,
            difficulty.visionHalfAngleDeg,
            difficulty.visionRange,
            world,
            unlockedDoorIds,
        )

        return GuardTickResult(
            updatedGuard = guard.copy(
                position = pos,
                facingRad = facing,
                fsmState = nextState,
                waypointIndex = wpIdx,
                stateTimer = timer,
                suspicion = suspicion,
                investigateTarget = investigateTarget,
                flashlightPolygon = cone,
                walkPhase = walkPhase,
            ),
            caughtPlayer = caught,
        )
    }

    fun normalizeAngle(rad: Float): Float {
        var a = rad
        while (a > PI) a -= (2.0 * PI).toFloat()
        while (a < -PI) a += (2.0 * PI).toFloat()
        return a
    }

    fun lerpAngle(from: Float, to: Float, t: Float): Float {
        val diff = normalizeAngle(to - from)
        return normalizeAngle(from + diff * t)
    }

    fun doorCenter(door: DoorObject): Vec2 = Vec2(door.tileX + 0.5f, door.tileY + 0.5f)
}

package com.example.diwali.ui.burst

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerLevelConfig
import com.example.diwali.model.CrackerType
import com.example.diwali.model.SutliBombLevelConfig
import com.example.diwali.model.SutliSoundProfile
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldYellow
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

enum class BurstStage {
    READY,          // Cracker is on pedestal, waiting for lighter
    LIGHTING,       // Lighter touches fuse
    FUSE_BURNING,   // Fuse is hissing and burning down
    BURSTING,       // Cracker is bursting / active simulation
    COMPLETED       // Burst finished, ready for Well Done dialog
}

private enum class ParticleType {
    SPARK_TRAIL,
    FIREBALL,
    SMOKE_PUFF,
    DEBRIS,
    SHOCKWAVE,
    CONFETTI,
    ASH_SEGMENT
}

private data class RealisticParticle(
    var type: ParticleType,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var coreColor: Color = Color.White,
    var radius: Float,
    var maxRadius: Float = radius,
    var alpha: Float = 1f,
    var decay: Float = 0.02f,
    var gravity: Float = 0f,
    var drag: Float = 0.98f,
    var trailLength: Float = 2.5f,
    var age: Float = 0f,
    var rotation: Float = 0f,
    var rotationSpeed: Float = 0f
)

@Composable
fun CrackerSimulationView(
    cracker: CrackerItem,
    stage: BurstStage,
    fuseBurnProgress: Float,
    level: Int = 1,
    onBurstComplete: () -> Unit,
    onDetonate: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val levelScale = CrackerLevelConfig.getLevelScale(level)
    val particleMultiplier = CrackerLevelConfig.getLevelParticleMultiplier(level)
    val coroutineScope = rememberCoroutineScope()
    val particles = remember { mutableStateListOf<RealisticParticle>() }
    var burstElapsedMs by remember { mutableFloatStateOf(0f) }
    var screenShakeX by remember { mutableFloatStateOf(0f) }
    var screenShakeY by remember { mutableFloatStateOf(0f) }
    var ambientFlashAlpha by remember { mutableFloatStateOf(0f) }
    var scorchMarkRadius by remember { mutableFloatStateOf(0f) }

    var chakriAngle by remember { mutableFloatStateOf(0f) }
    var rocketAltitude by remember { mutableFloatStateOf(0f) }
    var rocketVelocityY by remember { mutableFloatStateOf(0f) }
    var rocketExploded by remember { mutableStateOf(false) }
    var bombDetonated by remember { mutableStateOf(false) }

    var whizAltitude by remember { mutableFloatStateOf(0f) }
    var mortarAltitude by remember { mutableFloatStateOf(0f) }
    var mortarVelocityY by remember { mutableFloatStateOf(0f) }
    var mortarExploded by remember { mutableStateOf(false) }
    var snakeTipX by remember { mutableFloatStateOf(0f) }
    var snakeTipY by remember { mutableFloatStateOf(-6f) }
    var ladiNextPopMs by remember { mutableFloatStateOf(0f) }
    var skyShotsFired by remember { mutableIntStateOf(0) }
    var skyShotsNextLaunchMs by remember { mutableFloatStateOf(0f) }

    // Reset state
    LaunchedEffect(stage, cracker.id) {
        if (stage == BurstStage.READY) {
            particles.clear()
            burstElapsedMs = 0f
            screenShakeX = 0f
            screenShakeY = 0f
            ambientFlashAlpha = 0f
            scorchMarkRadius = 0f
            chakriAngle = 0f
            rocketAltitude = 0f
            rocketVelocityY = 0f
            rocketExploded = false
            bombDetonated = false
            whizAltitude = 0f
            mortarAltitude = 0f
            mortarVelocityY = 0f
            mortarExploded = false
            snakeTipX = 0f
            snakeTipY = -6f
            ladiNextPopMs = 0f
            skyShotsFired = 0
            skyShotsNextLaunchMs = 150f
        }
    }

    // High-performance 60fps simulation loop
    LaunchedEffect(stage, cracker.id) {
        if (stage == BurstStage.BURSTING) {
            particles.clear()
            burstElapsedMs = 0f
            bombDetonated = false
            var completedTriggered = false
            var lastTime = 0L
            while (stage == BurstStage.BURSTING) {
                withFrameNanos { now ->
                    if (lastTime == 0L) lastTime = now
                    val dt = ((now - lastTime) / 1_000_000f).coerceIn(1f, 32f)
                    lastTime = now
                    burstElapsedMs += dt

                    val rand = Random(now.toInt())

                    // Ambient flash decay (extended duration for nuclear/boss whiteout)
                    if (ambientFlashAlpha > 0f) {
                        val decayRate = if (cracker.type == CrackerType.SUTLI_BOMB && SutliBombLevelConfig.getDetail(level).explosionProfile.hasWhiteoutFlash) {
                            0.0012f // 1 full second blinding whiteout flash
                        } else {
                            0.006f
                        }
                        ambientFlashAlpha = (ambientFlashAlpha - dt * decayRate).coerceAtLeast(0f)
                    }

                    // Screen shake damping
                    screenShakeX *= 0.88f
                    screenShakeY *= 0.88f

                    // -------------------------------------------------------------
                    // PARTICLE GENERATION PER CRACKER TYPE
                    // -------------------------------------------------------------
                    when (cracker.type) {
                        CrackerType.SUTLI_BOMB -> {
                            if (!bombDetonated) {
                                bombDetonated = true
                                val detail = SutliBombLevelConfig.getDetail(level)
                                val exp = detail.explosionProfile

                                ambientFlashAlpha = exp.flashIntensity
                                scorchMarkRadius = exp.scorchRadius
                                screenShakeX = (rand.nextFloat() - 0.5f) * exp.screenShakePx
                                screenShakeY = (rand.nextFloat() - 0.5f) * exp.screenShakePx

                                // 1. Shockwave ring expanding rapidly
                                particles.add(
                                    RealisticParticle(
                                        type = ParticleType.SHOCKWAVE,
                                        x = 0f,
                                        y = -35f,
                                        vx = 0f,
                                        vy = 0f,
                                        color = exp.shockwaveColor,
                                        radius = 14f,
                                        maxRadius = exp.shockwaveMaxRadius,
                                        alpha = 1.0f,
                                        decay = 0.024f
                                    )
                                )

                                // Twin shockwave for Level 24 (Pre-boss)
                                if (detail.soundProfile == SutliSoundProfile.PRE_BOSS_DUAL) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SHOCKWAVE,
                                            x = 0f,
                                            y = -35f,
                                            vx = 0f,
                                            vy = 0f,
                                            color = Color.White,
                                            radius = 8f,
                                            maxRadius = exp.shockwaveMaxRadius * 0.72f,
                                            alpha = 0.85f,
                                            decay = 0.035f
                                        )
                                    )
                                }

                                // 2. Additive Fireball Cloud
                                repeat(exp.fireCount) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 16f + 2f
                                    val firePalette = if (detail.isGoldStyle) {
                                        listOf(Color.White, Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFFFA000))
                                    } else if (detail.soundProfile == SutliSoundProfile.NUCLEAR_APOCALYPSE) {
                                        listOf(Color.White, Color(0xFFFFFF8D), Color(0xFFFFD600), Color.White)
                                    } else {
                                        listOf(Color(0xFFFFFFFF), Color(0xFFFFF9C4), Color(0xFFFFEB3B), Color(0xFFFF9800), Color(0xFFFF1744))
                                    }
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.FIREBALL,
                                            x = (rand.nextFloat() - 0.5f) * 24f,
                                            y = -35f + (rand.nextFloat() - 0.5f) * 24f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 2.5f,
                                            color = firePalette[rand.nextInt(firePalette.size)],
                                            coreColor = Color.White,
                                            radius = rand.nextFloat() * 32f + 22f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.022f + 0.016f,
                                            drag = 0.93f,
                                            gravity = -0.08f
                                        )
                                    )
                                }

                                // 3. Flying Charred Debris & Spark Needles
                                repeat(exp.sparkCount) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 25f + 4f
                                    val sparkColor = if (detail.isGoldStyle) {
                                        Color(0xFFFFD700)
                                    } else if (detail.hasChemicalMark) {
                                        if (rand.nextBoolean()) Color(0xFF00E5FF) else Color(0xFFE040FB)
                                    } else {
                                        if (rand.nextBoolean()) Color(0xFFFFD54F) else Color(0xFFFF3D00)
                                    }
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = 0f,
                                            y = -35f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 5f,
                                            color = sparkColor,
                                            radius = rand.nextFloat() * 4.2f + 1.8f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.014f + 0.010f,
                                            gravity = 0.32f,
                                            drag = 0.965f,
                                            trailLength = 4.8f
                                        )
                                    )
                                }

                                // 4. Volumetric Smoke Cloud Billowing
                                repeat(exp.smokeCount) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 6f + 1f
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SMOKE_PUFF,
                                            x = 0f,
                                            y = -35f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 4.5f,
                                            color = exp.smokeColor.copy(alpha = 0.75f),
                                            radius = rand.nextFloat() * 28f + 16f,
                                            alpha = 0.9f,
                                            decay = 0.008f,
                                            drag = 0.96f,
                                            gravity = -0.06f
                                        )
                                    )
                                }

                                // 5. Expanding Smoke Ring (Toroidal vortex for Level 8)
                                if (exp.hasSmokeRing) {
                                    val ringCount = 45
                                    for (i in 0 until ringCount) {
                                        val angle = (i.toDouble() / ringCount.toDouble()) * 2 * Math.PI
                                        val ringSpeed = 8.5f
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SMOKE_PUFF,
                                                x = 0f,
                                                y = -35f,
                                                vx = (ringSpeed * cos(angle)).toFloat(),
                                                vy = (ringSpeed * sin(angle) * 0.4f).toFloat() - 2.8f,
                                                color = Color(0xFF616161),
                                                radius = 16f,
                                                alpha = 0.95f,
                                                decay = 0.007f,
                                                drag = 0.98f,
                                                gravity = -0.02f
                                            )
                                        )
                                    }
                                }

                                // 6. Expanding Golden Spark Ring (Level 20)
                                if (exp.hasGoldenRing) {
                                    val ringCount = 50
                                    for (i in 0 until ringCount) {
                                        val angle = (i.toDouble() / ringCount.toDouble()) * 2 * Math.PI
                                        val ringSpeed = 14f
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SPARK_TRAIL,
                                                x = 0f,
                                                y = -35f,
                                                vx = (ringSpeed * cos(angle)).toFloat(),
                                                vy = (ringSpeed * sin(angle)).toFloat(),
                                                color = Color(0xFFFFD700),
                                                radius = 3.5f,
                                                alpha = 1.0f,
                                                decay = 0.012f,
                                                drag = 0.975f,
                                                trailLength = 4.0f
                                            )
                                        )
                                    }
                                }

                                // 7. Dust Kickup (Level 5, 13, 25)
                                if (exp.hasDustKickup) {
                                    repeat(40) {
                                        val vx = (rand.nextFloat() - 0.5f) * 22f
                                        val vy = -(rand.nextFloat() * 6f + 2f)
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SMOKE_PUFF,
                                                x = (rand.nextFloat() - 0.5f) * 30f,
                                                y = 0f,
                                                vx = vx,
                                                vy = vy,
                                                color = Color(0xFF8D6E63).copy(alpha = 0.65f),
                                                radius = rand.nextFloat() * 20f + 12f,
                                                alpha = 0.85f,
                                                decay = 0.010f,
                                                drag = 0.95f,
                                                gravity = 0.05f
                                            )
                                        )
                                    }
                                }

                                // 8. High Aerial Flares (Level 15)
                                if (exp.hasAerialFlares) {
                                    repeat(25) {
                                        val speed = rand.nextFloat() * 12f + 18f
                                        val angle = -Math.PI / 2 + (rand.nextDouble() - 0.5) * 0.6
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SPARK_TRAIL,
                                                x = 0f,
                                                y = -35f,
                                                vx = (speed * cos(angle)).toFloat(),
                                                vy = (speed * sin(angle)).toFloat(),
                                                color = Color(0xFFE040FB),
                                                radius = 4f,
                                                alpha = 1.0f,
                                                decay = 0.009f,
                                                gravity = 0.25f,
                                                drag = 0.985f,
                                                trailLength = 5.5f
                                            )
                                        )
                                    }
                                }

                                // 9. Final Boss Skyward Fireworks Burst (Level 25)
                                if (exp.hasFinalFireworks) {
                                    val shellPalette = listOf(
                                        Color(0xFFFF1744), Color(0xFFFFD700), Color(0xFF00E5FF),
                                        Color(0xFF76FF03), Color(0xFFE040FB), Color(0xFFFF9100)
                                    )
                                    val shellOffsets = listOf(
                                        Offset(-120f, -280f),
                                        Offset(120f, -280f),
                                        Offset(0f, -380f),
                                        Offset(-70f, -440f),
                                        Offset(70f, -440f)
                                    )
                                    for ((sIdx, shellCenter) in shellOffsets.withIndex()) {
                                        val shellColor = shellPalette[sIdx % shellPalette.size]
                                        repeat(35) {
                                            val angle = rand.nextDouble() * 2 * Math.PI
                                            val speed = rand.nextFloat() * 14f + 3f
                                            particles.add(
                                                RealisticParticle(
                                                    type = ParticleType.SPARK_TRAIL,
                                                    x = shellCenter.x,
                                                    y = shellCenter.y,
                                                    vx = (speed * cos(angle)).toFloat(),
                                                    vy = (speed * sin(angle)).toFloat(),
                                                    color = shellColor,
                                                    radius = 3.5f,
                                                    alpha = 1.0f,
                                                    decay = 0.011f,
                                                    gravity = 0.15f,
                                                    drag = 0.97f,
                                                    trailLength = 4.2f
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        CrackerType.CHAKRI -> {
                            // Centrifugal spinning vortex of fire on the ground
                            chakriAngle = (chakriAngle + dt * 4.2f) % 360f
                            scorchMarkRadius = 55f

                            if (burstElapsedMs < 4000f) {
                                val intensity = if (burstElapsedMs < 3200f) 1.0f else ((4000f - burstElapsedMs) / 800f).coerceIn(0f, 1f)
                                ambientFlashAlpha = (0.35f + (rand.nextFloat() * 0.15f)) * intensity
                                screenShakeX = (rand.nextFloat() - 0.5f) * 5f * intensity
                                screenShakeY = (rand.nextFloat() - 0.5f) * 5f * intensity

                                // Tangential spark streams radiating outwards 360° from the spinning disk
                                val sparkCount = (26 * intensity).toInt().coerceAtLeast(4)
                                repeat(sparkCount) {
                                    val angle = Math.toRadians((chakriAngle + rand.nextInt(360)).toDouble())
                                    val tangentAngle = angle + (Math.PI / 2.0)
                                    val speed = rand.nextFloat() * 18f + 8f
                                    val diskR = 36f

                                    val spawnX = (diskR * cos(angle)).toFloat()
                                    val spawnY = (diskR * sin(angle) * 0.40f).toFloat() // 3D ground perspective oval

                                    val colors = listOf(Color(0xFFFFFFFF), Color(0xFFFFD54F), Color(0xFFFF9100), Color(0xFFFF3D00), Color(0xFFFFEB3B))
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = spawnX,
                                            y = spawnY - 10f,
                                            vx = (speed * cos(tangentAngle)).toFloat(),
                                            vy = (speed * sin(tangentAngle) * 0.40f).toFloat() - rand.nextFloat() * 2.5f,
                                            color = colors[rand.nextInt(colors.size)],
                                            radius = rand.nextFloat() * 3.8f + 1.8f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.008f + 0.020f,
                                            gravity = 0.16f,
                                            drag = 0.965f,
                                            trailLength = 4.2f
                                        )
                                    )
                                }

                                // Glowing ground fire core
                                if (rand.nextFloat() < 0.6f * intensity) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.FIREBALL,
                                            x = (rand.nextFloat() - 0.5f) * 24f,
                                            y = -10f + (rand.nextFloat() - 0.5f) * 6f,
                                            vx = (rand.nextFloat() - 0.5f) * 2f,
                                            vy = -rand.nextFloat() * 2f,
                                            color = Color(0xFFFFB300),
                                            radius = rand.nextFloat() * 20f + 14f,
                                            alpha = 0.85f,
                                            decay = 0.05f,
                                            drag = 0.9f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.ANAR -> {
                            // Towering High-Pressure Flowerpot Fountain (Dadam)
                            if (burstElapsedMs < 4800f) {
                                val intensity = when {
                                    burstElapsedMs < 350f -> (burstElapsedMs / 350f).coerceIn(0.2f, 1f)
                                    burstElapsedMs < 3800f -> 1.0f
                                    else -> ((4800f - burstElapsedMs) / 1000f).coerceIn(0f, 1f)
                                }

                                ambientFlashAlpha = (0.42f * intensity) + (rand.nextFloat() * 0.15f * intensity)
                                screenShakeX = (rand.nextFloat() - 0.5f) * 6f * intensity
                                screenShakeY = (rand.nextFloat() - 0.5f) * 6f * intensity

                                // 1. Ultra-High Velocity Core Jet (shoots 450-550px into the night sky!)
                                val coreCount = (20 * intensity).toInt().coerceAtLeast(4)
                                repeat(coreCount) {
                                    val spreadX = (rand.nextFloat() - 0.5f) * 5.0f * intensity
                                    val launchVy = -(rand.nextFloat() * 26f + 38f) * intensity
                                    val colors = listOf(Color.White, Color(0xFFFFF9C4), Color(0xFFFFD54F), Color(0xFFFFE082))
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 8f,
                                            y = -88f,
                                            vx = spreadX,
                                            vy = launchVy,
                                            color = colors[rand.nextInt(colors.size)],
                                            radius = rand.nextFloat() * 3.8f + 2.0f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.004f + 0.007f, // survives 100-140 frames!
                                            gravity = 0.20f,
                                            drag = 0.988f,
                                            trailLength = 5.4f
                                        )
                                    )
                                }

                                // 2. Wide Multicolored Weeping Willow Fan-out (creates umbrella cascading shower)
                                val fanCount = (18 * intensity).toInt().coerceAtLeast(3)
                                repeat(fanCount) {
                                    val spreadX = (rand.nextFloat() - 0.5f) * 15f * intensity
                                    val launchVy = -(rand.nextFloat() * 20f + 26f) * intensity
                                    val fanColors = listOf(
                                        Color(0xFFFFD54F), // Gold
                                        Color(0xFFFF1744), // Ruby Red
                                        Color(0xFF00E5FF), // Electric Cyan
                                        Color(0xFF76FF03), // Lime Sparkle
                                        Color(0xFFFF4081), // Pink Neon
                                        Color(0xFFFF9100)  // Deep Orange
                                    )
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 12f,
                                            y = -88f,
                                            vx = spreadX,
                                            vy = launchVy,
                                            color = fanColors[rand.nextInt(fanColors.size)],
                                            radius = rand.nextFloat() * 4.2f + 1.8f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.005f + 0.008f,
                                            gravity = 0.22f,
                                            drag = 0.985f,
                                            trailLength = 4.8f
                                        )
                                    )
                                }

                                // 3. Drifting Fairy Gold Sparkles (twinkling embers that float down slowly)
                                if (rand.nextFloat() < 0.6f * intensity) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 18f,
                                            y = -88f,
                                            vx = (rand.nextFloat() - 0.5f) * 7f,
                                            vy = -(rand.nextFloat() * 22f + 20f),
                                            color = Color(0xFFFFF176),
                                            radius = rand.nextFloat() * 2.5f + 1.2f,
                                            alpha = 1f,
                                            decay = 0.006f,
                                            gravity = 0.12f,
                                            drag = 0.975f,
                                            trailLength = 3.2f
                                        )
                                    )
                                }

                                // 4. Volumetric Smoke above fountain
                                if (rand.nextFloat() < 0.35f * intensity) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SMOKE_PUFF,
                                            x = (rand.nextFloat() - 0.5f) * 45f,
                                            y = -340f * intensity,
                                            vx = (rand.nextFloat() - 0.5f) * 2f,
                                            vy = -rand.nextFloat() * 2.5f - 1f,
                                            color = Color(0x44B0BEC5),
                                            radius = rand.nextFloat() * 26f + 16f,
                                            alpha = 0.45f,
                                            decay = 0.010f,
                                            gravity = -0.04f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.ROCKET -> {
                            if (!rocketExploded) {
                                // Rocket ascends smoothly and patiently into the deep upper sky ("dhire thi santi thi")
                                if (rocketVelocityY < 2.0f) rocketVelocityY = 2.0f
                                rocketVelocityY += 0.048f
                                rocketAltitude += rocketVelocityY

                                // Exhaust thrust sparks & smoke trailing below rocket
                                repeat(6) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 6f,
                                            y = -rocketAltitude + 20f,
                                            vx = (rand.nextFloat() - 0.5f) * 2.5f,
                                            vy = rand.nextFloat() * 6f + 3.5f,
                                            color = if (rand.nextBoolean()) Color(0xFFFFD54F) else Color(0xFFFF9100),
                                            radius = rand.nextFloat() * 3.5f + 1.5f,
                                            alpha = 0.9f,
                                            decay = 0.038f,
                                            trailLength = 3.8f
                                        )
                                    )
                                }

                                // Exhaust smoke
                                particles.add(
                                    RealisticParticle(
                                        type = ParticleType.SMOKE_PUFF,
                                        x = (rand.nextFloat() - 0.5f) * 8f,
                                        y = -rocketAltitude + 26f,
                                        vx = (rand.nextFloat() - 0.5f) * 1.5f,
                                        vy = 1.0f,
                                        color = Color(0x66B0BEC5),
                                        radius = rand.nextFloat() * 14f + 8f,
                                        alpha = 0.65f,
                                        decay = 0.018f
                                    )
                                )

                                // Reached stratosphere apex near the very top of the sky!
                                val targetRocketApex = 1350f + (level * 25f)
                                if (rocketAltitude > targetRocketApex) {
                                    rocketExploded = true
                                    onDetonate()
                                    ambientFlashAlpha = 1.0f
                                    screenShakeX = (rand.nextFloat() - 0.5f) * 28f * levelScale
                                    screenShakeY = (rand.nextFloat() - 0.5f) * 28f * levelScale

                                    // Spherical Sky Shockwave
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SHOCKWAVE,
                                            x = 0f,
                                            y = -rocketAltitude,
                                            vx = 0f,
                                            vy = 0f,
                                            color = Color(0xFFFFEA00),
                                            radius = 10f,
                                            maxRadius = 360f * levelScale,
                                            alpha = 1f,
                                            decay = 0.030f
                                        )
                                    )

                                    // Spherical Aerial Chrysanthemum Bloom (180 radiant stars)
                                    val rocketStars = (180 * particleMultiplier).toInt()
                                    repeat(rocketStars) {
                                        val angle = rand.nextDouble() * 2 * Math.PI
                                        val speed = rand.nextFloat() * 17f + 3f
                                        val starColors = listOf(
                                            Color(0xFFFF1744),
                                            Color(0xFF00E5FF),
                                            Color(0xFFFFEA00),
                                            Color(0xFF76FF03),
                                            Color(0xFFE040FB),
                                            Color(0xFFFF6D00),
                                            Color.White
                                        )
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SPARK_TRAIL,
                                                x = 0f,
                                                y = -rocketAltitude,
                                                vx = (speed * cos(angle)).toFloat(),
                                                vy = (speed * sin(angle)).toFloat(),
                                                color = starColors[rand.nextInt(starColors.size)],
                                                radius = rand.nextFloat() * 4.6f + 2.0f,
                                                alpha = 1f,
                                                decay = rand.nextFloat() * 0.012f + 0.010f,
                                                gravity = 0.14f,
                                                drag = 0.968f,
                                                trailLength = 4.8f
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        CrackerType.SKY_SHOTS -> {
                            val totalShots = when {
                                level <= 3 -> 3
                                level <= 8 -> 5
                                level <= 16 -> 7
                                level <= 22 -> 9
                                else -> 12
                            }
                            if (skyShotsFired < totalShots && burstElapsedMs >= skyShotsNextLaunchMs) {
                                skyShotsFired++
                                skyShotsNextLaunchMs = burstElapsedMs + 650f

                                ambientFlashAlpha = 0.65f
                                screenShakeX = (rand.nextFloat() - 0.5f) * 12f
                                screenShakeY = (rand.nextFloat() - 0.5f) * 12f

                                // Launch ground smoke & muzzle flash
                                repeat(20) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 9f + 2f
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 24f,
                                            y = -40f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 4f,
                                            color = Color(0xFFFFD54F),
                                            radius = rand.nextFloat() * 3.5f + 1.5f,
                                            alpha = 1f,
                                            decay = 0.025f
                                        )
                                    )
                                }

                                // Ascending tracer comet soaring to sky apex
                                val shotIndex = skyShotsFired
                                val targetApexY = -(1200f + rand.nextFloat() * 250f)
                                val shotPalettes = listOf(
                                    listOf(Color(0xFFFF1744), Color(0xFFFF8A80), Color.White),
                                    listOf(Color(0xFF00E5FF), Color(0xFF80D8FF), Color.White),
                                    listOf(Color(0xFFFFEA00), Color(0xFFFFD54F), Color(0xFFFF6D00)),
                                    listOf(Color(0xFF76FF03), Color(0xFFB9F6CA), Color.White),
                                    listOf(Color(0xFFE040FB), Color(0xFFEA80FC), Color.White),
                                    listOf(Color(0xFFFF3D00), Color(0xFFFFD600), Color(0xFFFF1744)),
                                    listOf(Color(0xFFFF1744), Color(0xFF00E5FF), Color(0xFFFFEA00), Color(0xFFE040FB), Color.White)
                                )
                                val currentPalette = shotPalettes[(shotIndex - 1) % shotPalettes.size]

                                // Fast soaring tracer comet
                                particles.add(
                                    RealisticParticle(
                                        type = ParticleType.FIREBALL,
                                        x = (rand.nextFloat() - 0.5f) * 16f,
                                        y = -40f,
                                        vx = (rand.nextFloat() - 0.5f) * 1.5f,
                                        vy = -30f,
                                        color = Color(0xFFFFD54F),
                                        coreColor = Color.White,
                                        radius = 9f,
                                        alpha = 1f,
                                        decay = 0.024f,
                                        drag = 0.985f,
                                        gravity = 0.25f
                                    )
                                )

                                // Detonate burst in high sky
                                coroutineScope.launch {
                                    delay(520)
                                    ambientFlashAlpha = 0.9f
                                    val shotStars = (90 * particleMultiplier).toInt()
                                    repeat(shotStars) {
                                        val angle = rand.nextDouble() * 2 * Math.PI
                                        val speed = rand.nextFloat() * 16f + 3f
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SPARK_TRAIL,
                                                x = (rand.nextFloat() - 0.5f) * 28f,
                                                y = targetApexY,
                                                vx = (speed * cos(angle)).toFloat(),
                                                vy = (speed * sin(angle)).toFloat(),
                                                color = currentPalette[rand.nextInt(currentPalette.size)],
                                                radius = rand.nextFloat() * 4.2f + 1.8f,
                                                alpha = 1f,
                                                decay = rand.nextFloat() * 0.012f + 0.010f,
                                                gravity = 0.16f,
                                                drag = 0.968f,
                                                trailLength = 4.5f
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        CrackerType.PHOOLJHADI -> {
                            if (burstElapsedMs < 3800f) {
                                ambientFlashAlpha = 0.22f + (rand.nextFloat() * 0.12f)
                                repeat(18) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 11f + 3f
                                    val colors = listOf(Color.White, Color(0xFFFFF59D), Color(0xFFFFD54F), Color(0xFF00E5FF), Color(0xFFFF4081))
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = 15f,
                                            y = -85f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat(),
                                            color = colors[rand.nextInt(colors.size)],
                                            radius = rand.nextFloat() * 3.6f + 1.5f,
                                            alpha = 1f,
                                            decay = 0.038f,
                                            gravity = 0.12f,
                                            trailLength = 3.8f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.PENCIL_WHIP -> {
                            if (burstElapsedMs < 3600f) {
                                ambientFlashAlpha = 0.20f + (rand.nextFloat() * 0.15f)
                                val stickY = -(rand.nextFloat() * 85f + 15f)
                                val stickX = (rand.nextFloat() - 0.5f) * 8f
                                repeat(12) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 16f + 3f
                                    val colors = listOf(Color.White, Color(0xFFFFD54F), Color(0xFFEA80FC), Color(0xFFFF4081))
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = stickX,
                                            y = stickY,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat(),
                                            color = colors[rand.nextInt(colors.size)],
                                            radius = rand.nextFloat() * 3.4f + 1.4f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.025f + 0.020f,
                                            gravity = 0.10f,
                                            trailLength = 3.5f
                                        )
                                    )
                                }
                                if (rand.nextFloat() < 0.25f) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SMOKE_PUFF,
                                            x = stickX,
                                            y = stickY,
                                            vx = (rand.nextFloat() - 0.5f) * 1.5f,
                                            vy = -rand.nextFloat() * 2f,
                                            color = Color(0x55B0BEC5),
                                            radius = rand.nextFloat() * 14f + 8f,
                                            alpha = 0.5f,
                                            decay = 0.015f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.PAPER_BOMB, CrackerType.LAXMI_BOMB -> {
                            if (!bombDetonated) {
                                bombDetonated = true
                                ambientFlashAlpha = 1.0f
                                screenShakeX = (rand.nextFloat() - 0.5f) * 22f * levelScale
                                screenShakeY = (rand.nextFloat() - 0.5f) * 22f * levelScale
                                scorchMarkRadius = 60f * levelScale

                                particles.add(
                                    RealisticParticle(
                                        type = ParticleType.SHOCKWAVE,
                                        x = 0f,
                                        y = -25f,
                                        vx = 0f,
                                        vy = 0f,
                                        color = Color(0xFFFFEB3B),
                                        radius = 10f,
                                        maxRadius = 300f * levelScale,
                                        alpha = 1f,
                                        decay = 0.038f
                                    )
                                )

                                val fireCount = (30 * particleMultiplier).toInt()
                                repeat(fireCount) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 12f + 2f
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.FIREBALL,
                                            x = 0f,
                                            y = -25f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 2f,
                                            color = Color(0xFFFF5722),
                                            radius = rand.nextFloat() * 26f + 16f,
                                            alpha = 1f,
                                            decay = 0.024f,
                                            drag = 0.92f
                                        )
                                    )
                                }

                                val confettiCount = (90 * particleMultiplier).toInt()
                                repeat(confettiCount) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 20f + 5f
                                    val redColors = listOf(Color(0xFFD50000), Color(0xFFC62828), Color(0xFFE53935), Color(0xFFFFD54F))
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.CONFETTI,
                                            x = 0f,
                                            y = -25f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 5f,
                                            color = redColors[rand.nextInt(redColors.size)],
                                            radius = rand.nextFloat() * 5f + 3f,
                                            alpha = 1f,
                                            decay = 0.006f,
                                            gravity = 0.16f,
                                            drag = 0.94f,
                                            rotation = rand.nextFloat() * 360f,
                                            rotationSpeed = (rand.nextFloat() - 0.5f) * 16f
                                        )
                                    )
                                }

                                val sparkCount = (55 * particleMultiplier).toInt()
                                repeat(sparkCount) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 22f + 4f
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = 0f,
                                            y = -25f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 3f,
                                            color = if (rand.nextBoolean()) Color(0xFFFFD54F) else Color(0xFFFF3D00),
                                            radius = rand.nextFloat() * 3.5f + 1.5f,
                                            alpha = 1f,
                                            decay = 0.015f,
                                            gravity = 0.28f,
                                            drag = 0.96f,
                                            trailLength = 4.2f
                                        )
                                    )
                                }

                                repeat(25) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 5f + 1f
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SMOKE_PUFF,
                                            x = 0f,
                                            y = -25f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 3f,
                                            color = Color(0x66B0BEC5),
                                            radius = rand.nextFloat() * 22f + 14f,
                                            alpha = 0.8f,
                                            decay = 0.010f,
                                            gravity = -0.05f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.LADI -> {
                            if (burstElapsedMs < 4400f) {
                                if (burstElapsedMs >= ladiNextPopMs) {
                                    ladiNextPopMs = burstElapsedMs + 110f + (rand.nextFloat() * 60f)
                                    ambientFlashAlpha = 0.45f
                                    screenShakeX = (rand.nextFloat() - 0.5f) * 8f
                                    screenShakeY = (rand.nextFloat() - 0.5f) * 8f
                                    scorchMarkRadius = (scorchMarkRadius + 1.5f).coerceAtMost(65f)

                                    val popY = -90f + ((burstElapsedMs / 4400f) * 80f)
                                    val popX = (rand.nextFloat() - 0.5f) * 24f

                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.FIREBALL,
                                            x = popX,
                                            y = popY,
                                            vx = (rand.nextFloat() - 0.5f) * 2f,
                                            vy = -rand.nextFloat() * 2f,
                                            color = Color(0xFFFF5722),
                                            radius = rand.nextFloat() * 16f + 10f,
                                            alpha = 1f,
                                            decay = 0.05f
                                        )
                                    )

                                    repeat(7) {
                                        val angle = rand.nextDouble() * 2 * Math.PI
                                        val speed = rand.nextFloat() * 14f + 3f
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.CONFETTI,
                                                x = popX,
                                                y = popY,
                                                vx = (speed * cos(angle)).toFloat(),
                                                vy = (speed * sin(angle)).toFloat() - 3f,
                                                color = Color(0xFFD32F2F),
                                                radius = rand.nextFloat() * 4f + 2f,
                                                alpha = 1f,
                                                decay = 0.007f,
                                                gravity = 0.18f,
                                                drag = 0.93f,
                                                rotation = rand.nextFloat() * 360f,
                                                rotationSpeed = (rand.nextFloat() - 0.5f) * 20f
                                            )
                                        )
                                    }

                                    repeat(12) {
                                        val angle = rand.nextDouble() * 2 * Math.PI
                                        val speed = rand.nextFloat() * 16f + 4f
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SPARK_TRAIL,
                                                x = popX,
                                                y = popY,
                                                vx = (speed * cos(angle)).toFloat(),
                                                vy = (speed * sin(angle)).toFloat() - 2f,
                                                color = if (rand.nextBoolean()) Color(0xFFFFEB3B) else Color(0xFFFF9800),
                                                radius = rand.nextFloat() * 3.2f + 1.2f,
                                                alpha = 1f,
                                                decay = 0.025f,
                                                gravity = 0.22f,
                                                trailLength = 3.6f
                                            )
                                        )
                                    }

                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SMOKE_PUFF,
                                            x = popX,
                                            y = popY,
                                            vx = (rand.nextFloat() - 0.5f) * 2f,
                                            vy = -rand.nextFloat() * 3f - 1.5f,
                                            color = Color(0x66CFD8DC),
                                            radius = rand.nextFloat() * 18f + 12f,
                                            alpha = 0.85f,
                                            decay = 0.008f,
                                            gravity = -0.04f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.SNAKE_TABLET -> {
                            if (burstElapsedMs < 4800f) {
                                ambientFlashAlpha = 0.12f
                                val progress = (burstElapsedMs / 4800f).coerceIn(0f, 1f)
                                val targetHeight = progress * 160f
                                val wave = sin(progress * 14.0).toFloat() * (12f * progress)
                                snakeTipX = wave
                                snakeTipY = -6f - targetHeight

                                if (rand.nextFloat() < 0.45f) {
                                    val segmentColor = if (rand.nextBoolean()) Color(0xFF263238) else Color(0xFF212121)
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.ASH_SEGMENT,
                                            x = snakeTipX + (rand.nextFloat() - 0.5f) * 4f,
                                            y = snakeTipY,
                                            vx = 0f,
                                            vy = 0f,
                                            color = segmentColor,
                                            coreColor = if (rand.nextFloat() < 0.2f) Color(0xFFFF5722) else Color.Transparent,
                                            radius = (rand.nextFloat() * 5f + 9f) * (1f - progress * 0.25f),
                                            alpha = 1f,
                                            decay = 0f
                                        )
                                    )
                                }

                                if (rand.nextFloat() < 0.35f) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = snakeTipX,
                                            y = snakeTipY,
                                            vx = (rand.nextFloat() - 0.5f) * 3f,
                                            vy = -rand.nextFloat() * 4f - 1f,
                                            color = Color(0xFFFFAB00),
                                            radius = rand.nextFloat() * 2f + 1f,
                                            alpha = 1f,
                                            decay = 0.05f
                                        )
                                    )
                                }

                                if (rand.nextFloat() < 0.20f) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SMOKE_PUFF,
                                            x = snakeTipX,
                                            y = snakeTipY,
                                            vx = (rand.nextFloat() - 0.5f) * 1.5f,
                                            vy = -rand.nextFloat() * 2.5f - 1f,
                                            color = Color(0x4490A4AE),
                                            radius = rand.nextFloat() * 12f + 8f,
                                            alpha = 0.5f,
                                            decay = 0.012f,
                                            gravity = -0.05f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.COLOR_SPARKLER -> {
                            if (burstElapsedMs < 3800f) {
                                ambientFlashAlpha = 0.24f + (rand.nextFloat() * 0.12f)
                                repeat(22) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 13f + 3f
                                    val neonColors = listOf(
                                        Color(0xFF00E676),
                                        Color(0xFFFF1744),
                                        Color(0xFF00E5FF),
                                        Color(0xFFFFEA00),
                                        Color(0xFFD500F9)
                                    )
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = 14f,
                                            y = -75f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat(),
                                            color = neonColors[rand.nextInt(neonColors.size)],
                                            radius = rand.nextFloat() * 3.8f + 1.6f,
                                            alpha = 1f,
                                            decay = 0.035f,
                                            gravity = 0.10f,
                                            trailLength = 4.0f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.AERIAL_SHELL -> {
                            if (!mortarExploded) {
                                mortarVelocityY += dt * 0.065f + 0.50f
                                mortarAltitude += mortarVelocityY

                                if (mortarAltitude < 50f && rand.nextFloat() < 0.6f) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.FIREBALL,
                                            x = 0f,
                                            y = -48f,
                                            vx = (rand.nextFloat() - 0.5f) * 4f,
                                            vy = -rand.nextFloat() * 5f,
                                            color = Color(0xFFFF9100),
                                            radius = rand.nextFloat() * 20f + 14f,
                                            alpha = 0.9f,
                                            decay = 0.06f
                                        )
                                    )
                                }

                                repeat(8) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 6f,
                                            y = -mortarAltitude - 48f,
                                            vx = (rand.nextFloat() - 0.5f) * 4f,
                                            vy = rand.nextFloat() * 10f + 5f,
                                            color = Color(0xFFFFD54F),
                                            radius = rand.nextFloat() * 3.8f + 1.8f,
                                            alpha = 0.95f,
                                            decay = 0.040f,
                                            trailLength = 4.2f
                                        )
                                    )
                                }

                                particles.add(
                                    RealisticParticle(
                                        type = ParticleType.SMOKE_PUFF,
                                        x = (rand.nextFloat() - 0.5f) * 10f,
                                        y = -mortarAltitude - 44f,
                                        vx = (rand.nextFloat() - 0.5f) * 1.5f,
                                        vy = 1.5f,
                                        color = Color(0x66B0BEC5),
                                        radius = rand.nextFloat() * 16f + 10f,
                                        alpha = 0.6f,
                                        decay = 0.018f
                                    )
                                )

                                if (mortarAltitude > 1250f) {
                                    mortarExploded = true
                                    onDetonate()
                                    ambientFlashAlpha = 1.0f
                                    screenShakeX = (rand.nextFloat() - 0.5f) * 28f
                                    screenShakeY = (rand.nextFloat() - 0.5f) * 28f

                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SHOCKWAVE,
                                            x = 0f,
                                            y = -mortarAltitude - 48f,
                                            vx = 0f,
                                            vy = 0f,
                                            color = Color(0xFFFFD54F),
                                            radius = 12f,
                                            maxRadius = 420f,
                                            alpha = 1f,
                                            decay = 0.026f
                                        )
                                    )
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SHOCKWAVE,
                                            x = 0f,
                                            y = -mortarAltitude - 48f,
                                            vx = 0f,
                                            vy = 0f,
                                            color = Color(0xFF00E5FF),
                                            radius = 12f,
                                            maxRadius = 320f,
                                            alpha = 0.8f,
                                            decay = 0.032f
                                        )
                                    )

                                    repeat(200) {
                                        val angle = rand.nextDouble() * 2 * Math.PI
                                        val speed = rand.nextFloat() * 20f + 4f
                                        val shellColors = listOf(
                                            Color(0xFF1565C0),
                                            Color(0xFFFFD600),
                                            Color(0xFFFF1744),
                                            Color(0xFF00E5FF),
                                            Color(0xFF76FF03),
                                            Color(0xFFE040FB),
                                            Color.White
                                        )
                                        particles.add(
                                            RealisticParticle(
                                                type = ParticleType.SPARK_TRAIL,
                                                x = 0f,
                                                y = -mortarAltitude - 48f,
                                                vx = (speed * cos(angle)).toFloat(),
                                                vy = (speed * sin(angle)).toFloat(),
                                                color = shellColors[rand.nextInt(shellColors.size)],
                                                radius = rand.nextFloat() * 4.8f + 2.2f,
                                                alpha = 1f,
                                                decay = rand.nextFloat() * 0.010f + 0.009f,
                                                gravity = 0.12f,
                                                drag = 0.972f,
                                                trailLength = 5.2f
                                            )
                                        )
                                    }
                                }
                            }
                        }

                        CrackerType.WHIZ_WHEEL -> {
                            chakriAngle = (chakriAngle + dt * 6.5f) % 360f
                            scorchMarkRadius = 60f

                            if (burstElapsedMs < 4200f) {
                                val intensity = if (burstElapsedMs < 3400f) 1.0f else ((4200f - burstElapsedMs) / 800f).coerceIn(0f, 1f)
                                whizAltitude = (sin(burstElapsedMs * 0.008f) * 18f + 22f) * intensity
                                ambientFlashAlpha = (0.35f + rand.nextFloat() * 0.15f) * intensity
                                screenShakeX = (rand.nextFloat() - 0.5f) * 6f * intensity
                                screenShakeY = (rand.nextFloat() - 0.5f) * 6f * intensity

                                val sparkCount = (28 * intensity).toInt().coerceAtLeast(4)
                                repeat(sparkCount) {
                                    val angle = Math.toRadians((chakriAngle + rand.nextInt(360)).toDouble())
                                    val tangentAngle = angle + (Math.PI / 2.0)
                                    val speed = rand.nextFloat() * 22f + 8f
                                    val diskR = 38f
                                    val spawnX = (diskR * cos(angle)).toFloat()
                                    val spawnY = (diskR * sin(angle) * 0.40f).toFloat() - whizAltitude

                                    val colors = listOf(Color(0xFF00B0FF), Color(0xFFFFD54F), Color(0xFFFFAB00), Color.White)
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = spawnX,
                                            y = spawnY,
                                            vx = (speed * cos(tangentAngle)).toFloat(),
                                            vy = (speed * sin(tangentAngle) * 0.40f).toFloat() - rand.nextFloat() * 3f,
                                            color = colors[rand.nextInt(colors.size)],
                                            radius = rand.nextFloat() * 3.8f + 1.6f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.009f + 0.018f,
                                            gravity = 0.14f,
                                            drag = 0.965f,
                                            trailLength = 4.5f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.ELECTRIC_FOUNTAIN -> {
                            if (burstElapsedMs < 4600f) {
                                val intensity = when {
                                    burstElapsedMs < 250f -> (burstElapsedMs / 250f).coerceIn(0.2f, 1f)
                                    burstElapsedMs < 3800f -> 1.0f
                                    else -> ((4600f - burstElapsedMs) / 800f).coerceIn(0f, 1f)
                                }

                                ambientFlashAlpha = (0.45f * intensity) + (rand.nextFloat() * 0.15f * intensity)
                                screenShakeX = (rand.nextFloat() - 0.5f) * 6f * intensity
                                screenShakeY = (rand.nextFloat() - 0.5f) * 6f * intensity

                                val sparkCount = (32 * intensity).toInt().coerceAtLeast(6)
                                repeat(sparkCount) {
                                    val spreadX = (rand.nextFloat() - 0.5f) * 7.5f * intensity
                                    val launchVy = -(rand.nextFloat() * 30f + 42f) * intensity
                                    val silverColors = listOf(Color.White, Color(0xFFE0F7FA), Color(0xFFB2EBF2), Color(0xFF80DEEA))
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 8f,
                                            y = -98f,
                                            vx = spreadX,
                                            vy = launchVy,
                                            color = silverColors[rand.nextInt(silverColors.size)],
                                            radius = rand.nextFloat() * 3.5f + 1.8f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.004f + 0.008f,
                                            gravity = 0.18f,
                                            drag = 0.988f,
                                            trailLength = 5.2f
                                        )
                                    )
                                }

                                if (rand.nextFloat() < 0.8f * intensity) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.FIREBALL,
                                            x = (rand.nextFloat() - 0.5f) * 12f,
                                            y = -98f,
                                            vx = (rand.nextFloat() - 0.5f) * 1.5f,
                                            vy = -rand.nextFloat() * 2f,
                                            color = Color.White,
                                            radius = rand.nextFloat() * 22f + 14f,
                                            alpha = 0.95f,
                                            decay = 0.06f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // -------------------------------------------------------------
                    // PARTICLE PHYSICS UPDATE LOOP
                    // -------------------------------------------------------------
                    val iterator = particles.iterator()
                    while (iterator.hasNext()) {
                        val p = iterator.next()
                        p.age += 1f
                        p.x += p.vx
                        p.y += p.vy
                        p.vx *= p.drag
                        p.vy = (p.vy + p.gravity) * p.drag
                        p.alpha -= p.decay

                        // For Anar, if a falling spark hits the ground, extinguish it
                        if (cracker.type == CrackerType.ANAR && p.y > 6f) {
                            p.decay = 0.12f
                            p.vy = -p.vy * 0.2f
                        }

                        if (p.type == ParticleType.CONFETTI) {
                            p.rotation += p.rotationSpeed
                            p.vx *= 0.94f
                            p.vy = (p.vy + p.gravity) * 0.94f
                        } else if (p.type == ParticleType.SHOCKWAVE) {
                            p.radius += (p.maxRadius - p.radius) * 0.22f
                        } else if (p.type == ParticleType.FIREBALL || p.type == ParticleType.SMOKE_PUFF) {
                            p.radius += 0.8f // expansion
                        }

                        if (p.alpha <= 0f) {
                            iterator.remove()
                        }
                    }

                    val durationLimit = when (cracker.type) {
                        CrackerType.SUTLI_BOMB -> {
                            when {
                                level >= 25 -> 3800f
                                level == 22 -> 3000f
                                else -> 2400f
                            }
                        }
                        CrackerType.PAPER_BOMB, CrackerType.LAXMI_BOMB -> 2200f
                        CrackerType.ROCKET -> 4800f
                        CrackerType.SKY_SHOTS -> {
                            val sCount = when {
                                level <= 3 -> 3
                                level <= 8 -> 5
                                level <= 16 -> 7
                                level <= 22 -> 9
                                else -> 12
                            }
                            sCount * 680f + 1800f
                        }
                        CrackerType.AERIAL_SHELL -> 4600f
                        CrackerType.CHAKRI -> 4200f
                        CrackerType.WHIZ_WHEEL -> 4400f
                        CrackerType.ANAR -> 5200f
                        CrackerType.ELECTRIC_FOUNTAIN -> 5000f
                        CrackerType.PHOOLJHADI -> 4000f
                        CrackerType.COLOR_SPARKLER -> 4000f
                        CrackerType.PENCIL_WHIP -> 3800f
                        CrackerType.LADI -> 4800f
                        CrackerType.SNAKE_TABLET -> 5200f
                    }

                    if (!completedTriggered && (
                        (burstElapsedMs > durationLimit && particles.isEmpty()) || 
                        (cracker.type == CrackerType.SNAKE_TABLET && burstElapsedMs > durationLimit) ||
                        burstElapsedMs > durationLimit + 1600f
                    )) {
                        completedTriggered = true
                        onBurstComplete()
                    }
                }
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Scenic Night Sky & Background Monument with Ambient Lighting
            drawPhotorealisticScene(width, height, ambientFlashAlpha, cracker.primaryColor)

            // Dynamic gentle camera follow allowing rocket to soar high into the deep night sky
            val cameraFollowY = when (cracker.type) {
                CrackerType.ROCKET -> (rocketAltitude * 0.12f).coerceAtMost(height * 0.10f)
                CrackerType.AERIAL_SHELL -> (mortarAltitude * 0.60f).coerceAtMost(height * 0.45f)
                else -> 0f
            }

            // 2. Pedestal / Stand Center Platform
            val pedestalCenter = Offset(width / 2 + screenShakeX, height * 0.76f + cameraFollowY + screenShakeY)
            drawPedestalWithScorch(pedestalCenter, scorchMarkRadius)

            // Chakri Ground Fiery Vortex Glow
            if (cracker.type == CrackerType.CHAKRI && stage == BurstStage.BURSTING) {
                val vortexRadius = 120f + (sin(burstElapsedMs * 0.025f) * 16f)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.90f),
                            Color(0xFFFFD54F).copy(alpha = 0.80f),
                            Color(0xFFFF6D00).copy(alpha = 0.50f),
                            Color.Transparent
                        ),
                        center = Offset(pedestalCenter.x, pedestalCenter.y - 10f),
                        radius = vortexRadius
                    ),
                    topLeft = Offset(pedestalCenter.x - vortexRadius, pedestalCenter.y - 10f - vortexRadius * 0.36f),
                    size = Size(vortexRadius * 2f, vortexRadius * 0.72f),
                    blendMode = BlendMode.Plus
                )
            }

            // Anar Roaring Jet Flame at Top Nozzle
            if (cracker.type == CrackerType.ANAR && stage == BurstStage.BURSTING) {
                val nozzleRadius = 38f + (sin(burstElapsedMs * 0.04f) * 6f)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFFFFD54F).copy(alpha = 0.90f),
                            Color(0xFFFF3D00).copy(alpha = 0.50f),
                            Color.Transparent
                        ),
                        center = Offset(pedestalCenter.x, pedestalCenter.y - 88f),
                        radius = nozzleRadius
                    ),
                    topLeft = Offset(pedestalCenter.x - nozzleRadius * 0.8f, pedestalCenter.y - 88f - nozzleRadius),
                    size = Size(nozzleRadius * 1.6f, nozzleRadius * 1.5f),
                    blendMode = BlendMode.Plus
                )
            }

            // Whiz Wheel Ground & Aerial Fiery Vortex Glow
            if (cracker.type == CrackerType.WHIZ_WHEEL && stage == BurstStage.BURSTING) {
                val vortexRadius = 110f + (sin(burstElapsedMs * 0.03f) * 14f)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFF00B0FF).copy(alpha = 0.80f),
                            Color(0xFFFFAB00).copy(alpha = 0.50f),
                            Color.Transparent
                        ),
                        center = Offset(pedestalCenter.x, pedestalCenter.y - 10f - whizAltitude),
                        radius = vortexRadius
                    ),
                    topLeft = Offset(pedestalCenter.x - vortexRadius, pedestalCenter.y - 10f - whizAltitude - vortexRadius * 0.36f),
                    size = Size(vortexRadius * 2f, vortexRadius * 0.72f),
                    blendMode = BlendMode.Plus
                )
            }

            // Electric Fountain Crystalline Nozzle Flame
            if (cracker.type == CrackerType.ELECTRIC_FOUNTAIN && stage == BurstStage.BURSTING) {
                val nozzleRadius = 36f + (sin(burstElapsedMs * 0.05f) * 6f)
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White,
                            Color(0xFF80DEEA).copy(alpha = 0.90f),
                            Color(0xFF00E5FF).copy(alpha = 0.50f),
                            Color.Transparent
                        ),
                        center = Offset(pedestalCenter.x, pedestalCenter.y - 98f),
                        radius = nozzleRadius
                    ),
                    topLeft = Offset(pedestalCenter.x - nozzleRadius * 0.8f, pedestalCenter.y - 98f - nozzleRadius),
                    size = Size(nozzleRadius * 1.6f, nozzleRadius * 1.5f),
                    blendMode = BlendMode.Plus
                )
            }

            // 3. Cracker Body on Pedestal (disappears when bomb detonates or rocket flies)
            val showCrackerBody = when (cracker.type) {
                CrackerType.SUTLI_BOMB, CrackerType.PAPER_BOMB, CrackerType.LAXMI_BOMB -> !bombDetonated
                CrackerType.ROCKET -> !rocketExploded
                else -> true
            }

            if (showCrackerBody) {
                val crackerY = when {
                    cracker.type == CrackerType.ROCKET && stage == BurstStage.BURSTING -> pedestalCenter.y - rocketAltitude
                    cracker.type == CrackerType.WHIZ_WHEEL && stage == BurstStage.BURSTING -> pedestalCenter.y - whizAltitude
                    else -> pedestalCenter.y
                }

                scale(levelScale, pivot = Offset(pedestalCenter.x, crackerY)) {
                    drawCrackerStageDetailed(
                        center = Offset(pedestalCenter.x, crackerY),
                        cracker = cracker,
                        stage = stage,
                        fuseProgress = fuseBurnProgress,
                        chakriAngle = chakriAngle,
                        level = level
                    )
                }
            }

            // 4. DRAW PARTICLES WITH ADDITIVE BLENDING & REALISTIC STREAKS
            // Step A: Draw smoke puffs first (normal blend mode)
            particles.filter { it.type == ParticleType.SMOKE_PUFF }.forEach { p ->
                val px = pedestalCenter.x + p.x
                val py = pedestalCenter.y + p.y
                drawCircle(
                    color = p.color.copy(alpha = (p.alpha * 0.5f).coerceIn(0f, 1f)),
                    radius = p.radius,
                    center = Offset(px, py)
                )
            }

            // Step B: Draw Confetti (fluttering paper fragments)
            particles.filter { it.type == ParticleType.CONFETTI }.forEach { p ->
                val px = pedestalCenter.x + p.x
                val py = pedestalCenter.y + p.y
                val curAlpha = p.alpha.coerceIn(0f, 1f)
                rotate(p.rotation, pivot = Offset(px, py)) {
                    drawRect(
                        color = p.color.copy(alpha = curAlpha),
                        topLeft = Offset(px - p.radius, py - p.radius * 0.6f),
                        size = Size(p.radius * 2f, p.radius * 1.2f)
                    )
                }
            }

            // Step C: Draw Ash Segments (Growing serpentine carbon snake)
            particles.filter { it.type == ParticleType.ASH_SEGMENT }.forEach { p ->
                val px = pedestalCenter.x + p.x
                val py = pedestalCenter.y + p.y
                val curAlpha = p.alpha.coerceIn(0f, 1f)
                drawCircle(
                    color = p.color.copy(alpha = curAlpha),
                    radius = p.radius,
                    center = Offset(px, py)
                )
                if (p.coreColor != Color.Transparent) {
                    drawCircle(
                        color = p.coreColor.copy(alpha = curAlpha * 0.85f),
                        radius = p.radius * 0.45f,
                        center = Offset(px, py),
                        blendMode = BlendMode.Plus
                    )
                }
            }

            // Step D: Draw Shockwave rings
            particles.filter { it.type == ParticleType.SHOCKWAVE }.forEach { p ->
                val px = pedestalCenter.x + p.x
                val py = pedestalCenter.y + p.y
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.radius,
                    center = Offset(px, py),
                    style = Stroke(width = 6f * p.alpha)
                )
            }

            // Step E: Draw Additive Fireballs (Glowing overlapping fire)
            particles.filter { it.type == ParticleType.FIREBALL }.forEach { p ->
                val px = pedestalCenter.x + p.x
                val py = pedestalCenter.y + p.y
                val curAlpha = p.alpha.coerceIn(0f, 1f)

                // Soft radial glow with Plus blend mode
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = curAlpha),
                            GoldYellow.copy(alpha = curAlpha * 0.85f),
                            FestiveOrange.copy(alpha = curAlpha * 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(px, py),
                        radius = p.radius
                    ),
                    radius = p.radius,
                    center = Offset(px, py),
                    blendMode = BlendMode.Plus
                )
            }

            // Step F: Draw Spark Trails (Needle sparks pointing in velocity direction with Additive Glow!)
            particles.filter { it.type == ParticleType.SPARK_TRAIL }.forEach { p ->
                val px = pedestalCenter.x + p.x
                val py = pedestalCenter.y + p.y
                val effectiveTrail = p.trailLength * (p.age / 4f).coerceIn(0f, 1f)
                val tailX = px - p.vx * effectiveTrail
                val tailY = py - p.vy * effectiveTrail
                val curAlpha = p.alpha.coerceIn(0f, 1f)

                // Outer glowing trail streak
                drawLine(
                    color = p.color.copy(alpha = curAlpha),
                    start = Offset(tailX, tailY),
                    end = Offset(px, py),
                    strokeWidth = p.radius * 1.8f,
                    cap = StrokeCap.Round,
                    blendMode = BlendMode.Plus
                )

                // Intense White-Hot Core Head
                drawCircle(
                    color = Color.White.copy(alpha = curAlpha),
                    radius = p.radius * 0.7f,
                    center = Offset(px, py),
                    blendMode = BlendMode.Plus
                )
            }

            // 5. Full Screen Blinding Detonation Flash Overlay
            if (ambientFlashAlpha > 0.02f) {
                drawRect(
                    color = Color.White.copy(alpha = ambientFlashAlpha * 0.75f),
                    size = Size(width, height),
                    blendMode = BlendMode.Plus
                )
            }
        }
    }
}

// Draw the Taj Mahal scenic backdrop with dynamic ambient lighting
private fun DrawScope.drawPhotorealisticScene(
    width: Float,
    height: Float,
    ambientFlash: Float,
    ambientColor: Color
) {
    // Sky gradient with dynamic lighting from fireworks
    val skyTop = Color(0xFF070B1E)
    val skyMid = Color(0xFF101938)
    val skyBottom = Color(0xFF1A274E)

    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(skyTop, skyMid, skyBottom),
            startY = 0f,
            endY = height * 0.78f
        ),
        size = Size(width, height)
    )

    // Ambient colored glow in the sky
    if (ambientFlash > 0.01f) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    ambientColor.copy(alpha = ambientFlash * 0.45f),
                    Color.Transparent
                ),
                center = Offset(width / 2, height * 0.65f),
                radius = width * 0.9f
            ),
            radius = width * 0.9f,
            center = Offset(width / 2, height * 0.65f),
            blendMode = BlendMode.Plus
        )
    }

    // Twinkling stars in sky
    val rand = Random(42)
    repeat(35) {
        val sx = rand.nextFloat() * width
        val sy = rand.nextFloat() * height * 0.48f
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = rand.nextFloat() * 1.8f + 1f,
            center = Offset(sx, sy)
        )
    }

    // Taj Mahal / Monument Silhouette
    val horizonY = height * 0.72f
    val baseSilhouette = Color(0xFF0F172E)
    val litSilhouette = Color(0xFF2E3E66)
    val silhouetteColor = if (ambientFlash > 0.05f) litSilhouette else baseSilhouette

    // Central Dome
    drawCircle(
        color = silhouetteColor,
        radius = width * 0.17f,
        center = Offset(width / 2, horizonY - 45f)
    )
    // Left & Right Minarets
    drawRect(
        color = silhouetteColor,
        topLeft = Offset(width * 0.12f, horizonY - 105f),
        size = Size(18f, 105f)
    )
    drawRect(
        color = silhouetteColor,
        topLeft = Offset(width * 0.84f, horizonY - 105f),
        size = Size(18f, 105f)
    )

    // Ground Floor Pathway
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF161F33), Color(0xFF0B101C)),
            startY = horizonY,
            endY = height
        ),
        topLeft = Offset(0f, horizonY),
        size = Size(width, height - horizonY)
    )
}

// Stone Pedestal with realistic shadow and ground burn scorch mark
private fun DrawScope.drawPedestalWithScorch(center: Offset, scorchRadius: Float) {
    val pedWidth = 144f
    val pedHeight = 24f

    // Shadow on ground
    drawOval(
        color = Color(0x77000000),
        topLeft = Offset(center.x - pedWidth * 0.68f, center.y + 6f),
        size = Size(pedWidth * 1.36f, 18f)
    )

    // Pedestal Stone Base
    drawRoundRect(
        color = Color(0xFF37474F),
        topLeft = Offset(center.x - pedWidth / 2, center.y),
        size = Size(pedWidth, pedHeight),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Top Rim Bevel Highlight
    drawRoundRect(
        color = Color(0xFF546E7A),
        topLeft = Offset(center.x - pedWidth / 2 + 4f, center.y - 4f),
        size = Size(pedWidth - 8f, 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )

    // Charred Black Scorch Mark from Bomb Detonation
    if (scorchRadius > 1f) {
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xDD000000), Color(0x66212121), Color.Transparent),
                center = Offset(center.x, center.y - 2f),
                radius = scorchRadius
            ),
            topLeft = Offset(center.x - scorchRadius, center.y - 8f),
            size = Size(scorchRadius * 2f, 16f)
        )
    }
}

// Realistic Cracker Models with Detailed Fuse Combustion
private fun DrawScope.drawCrackerStageDetailed(
    center: Offset,
    cracker: CrackerItem,
    stage: BurstStage,
    fuseProgress: Float,
    chakriAngle: Float,
    level: Int = 1
) {
    when (cracker.type) {
        CrackerType.SUTLI_BOMB -> {
            drawSutliBombStage(
                center = center,
                stage = stage,
                fuseProgress = fuseProgress,
                level = level
            )
        }

        CrackerType.CHAKRI -> {
            rotate(chakriAngle, pivot = Offset(center.x, center.y - 12f)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFFFFB300), Color(0xFFE65100)),
                        center = Offset(center.x, center.y - 12f),
                        radius = 42f
                    ),
                    radius = 42f,
                    center = Offset(center.x, center.y - 12f)
                )
                drawCircle(
                    color = Color(0xFFFFD54F),
                    radius = 42f,
                    center = Offset(center.x, center.y - 12f),
                    style = Stroke(width = 4.5f)
                )
                // Center hub
                drawCircle(
                    color = Color(0xFFD50000),
                    radius = 12f,
                    center = Offset(center.x, center.y - 12f)
                )
            }

            drawRealisticCombustionFuse(
                base = Offset(center.x + 30f, center.y - 16f),
                tipTarget = Offset(center.x + 46f, center.y - 38f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.ANAR -> {
            val potWidth = 76f
            val potHeight = 88f
            val potTop = center.y - potHeight

            val path = Path().apply {
                moveTo(center.x - 14f, potTop)
                lineTo(center.x + 14f, potTop)
                lineTo(center.x + potWidth / 2, center.y)
                lineTo(center.x - potWidth / 2, center.y)
                close()
            }
            drawPath(path, Color(0xFFC2185B))
            drawLine(
                color = GoldYellow,
                start = Offset(center.x - 26f, center.y - 36f),
                end = Offset(center.x + 26f, center.y - 36f),
                strokeWidth = 7f
            )

            drawRealisticCombustionFuse(
                base = Offset(center.x, potTop),
                tipTarget = Offset(center.x + 16f, potTop - 28f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.ROCKET -> {
            val bodyTop = center.y - 78f
            drawLine(
                color = Color(0xFFBCAAA4),
                start = Offset(center.x, bodyTop + 20f),
                end = Offset(center.x, center.y + 40f),
                strokeWidth = 3.5f
            )
            drawRoundRect(
                color = Color(0xFF6A1B9A),
                topLeft = Offset(center.x - 13f, bodyTop),
                size = Size(26f, 50f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            val cone = Path().apply {
                moveTo(center.x, bodyTop - 24f)
                lineTo(center.x + 16f, bodyTop)
                lineTo(center.x - 16f, bodyTop)
                close()
            }
            drawPath(cone, GoldYellow)

            // Rocket evolutions per level
            if (level >= 5) {
                val leftFin = Path().apply {
                    moveTo(center.x - 13f, bodyTop + 32f)
                    lineTo(center.x - 22f, bodyTop + 48f)
                    lineTo(center.x - 13f, bodyTop + 48f)
                    close()
                }
                val rightFin = Path().apply {
                    moveTo(center.x + 13f, bodyTop + 32f)
                    lineTo(center.x + 22f, bodyTop + 48f)
                    lineTo(center.x + 13f, bodyTop + 48f)
                    close()
                }
                drawPath(leftFin, Color(0xFFFF6D00))
                drawPath(rightFin, Color(0xFFFF6D00))
            }
            if (level >= 12) {
                drawLine(
                    color = GoldYellow,
                    start = Offset(center.x - 13f, bodyTop + 18f),
                    end = Offset(center.x + 13f, bodyTop + 18f),
                    strokeWidth = 3f
                )
                drawLine(
                    color = GoldYellow,
                    start = Offset(center.x - 13f, bodyTop + 28f),
                    end = Offset(center.x + 13f, bodyTop + 28f),
                    strokeWidth = 3f
                )
            }
            if (level >= 20) {
                drawRoundRect(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(center.x - 20f, bodyTop + 16f),
                    size = Size(6f, 30f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
                drawRoundRect(
                    color = Color(0xFFFFD54F),
                    topLeft = Offset(center.x + 14f, bodyTop + 16f),
                    size = Size(6f, 30f),
                    cornerRadius = CornerRadius(2f, 2f)
                )
            }

            drawRealisticCombustionFuse(
                base = Offset(center.x, bodyTop + 50f),
                tipTarget = Offset(center.x + 18f, bodyTop + 72f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.PHOOLJHADI -> {
            drawLine(
                color = Color(0xFF90A4AE),
                start = Offset(center.x - 15f, center.y + 20f),
                end = Offset(center.x + 15f, center.y - 80f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )
            drawRealisticCombustionFuse(
                base = Offset(center.x + 15f, center.y - 80f),
                tipTarget = Offset(center.x + 24f, center.y - 96f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.PENCIL_WHIP -> {
            val stickHeight = 110f
            val stickWidth = 14f
            val stickTop = center.y - stickHeight

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF8E24AA), Color(0xFF4A148C))
                ),
                topLeft = Offset(center.x - stickWidth / 2, stickTop + 14f),
                size = Size(stickWidth, stickHeight - 14f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            for (i in 0 until 5) {
                val y = stickTop + 24f + i * 16f
                drawLine(
                    color = GoldYellow,
                    start = Offset(center.x - stickWidth / 2, y),
                    end = Offset(center.x + stickWidth / 2, y + 6f),
                    strokeWidth = 3f
                )
            }

            val tipPath = Path().apply {
                moveTo(center.x, stickTop)
                lineTo(center.x + stickWidth / 2, stickTop + 14f)
                lineTo(center.x - stickWidth / 2, stickTop + 14f)
                close()
            }
            drawPath(tipPath, Color(0xFFFFD54F))

            drawRealisticCombustionFuse(
                base = Offset(center.x, stickTop),
                tipTarget = Offset(center.x + 14f, stickTop - 24f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.PAPER_BOMB, CrackerType.LAXMI_BOMB -> {
            val bombSize = 68f
            val bombTop = center.y - bombSize

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFE53935), Color(0xFFB71C1C))
                ),
                topLeft = Offset(center.x - bombSize / 2, bombTop),
                size = Size(bombSize, bombSize),
                cornerRadius = CornerRadius(8f, 8f)
            )

            drawRoundRect(
                color = GoldYellow,
                topLeft = Offset(center.x - bombSize / 2 + 5f, bombTop + 5f),
                size = Size(bombSize - 10f, bombSize - 10f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2.5f)
            )

            drawLine(
                color = Color(0xFFFFD54F),
                start = Offset(center.x, bombTop),
                end = Offset(center.x, center.y),
                strokeWidth = 4f
            )
            drawLine(
                color = Color(0xFFFFD54F),
                start = Offset(center.x - bombSize / 2, bombTop + bombSize / 2),
                end = Offset(center.x + bombSize / 2, bombTop + bombSize / 2),
                strokeWidth = 4f
            )

            drawCircle(color = GoldYellow, radius = 13f, center = Offset(center.x, bombTop + bombSize / 2))
            drawCircle(color = Color(0xFFD50000), radius = 10f, center = Offset(center.x, bombTop + bombSize / 2))

            drawRealisticCombustionFuse(
                base = Offset(center.x, bombTop),
                tipTarget = Offset(center.x + 16f, bombTop - 26f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.LADI -> {
            val garlandWidth = 56f
            val garlandHeight = 120f
            val topY = center.y - garlandHeight

            drawLine(
                color = Color(0xFF8D6E63),
                start = Offset(center.x, topY - 12f),
                end = Offset(center.x, center.y),
                strokeWidth = 3.5f
            )

            val rows = 7
            for (r in 0 until rows) {
                val y = topY + r * 16f
                drawRoundRect(
                    color = Color(0xFFD32F2F),
                    topLeft = Offset(center.x - garlandWidth / 2, y),
                    size = Size(garlandWidth / 2 - 3f, 10f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRect(
                    color = GoldYellow,
                    topLeft = Offset(center.x - garlandWidth / 2, y + 2f),
                    size = Size(5f, 6f)
                )

                drawRoundRect(
                    color = Color(0xFFD32F2F),
                    topLeft = Offset(center.x + 3f, y),
                    size = Size(garlandWidth / 2 - 3f, 10f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
                drawRect(
                    color = GoldYellow,
                    topLeft = Offset(center.x + garlandWidth / 2 - 5f, y + 2f),
                    size = Size(5f, 6f)
                )
            }

            drawRealisticCombustionFuse(
                base = Offset(center.x, topY - 10f),
                tipTarget = Offset(center.x + 18f, topY - 30f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.SNAKE_TABLET -> {
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF78909C), Color(0xFF37474F)),
                    center = center,
                    radius = 58f
                ),
                topLeft = Offset(center.x - 56f, center.y - 12f),
                size = Size(112f, 24f)
            )

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF424242), Color(0xFF212121), Color(0xFF000000)),
                    center = Offset(center.x, center.y - 8f),
                    radius = 24f
                ),
                radius = 24f,
                center = Offset(center.x, center.y - 8f)
            )

            drawRealisticCombustionFuse(
                base = Offset(center.x, center.y - 8f),
                tipTarget = Offset(center.x + 16f, center.y - 28f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.COLOR_SPARKLER -> {
            drawLine(
                color = Color(0xFFB0BEC5),
                start = Offset(center.x - 18f, center.y + 20f),
                end = Offset(center.x + 14f, center.y - 75f),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )

            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF00E676), Color(0xFFFF1744), Color(0xFF00E5FF)),
                    start = Offset(center.x - 4f, center.y - 15f),
                    end = Offset(center.x + 14f, center.y - 75f)
                ),
                start = Offset(center.x - 4f, center.y - 15f),
                end = Offset(center.x + 14f, center.y - 75f),
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )

            drawRealisticCombustionFuse(
                base = Offset(center.x + 14f, center.y - 75f),
                tipTarget = Offset(center.x + 22f, center.y - 94f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.AERIAL_SHELL -> {
            val mortarWidth = 58f
            val mortarHeight = 90f
            val mortarTop = center.y - mortarHeight

            drawRoundRect(
                color = Color(0xFF4E342E),
                topLeft = Offset(center.x - 44f, center.y - 14f),
                size = Size(88f, 18f),
                cornerRadius = CornerRadius(4f, 4f)
            )

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1), Color(0xFF1976D2))
                ),
                topLeft = Offset(center.x - mortarWidth / 2, mortarTop),
                size = Size(mortarWidth, mortarHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            drawLine(
                color = GoldYellow,
                start = Offset(center.x - mortarWidth / 2, mortarTop + 18f),
                end = Offset(center.x + mortarWidth / 2, mortarTop + 18f),
                strokeWidth = 4f
            )
            drawLine(
                color = GoldYellow,
                start = Offset(center.x - mortarWidth / 2, center.y - 28f),
                end = Offset(center.x + mortarWidth / 2, center.y - 28f),
                strokeWidth = 4f
            )

            drawCircle(
                color = Color(0xFFFFB300),
                radius = 18f,
                center = Offset(center.x, mortarTop)
            )

            drawRealisticCombustionFuse(
                base = Offset(center.x + 8f, mortarTop),
                tipTarget = Offset(center.x + 24f, mortarTop - 26f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.WHIZ_WHEEL -> {
            rotate(chakriAngle, pivot = Offset(center.x, center.y - 12f)) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF40C4FF), Color(0xFF0288D1)),
                        center = Offset(center.x, center.y - 12f),
                        radius = 36f
                    ),
                    radius = 36f,
                    center = Offset(center.x, center.y - 12f)
                )

                for (i in 0 until 3) {
                    val angle = Math.toRadians(i * 120.0)
                    val wingEnd = Offset(
                        (center.x + 46f * cos(angle)).toFloat(),
                        (center.y - 12f + 46f * sin(angle)).toFloat()
                    )
                    drawLine(
                        color = Color(0xFFFFAB00),
                        start = Offset(center.x, center.y - 12f),
                        end = wingEnd,
                        strokeWidth = 9f,
                        cap = StrokeCap.Round
                    )
                    drawCircle(color = Color.White, radius = 5f, center = wingEnd)
                }
                drawCircle(color = Color(0xFFD50000), radius = 10f, center = Offset(center.x, center.y - 12f))
            }

            drawRealisticCombustionFuse(
                base = Offset(center.x + 28f, center.y - 14f),
                tipTarget = Offset(center.x + 44f, center.y - 36f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.ELECTRIC_FOUNTAIN -> {
            val fountainWidth = 54f
            val fountainHeight = 98f
            val fountainTop = center.y - fountainHeight

            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFB0BEC5), Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF90A4AE))
                ),
                topLeft = Offset(center.x - fountainWidth / 2, fountainTop),
                size = Size(fountainWidth, fountainHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            drawRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(center.x - fountainWidth / 2, center.y - 58f),
                size = Size(fountainWidth, 24f)
            )

            drawCircle(color = Color.White, radius = 7f, center = Offset(center.x, center.y - 46f))

            val conePath = Path().apply {
                moveTo(center.x, fountainTop - 12f)
                lineTo(center.x + fountainWidth / 2 - 4f, fountainTop)
                lineTo(center.x - fountainWidth / 2 + 4f, fountainTop)
                close()
            }
            drawPath(conePath, Color(0xFFECEFF1))

            drawRealisticCombustionFuse(
                base = Offset(center.x, fountainTop - 10f),
                tipTarget = Offset(center.x + 18f, fountainTop - 32f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.SKY_SHOTS -> {
            val boxWidth = 92f
            val boxHeight = 84f
            val boxTop = center.y - 34f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFF311B92))
                ),
                topLeft = Offset(center.x - boxWidth / 2f, boxTop),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(10f, 10f)
            )
            drawRoundRect(
                color = GoldYellow,
                topLeft = Offset(center.x - boxWidth / 2f, boxTop),
                size = Size(boxWidth, boxHeight),
                cornerRadius = CornerRadius(10f, 10f),
                style = Stroke(width = 2.2f)
            )
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFE082))
                ),
                topLeft = Offset(center.x - boxWidth / 2f, boxTop + 24f),
                size = Size(boxWidth, 20f)
            )
            val plateHeight = 24f
            drawOval(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF263238), Color(0xFF37474F))
                ),
                topLeft = Offset(center.x - boxWidth / 2f + 4f, boxTop - plateHeight / 2f),
                size = Size(boxWidth - 8f, plateHeight)
            )
            // 7 Tube Bores
            val tubeRadius = 5.2f
            drawCircle(color = Color(0xFF111111), radius = tubeRadius, center = Offset(center.x, boxTop))
            val rosetteRadiusX = 26f
            val rosetteRadiusY = 7.5f
            for (i in 0 until 6) {
                val rad = Math.toRadians(i * 60.0)
                val tx = (center.x + rosetteRadiusX * cos(rad)).toFloat()
                val ty = (boxTop + rosetteRadiusY * sin(rad)).toFloat()
                drawCircle(color = Color(0xFF111111), radius = tubeRadius, center = Offset(tx, ty))
            }

            drawRealisticCombustionFuse(
                base = Offset(center.x + boxWidth / 2f - 14f, boxTop - 2f),
                tipTarget = Offset(center.x + boxWidth / 2f + 10f, boxTop - 24f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }
    }
}

// Comprehensive 25-level procedural renderer for Sutli Bomb models across all 5 Phases
private fun DrawScope.drawSutliBombStage(
    center: Offset,
    stage: BurstStage,
    fuseProgress: Float,
    level: Int
) {
    val detail = SutliBombLevelConfig.getDetail(level)
    val widthPx = detail.widthDp * 1.35f
    val heightPx = detail.heightDp * 1.35f
    val bombTop = center.y - heightPx

    if (detail.isSpherical) {
        // Round circular Sutli bomb (Level 4)
        val radius = widthPx / 2f
        val ballCenter = Offset(center.x, center.y - radius)
        drawCircle(
            color = detail.bodyColor,
            radius = radius,
            center = ballCenter
        )
        // Criss-cross twine bands
        drawLine(
            color = detail.twineColor,
            start = Offset(ballCenter.x - radius, ballCenter.y),
            end = Offset(ballCenter.x + radius, ballCenter.y),
            strokeWidth = 4f
        )
        drawLine(
            color = detail.twineColor,
            start = Offset(ballCenter.x, ballCenter.y - radius),
            end = Offset(ballCenter.x, ballCenter.y + radius),
            strokeWidth = 4f
        )
        drawLine(
            color = detail.twineColor,
            start = Offset(ballCenter.x - radius * 0.7f, ballCenter.y - radius * 0.7f),
            end = Offset(ballCenter.x + radius * 0.7f, ballCenter.y + radius * 0.7f),
            strokeWidth = 3f
        )
        drawLine(
            color = detail.twineColor,
            start = Offset(ballCenter.x + radius * 0.7f, ballCenter.y - radius * 0.7f),
            end = Offset(ballCenter.x - radius * 0.7f, ballCenter.y + radius * 0.7f),
            strokeWidth = 3f
        )
        // Red badge
        drawCircle(
            color = Color(0xFFD50000),
            radius = radius * 0.35f,
            center = ballCenter
        )
        drawCircle(
            color = GoldYellow,
            radius = radius * 0.22f,
            center = ballCenter
        )
        // Fuse on top
        drawRealisticCombustionFuse(
            base = Offset(center.x, ballCenter.y - radius),
            tipTarget = Offset(center.x + 18f, ballCenter.y - radius - 30f),
            stage = stage,
            fuseProgress = fuseProgress
        )
    } else {
        // Cylindrical or Rounded Rect body
        val corner = if (detail.isElongated) 16f else if (detail.isWide) 8f else 12f
        drawRoundRect(
            color = detail.bodyColor,
            topLeft = Offset(center.x - widthPx / 2f, bombTop),
            size = Size(widthPx, heightPx),
            cornerRadius = CornerRadius(corner, corner)
        )

        // Twine wrapping lines
        val twineCount = if (detail.isElongated) 6 else if (detail.isWide) 4 else 5
        for (i in 1 until twineCount) {
            val y = bombTop + i * (heightPx / twineCount)
            drawLine(
                color = detail.twineColor,
                start = Offset(center.x - widthPx / 2f, y),
                end = Offset(center.x + widthPx / 2f, y),
                strokeWidth = if (detail.isBossMega) 5.5f else 3.5f
            )
        }
        // Vertical center twine
        drawLine(
            color = detail.twineColor,
            start = Offset(center.x, bombTop),
            end = Offset(center.x, bombTop + heightPx),
            strokeWidth = if (detail.isBossMega) 5.5f else 3.5f
        )

        // Special visual features per level type:
        if (detail.isNuclearStyle) {
            // Radiation Trefoil symbol in center
            val radCenter = Offset(center.x, bombTop + heightPx * 0.52f)
            drawCircle(color = Color.Black, radius = 16f, center = radCenter)
            drawCircle(color = Color(0xFFFFD600), radius = 6f, center = radCenter)
            for (ang in listOf(0f, 120f, 240f)) {
                drawArc(
                    color = Color(0xFFFFD600),
                    startAngle = ang - 30f,
                    sweepAngle = 60f,
                    useCenter = true,
                    topLeft = Offset(radCenter.x - 14f, radCenter.y - 14f),
                    size = Size(28f, 28f)
                )
            }
            drawCircle(color = Color.Black, radius = 5f, center = radCenter)
        } else if (detail.isRoxStyle) {
            // Industrial caution chevron stripes
            val tagTop = bombTop + heightPx * 0.35f
            drawRect(
                color = Color.Black,
                topLeft = Offset(center.x - widthPx * 0.38f, tagTop),
                size = Size(widthPx * 0.76f, heightPx * 0.30f)
            )
            for (c in -2..2) {
                drawLine(
                    color = Color(0xFFFF9800),
                    start = Offset(center.x + c * 16f - 8f, tagTop + heightPx * 0.30f),
                    end = Offset(center.x + c * 16f + 8f, tagTop),
                    strokeWidth = 3.5f
                )
            }
        } else if (detail.hasRocketCone) {
            // Rocket cone on top + side fins
            val conePath = Path().apply {
                moveTo(center.x, bombTop - 26f)
                lineTo(center.x - widthPx * 0.42f, bombTop)
                lineTo(center.x + widthPx * 0.42f, bombTop)
                close()
            }
            drawPath(path = conePath, color = Color(0xFFFF1744))
            // Left fin
            val leftFin = Path().apply {
                moveTo(center.x - widthPx / 2f, bombTop + heightPx * 0.65f)
                lineTo(center.x - widthPx / 2f - 16f, bombTop + heightPx)
                lineTo(center.x - widthPx / 2f, bombTop + heightPx)
                close()
            }
            drawPath(path = leftFin, color = Color(0xFFFFC107))
            // Right fin
            val rightFin = Path().apply {
                moveTo(center.x + widthPx / 2f, bombTop + heightPx * 0.65f)
                lineTo(center.x + widthPx / 2f + 16f, bombTop + heightPx)
                lineTo(center.x + widthPx / 2f, bombTop + heightPx)
                close()
            }
            drawPath(path = rightFin, color = Color(0xFFFFC107))
        } else if (detail.hasSideLadi) {
            // Twin red mini crackers dangling on sides
            drawRoundRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(center.x - widthPx / 2f - 12f, bombTop + heightPx * 0.4f),
                size = Size(10f, 22f),
                cornerRadius = CornerRadius(3f, 3f)
            )
            drawRoundRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(center.x + widthPx / 2f + 2f, bombTop + heightPx * 0.4f),
                size = Size(10f, 22f),
                cornerRadius = CornerRadius(3f, 3f)
            )
        } else if (detail.hasChemicalMark) {
            // Neon cyan cross chemical seal
            val midY = bombTop + heightPx * 0.5f
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(center.x - 18f, midY - 14f),
                end = Offset(center.x + 18f, midY + 14f),
                strokeWidth = 4f
            )
            drawLine(
                color = Color(0xFF00E5FF),
                start = Offset(center.x + 18f, midY - 14f),
                end = Offset(center.x - 18f, midY + 14f),
                strokeWidth = 4f
            )
        } else if (detail.isBossMega) {
            // Level 25 Boss: Royal Gold Crest & Crown Badge
            val bossTagTop = bombTop + heightPx * 0.32f
            drawRoundRect(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFFFFD700), Color(0xFFFFA000)),
                    center = Offset(center.x, bossTagTop + 18f),
                    radius = 28f
                ),
                topLeft = Offset(center.x - 26f, bossTagTop),
                size = Size(52f, 36f),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawCircle(color = Color.White, radius = 5f, center = Offset(center.x, bossTagTop + 18f))
            drawCircle(color = Color(0xFFD50000), radius = 2.5f, center = Offset(center.x, bossTagTop + 18f))
        } else if (detail.isGoldStyle) {
            // Level 20 Golden style: Gold metallic label tag
            val tagTop = bombTop + heightPx * 0.36f
            drawRoundRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFD700), Color(0xFFFFF9C4))
                ),
                topLeft = Offset(center.x - 22f, tagTop),
                size = Size(44f, 26f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawCircle(color = Color(0xFFD50000), radius = 4f, center = Offset(center.x, tagTop + 13f))
        } else {
            // Standard red/gold festive brand label
            val tagW = (widthPx * 0.55f).coerceIn(24f, 48f)
            val tagH = (heightPx * 0.32f).coerceIn(16f, 32f)
            val tagTop = bombTop + (heightPx - tagH) / 2f
            drawRect(
                color = Color(0xFFD50000),
                topLeft = Offset(center.x - tagW / 2f, tagTop),
                size = Size(tagW, tagH)
            )
            drawRect(
                color = GoldYellow,
                topLeft = Offset(center.x - tagW * 0.4f, tagTop + tagH * 0.15f),
                size = Size(tagW * 0.8f, tagH * 0.7f)
            )
        }

        // FUSE HANDLING
        val fuseBaseY = if (detail.hasRocketCone) bombTop - 26f else bombTop
        if (detail.isMultiFuse) {
            // Level 21: 3 branching fuses!
            drawRealisticCombustionFuse(
                base = Offset(center.x, fuseBaseY),
                tipTarget = Offset(center.x - 24f, fuseBaseY - 32f),
                stage = stage,
                fuseProgress = fuseProgress
            )
            drawRealisticCombustionFuse(
                base = Offset(center.x, fuseBaseY),
                tipTarget = Offset(center.x, fuseBaseY - 38f),
                stage = stage,
                fuseProgress = fuseProgress
            )
            drawRealisticCombustionFuse(
                base = Offset(center.x, fuseBaseY),
                tipTarget = Offset(center.x + 24f, fuseBaseY - 32f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        } else {
            drawRealisticCombustionFuse(
                base = Offset(center.x, fuseBaseY),
                tipTarget = Offset(center.x + 20f, fuseBaseY - 34f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }
    }
}

// Realistic Combustion Fuse with Ember Core and Sizzling Sparks
private fun DrawScope.drawRealisticCombustionFuse(
    base: Offset,
    tipTarget: Offset,
    stage: BurstStage,
    fuseProgress: Float
) {
    val currentTip = if (stage == BurstStage.FUSE_BURNING) {
        Offset(
            tipTarget.x + (base.x - tipTarget.x) * fuseProgress,
            tipTarget.y + (base.y - tipTarget.y) * fuseProgress
        )
    } else {
        tipTarget
    }

    // Fuse rope
    if (fuseProgress < 0.98f) {
        drawLine(
            color = Color(0xFF8D6E63),
            start = base,
            end = currentTip,
            strokeWidth = 4.5f,
            cap = StrokeCap.Round
        )
    }

    // Distinct Pulsating White/Gold Spark at Fuse Tip ("light white / bomb salagvani jagya")
    if (stage == BurstStage.READY) {
        val pulse = 1f + 0.25f * sin(System.currentTimeMillis() / 150.0).toFloat()
        // Outer soft glow
        drawCircle(
            color = Color(0xFFFFD54F).copy(alpha = 0.55f),
            radius = 14f * pulse,
            center = tipTarget
        )
        // Bright radiant white spark core
        drawCircle(
            color = Color.White,
            radius = 7f * pulse,
            center = tipTarget
        )
    }

    // Active Ember and Sizzling Sparks during lighting or burning
    if (stage == BurstStage.LIGHTING || stage == BurstStage.FUSE_BURNING) {
        // Red-hot incandescent ember with Plus blend mode
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, GoldYellow, FestiveOrange, Color.Transparent),
                center = currentTip,
                radius = 22f
            ),
            radius = 22f,
            center = currentTip,
            blendMode = BlendMode.Plus
        )

        // Micro-sparks sputtering from the burning fuse
        val rand = Random(System.nanoTime().toInt())
        repeat(8) {
            val angle = rand.nextDouble() * 2 * Math.PI
            val dist = rand.nextFloat() * 24f + 4f
            val sparkPos = Offset(
                (currentTip.x + dist * cos(angle)).toFloat(),
                (currentTip.y + dist * sin(angle)).toFloat()
            )
            drawLine(
                color = if (rand.nextBoolean()) GoldYellow else Color.White,
                start = currentTip,
                end = sparkPos,
                strokeWidth = 2.2f,
                blendMode = BlendMode.Plus
            )
        }
    }
}

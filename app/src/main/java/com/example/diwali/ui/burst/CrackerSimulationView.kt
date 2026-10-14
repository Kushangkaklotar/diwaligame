package com.example.diwali.ui.burst

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
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
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerType
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
    SHOCKWAVE
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
    onBurstComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
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

    // Reset state
    LaunchedEffect(stage) {
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
        }
    }

    // High-performance 60fps simulation loop
    LaunchedEffect(stage) {
        if (stage == BurstStage.BURSTING) {
            particles.clear()
            burstElapsedMs = 0f
            bombDetonated = false
            var lastTime = 0L
            while (stage == BurstStage.BURSTING) {
                withFrameNanos { now ->
                    if (lastTime == 0L) lastTime = now
                    val dt = ((now - lastTime) / 1_000_000f).coerceIn(1f, 32f)
                    lastTime = now
                    burstElapsedMs += dt

                    val rand = Random(now.toInt())

                    // Ambient flash decay
                    if (ambientFlashAlpha > 0f) {
                        ambientFlashAlpha = (ambientFlashAlpha - dt * 0.006f).coerceAtLeast(0f)
                    }

                    // Screen shake damping
                    screenShakeX *= 0.88f
                    screenShakeY *= 0.88f

                    // -------------------------------------------------------------
                    // PARTICLE GENERATION PER CRACKER TYPE
                    // -------------------------------------------------------------
                    when (cracker.type) {
                        CrackerType.SUTLI_BOMB -> {
                            // Instantaneous detonation: Initial flash & violent explosion
                            if (!bombDetonated) {
                                bombDetonated = true
                                ambientFlashAlpha = 1.0f
                                scorchMarkRadius = 68f
                                screenShakeX = (rand.nextFloat() - 0.5f) * 44f
                                screenShakeY = (rand.nextFloat() - 0.5f) * 44f

                                // 1. Shockwave ring expanding rapidly
                                particles.add(
                                    RealisticParticle(
                                        type = ParticleType.SHOCKWAVE,
                                        x = 0f,
                                        y = -35f,
                                        vx = 0f,
                                        vy = 0f,
                                        color = Color(0xFFFFD54F),
                                        radius = 12f,
                                        maxRadius = 380f,
                                        alpha = 1.0f,
                                        decay = 0.026f
                                    )
                                )

                                // 2. Additive Fireball Cloud (Inner hot white core + outer fireball)
                                repeat(48) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 16f + 2f
                                    val firePalette = listOf(
                                        Color(0xFFFFFFFF),
                                        Color(0xFFFFF9C4),
                                        Color(0xFFFFEB3B),
                                        Color(0xFFFF9800),
                                        Color(0xFFFF1744)
                                    )
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
                                            gravity = -0.08f // heat rises
                                        )
                                    )
                                }

                                // 3. Flying Charred Debris & Spark Needles
                                repeat(85) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 25f + 4f
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = 0f,
                                            y = -35f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 5f,
                                            color = if (rand.nextBoolean()) Color(0xFFFFD54F) else Color(0xFFFF3D00),
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
                                repeat(32) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 6f + 1f
                                    val gray = rand.nextInt(45) + 35
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SMOKE_PUFF,
                                            x = 0f,
                                            y = -35f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() - 4.5f,
                                            color = Color(gray, gray, gray, 190),
                                            radius = rand.nextFloat() * 24f + 16f,
                                            alpha = 0.9f,
                                            decay = 0.009f,
                                            drag = 0.96f,
                                            gravity = -0.06f
                                        )
                                    )
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
                                // Rocket ascends with increasing velocity
                                rocketVelocityY += dt * 0.055f + 0.45f
                                rocketAltitude += rocketVelocityY

                                // Exhaust thrust sparks & smoke trailing below rocket
                                repeat(7) {
                                    particles.add(
                                        RealisticParticle(
                                            type = ParticleType.SPARK_TRAIL,
                                            x = (rand.nextFloat() - 0.5f) * 6f,
                                            y = -rocketAltitude + 20f,
                                            vx = (rand.nextFloat() - 0.5f) * 3f,
                                            vy = rand.nextFloat() * 8f + 4f,
                                            color = Color(0xFFFFD54F),
                                            radius = rand.nextFloat() * 3.5f + 1.5f,
                                            alpha = 0.9f,
                                            decay = 0.045f,
                                            trailLength = 3.6f
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
                                        vy = 1.2f,
                                        color = Color(0x66B0BEC5),
                                        radius = rand.nextFloat() * 14f + 8f,
                                        alpha = 0.65f,
                                        decay = 0.020f
                                    )
                                )

                                // Reached sky apex (detonation!)
                                if (rocketAltitude > 520f) {
                                    rocketExploded = true
                                    ambientFlashAlpha = 0.95f
                                    screenShakeX = (rand.nextFloat() - 0.5f) * 22f
                                    screenShakeY = (rand.nextFloat() - 0.5f) * 22f

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
                                            maxRadius = 340f,
                                            alpha = 1f,
                                            decay = 0.032f
                                        )
                                    )

                                    // Spherical Aerial Chrysanthemum Bloom (160 stars)
                                    repeat(160) {
                                        val angle = rand.nextDouble() * 2 * Math.PI
                                        val speed = rand.nextFloat() * 16f + 3f
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
                                                decay = rand.nextFloat() * 0.012f + 0.011f,
                                                gravity = 0.14f,
                                                drag = 0.968f,
                                                trailLength = 4.8f
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

                        if (p.type == ParticleType.SHOCKWAVE) {
                            p.radius += (p.maxRadius - p.radius) * 0.22f
                        } else if (p.type == ParticleType.FIREBALL || p.type == ParticleType.SMOKE_PUFF) {
                            p.radius += 0.8f // expansion
                        }

                        if (p.alpha <= 0f) {
                            iterator.remove()
                        }
                    }

                    // Check completion
                    val durationLimit = when (cracker.type) {
                        CrackerType.SUTLI_BOMB -> 2400f
                        CrackerType.ROCKET -> 3000f
                        CrackerType.CHAKRI -> 4200f
                        CrackerType.ANAR -> 5200f
                        CrackerType.PHOOLJHADI -> 4000f
                    }

                    if (burstElapsedMs > durationLimit && particles.isEmpty()) {
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

            // 2. Pedestal / Stand Center Platform
            val pedestalCenter = Offset(width / 2 + screenShakeX, height * 0.76f + screenShakeY)
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

            // 3. Cracker Body on Pedestal (disappears when bomb detonates or rocket flies)
            val showCrackerBody = when (cracker.type) {
                CrackerType.SUTLI_BOMB -> !bombDetonated
                CrackerType.ROCKET -> !rocketExploded
                else -> true
            }

            if (showCrackerBody) {
                val crackerY = if (cracker.type == CrackerType.ROCKET && stage == BurstStage.BURSTING) {
                    pedestalCenter.y - rocketAltitude
                } else {
                    pedestalCenter.y
                }

                drawCrackerStageDetailed(
                    center = Offset(pedestalCenter.x, crackerY),
                    cracker = cracker,
                    stage = stage,
                    fuseProgress = fuseBurnProgress,
                    chakriAngle = chakriAngle
                )
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

            // Step B: Draw Shockwave rings
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

            // Step C: Draw Additive Fireballs (Glowing overlapping fire)
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

            // Step D: Draw Spark Trails (Needle sparks pointing in velocity direction with Additive Glow!)
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
    chakriAngle: Float
) {
    when (cracker.type) {
        CrackerType.SUTLI_BOMB -> {
            val bombWidth = 66f
            val bombHeight = 74f
            val bombTop = center.y - bombHeight

            // Green textured body
            drawRoundRect(
                color = Color(0xFF2E7D32),
                topLeft = Offset(center.x - bombWidth / 2, bombTop),
                size = Size(bombWidth, bombHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Twine wrapping lines
            for (i in 1..4) {
                val y = bombTop + i * (bombHeight / 5)
                drawLine(
                    color = Color(0xFF1B5E20),
                    start = Offset(center.x - bombWidth / 2, y),
                    end = Offset(center.x + bombWidth / 2, y),
                    strokeWidth = 3.5f
                )
            }
            // Red festive label tag
            drawRect(
                color = Color(0xFFD50000),
                topLeft = Offset(center.x - 18f, bombTop + 24f),
                size = Size(36f, 24f)
            )
            drawRect(
                color = GoldYellow,
                topLeft = Offset(center.x - 14f, bombTop + 28f),
                size = Size(28f, 16f)
            )

            // Burning Fuse on top
            drawRealisticCombustionFuse(
                base = Offset(center.x, bombTop),
                tipTarget = Offset(center.x + 20f, bombTop - 32f),
                stage = stage,
                fuseProgress = fuseProgress
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

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

private data class SimParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var radius: Float,
    var alpha: Float,
    var decay: Float,
    var gravity: Float = 0f
)

@Composable
fun CrackerSimulationView(
    cracker: CrackerItem,
    stage: BurstStage,
    fuseBurnProgress: Float, // 0f (full) to 1f (burnt down)
    onBurstComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val particles = remember { mutableStateListOf<SimParticle>() }
    var burstElapsedMs by remember { mutableFloatStateOf(0f) }
    var screenShakeX by remember { mutableFloatStateOf(0f) }
    var screenShakeY by remember { mutableFloatStateOf(0f) }

    // Chakri spinning angle
    var chakriAngle by remember { mutableFloatStateOf(0f) }
    // Rocket ascent position
    var rocketAltitude by remember { mutableFloatStateOf(0f) }
    var rocketExploded by remember { mutableStateOf(false) }

    // Reset when stage becomes ready
    LaunchedEffect(stage) {
        if (stage == BurstStage.READY) {
            particles.clear()
            burstElapsedMs = 0f
            screenShakeX = 0f
            screenShakeY = 0f
            chakriAngle = 0f
            rocketAltitude = 0f
            rocketExploded = false
        }
    }

    // Animation frame loop
    LaunchedEffect(stage) {
        if (stage == BurstStage.BURSTING) {
            var lastTime = 0L
            while (stage == BurstStage.BURSTING) {
                withFrameNanos { now ->
                    if (lastTime == 0L) lastTime = now
                    val dt = ((now - lastTime) / 1_000_000f).coerceAtMost(32f)
                    lastTime = now
                    burstElapsedMs += dt

                    val rand = Random(now.toInt())

                    // 1. Particle Generation based on cracker type
                    when (cracker.type) {
                        CrackerType.SUTLI_BOMB -> {
                            // Initial massive fireball in first 500ms
                            if (burstElapsedMs < 400f && particles.size < 120) {
                                screenShakeX = (rand.nextFloat() - 0.5f) * 22f
                                screenShakeY = (rand.nextFloat() - 0.5f) * 22f

                                repeat(8) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 12f + 2f
                                    val colors = listOf(Color.White, GoldYellow, FestiveOrange, Color(0xFFFF1744), Color(0xFF424242))
                                    particles.add(
                                        SimParticle(
                                            x = 0f,
                                            y = 0f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat(),
                                            color = colors[rand.nextInt(colors.size)],
                                            radius = rand.nextFloat() * 14f + 6f,
                                            alpha = 1f,
                                            decay = rand.nextFloat() * 0.025f + 0.015f,
                                            gravity = 0.15f
                                        )
                                    )
                                }
                            } else {
                                screenShakeX = 0f
                                screenShakeY = 0f
                            }
                        }

                        CrackerType.CHAKRI -> {
                            chakriAngle = (chakriAngle + dt * 1.5f) % 360f
                            if (burstElapsedMs < 3200f) {
                                repeat(5) {
                                    val angle = Math.toRadians((chakriAngle + rand.nextInt(360)).toDouble())
                                    val speed = rand.nextFloat() * 9f + 4f
                                    particles.add(
                                        SimParticle(
                                            x = 0f,
                                            y = 0f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat() * 0.45f, // flat ground disc
                                            color = if (rand.nextBoolean()) GoldYellow else FestiveOrange,
                                            radius = rand.nextFloat() * 4f + 2f,
                                            alpha = 1f,
                                            decay = 0.045f,
                                            gravity = 0.08f
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.ANAR -> {
                            if (burstElapsedMs < 3500f) {
                                repeat(7) {
                                    val spread = (rand.nextFloat() - 0.5f) * 6f
                                    val vy = -(rand.nextFloat() * 14f + 10f)
                                    val colors = listOf(Color.White, GoldYellow, Color(0xFFFF4081), Color(0xFF00E5FF))
                                    particles.add(
                                        SimParticle(
                                            x = spread * 2f,
                                            y = -20f,
                                            vx = spread,
                                            vy = vy,
                                            color = colors[rand.nextInt(colors.size)],
                                            radius = rand.nextFloat() * 4.5f + 2f,
                                            alpha = 1f,
                                            decay = 0.022f,
                                            gravity = 0.35f // falls back down gracefully
                                        )
                                    )
                                }
                            }
                        }

                        CrackerType.ROCKET -> {
                            if (!rocketExploded) {
                                rocketAltitude += dt * 0.9f
                                // Tail sparks
                                repeat(3) {
                                    particles.add(
                                        SimParticle(
                                            x = (rand.nextFloat() - 0.5f) * 8f,
                                            y = -rocketAltitude + 30f,
                                            vx = (rand.nextFloat() - 0.5f) * 2f,
                                            vy = rand.nextFloat() * 4f + 2f,
                                            color = GoldYellow,
                                            radius = rand.nextFloat() * 3.5f + 1.5f,
                                            alpha = 0.9f,
                                            decay = 0.06f
                                        )
                                    )
                                }

                                if (rocketAltitude > 520f) {
                                    rocketExploded = true
                                    // High aerial sky burst explosion!
                                    screenShakeX = (rand.nextFloat() - 0.5f) * 14f
                                    screenShakeY = (rand.nextFloat() - 0.5f) * 14f
                                    repeat(80) {
                                        val angle = rand.nextDouble() * 2 * Math.PI
                                        val speed = rand.nextFloat() * 10f + 2f
                                        val colors = listOf(Color.White, Color(0xFFFF4081), Color(0xFF00E5FF), GoldYellow, Color(0xFF76FF03))
                                        particles.add(
                                            SimParticle(
                                                x = 0f,
                                                y = -rocketAltitude,
                                                vx = (speed * cos(angle)).toFloat(),
                                                vy = (speed * sin(angle)).toFloat(),
                                                color = colors[rand.nextInt(colors.size)],
                                                radius = rand.nextFloat() * 5f + 2f,
                                                alpha = 1f,
                                                decay = 0.022f,
                                                gravity = 0.12f
                                            )
                                        )
                                    }
                                }
                            } else {
                                screenShakeX = 0f
                                screenShakeY = 0f
                            }
                        }

                        CrackerType.PHOOLJHADI -> {
                            if (burstElapsedMs < 3000f) {
                                repeat(6) {
                                    val angle = rand.nextDouble() * 2 * Math.PI
                                    val speed = rand.nextFloat() * 7f + 2f
                                    particles.add(
                                        SimParticle(
                                            x = 0f,
                                            y = -30f,
                                            vx = (speed * cos(angle)).toFloat(),
                                            vy = (speed * sin(angle)).toFloat(),
                                            color = if (rand.nextBoolean()) GoldYellow else Color.White,
                                            radius = rand.nextFloat() * 3.5f + 1.5f,
                                            alpha = 1f,
                                            decay = 0.05f
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // 2. Update existing particles
                    val iterator = particles.iterator()
                    while (iterator.hasNext()) {
                        val p = iterator.next()
                        p.x += p.vx
                        p.y += p.vy
                        p.vy += p.gravity
                        p.alpha -= p.decay
                        if (p.alpha <= 0f) {
                            iterator.remove()
                        }
                    }

                    // Completion check
                    val durationLimit = when (cracker.type) {
                        CrackerType.SUTLI_BOMB -> 2200f
                        CrackerType.ROCKET -> 2800f
                        else -> 3600f
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

            // 1. Scenic Night Sky & Background Monument (like in the video)
            drawNightScene(width, height)

            // 2. Pedestal / Stand Center Platform
            val pedestalCenter = Offset(width / 2 + screenShakeX, height * 0.76f + screenShakeY)
            drawPedestal(pedestalCenter)

            // 3. Cracker on Pedestal (only visible if not exploded in bomb or rocket flight)
            val showCrackerBody = when (cracker.type) {
                CrackerType.SUTLI_BOMB -> stage != BurstStage.BURSTING || burstElapsedMs < 80f
                CrackerType.ROCKET -> !rocketExploded
                else -> true
            }

            if (showCrackerBody) {
                val crackerY = if (cracker.type == CrackerType.ROCKET && stage == BurstStage.BURSTING) {
                    pedestalCenter.y - rocketAltitude
                } else {
                    pedestalCenter.y
                }

                drawCrackerStage(
                    center = Offset(pedestalCenter.x, crackerY),
                    cracker = cracker,
                    stage = stage,
                    fuseProgress = fuseBurnProgress,
                    chakriAngle = chakriAngle
                )
            }

            // 4. Draw Simulation Particles relative to pedestal
            particles.forEach { p ->
                val px = pedestalCenter.x + p.x
                val py = pedestalCenter.y + p.y
                drawCircle(
                    color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                    radius = p.radius,
                    center = Offset(px, py)
                )
            }
        }
    }
}

// Background Night Scene with Monument Silhouette
private fun DrawScope.drawNightScene(width: Float, height: Float) {
    // Sky gradient
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xFF090D22),
                Color(0xFF131D3F),
                Color(0xFF1F2B56)
            ),
            startY = 0f,
            endY = height * 0.8f
        ),
        size = Size(width, height)
    )

    // Twinkling stars in sky
    val rand = Random(123)
    repeat(25) {
        drawCircle(
            color = Color.White.copy(alpha = 0.7f),
            radius = rand.nextFloat() * 1.8f + 1f,
            center = Offset(rand.nextFloat() * width, rand.nextFloat() * height * 0.45f)
        )
    }

    // Taj Mahal / Monument Silhouette in distance
    val horizonY = height * 0.72f
    val monumentColor = Color(0x33101830)

    // Central Dome
    drawCircle(
        color = monumentColor,
        radius = width * 0.18f,
        center = Offset(width / 2, horizonY - 40f)
    )
    // Left & Right Minarets
    drawRect(
        color = monumentColor,
        topLeft = Offset(width * 0.14f, horizonY - 90f),
        size = Size(16f, 90f)
    )
    drawRect(
        color = monumentColor,
        topLeft = Offset(width * 0.82f, horizonY - 90f),
        size = Size(16f, 90f)
    )

    // Ground Pathway / Floor
    drawRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF1B243B), Color(0xFF0F1524)),
            startY = horizonY,
            endY = height
        ),
        topLeft = Offset(0f, horizonY),
        size = Size(width, height - horizonY)
    )
}

// Stone/Wooden Pedestal Platform
private fun DrawScope.drawPedestal(center: Offset) {
    val pedWidth = 140f
    val pedHeight = 22f

    // Shadow on ground
    drawOval(
        color = Color(0x66000000),
        topLeft = Offset(center.x - pedWidth * 0.65f, center.y + 6f),
        size = Size(pedWidth * 1.3f, 16f)
    )

    // Pedestal Block
    drawRoundRect(
        color = Color(0xFF37474F),
        topLeft = Offset(center.x - pedWidth / 2, center.y),
        size = Size(pedWidth, pedHeight),
        cornerRadius = CornerRadius(6f, 6f)
    )
    // Pedestal Top Rim Highlight
    drawRoundRect(
        color = Color(0xFF546E7A),
        topLeft = Offset(center.x - pedWidth / 2 + 4f, center.y - 4f),
        size = Size(pedWidth - 8f, 8f),
        cornerRadius = CornerRadius(4f, 4f)
    )
}

// Draw the specific cracker on the pedestal with fuse
private fun DrawScope.drawCrackerStage(
    center: Offset,
    cracker: CrackerItem,
    stage: BurstStage,
    fuseProgress: Float,
    chakriAngle: Float
) {
    when (cracker.type) {
        CrackerType.SUTLI_BOMB -> {
            val bombWidth = 64f
            val bombHeight = 72f
            val bombTop = center.y - bombHeight

            // Bomb body
            drawRoundRect(
                color = Color(0xFF2E7D32),
                topLeft = Offset(center.x - bombWidth / 2, bombTop),
                size = Size(bombWidth, bombHeight),
                cornerRadius = CornerRadius(12f, 12f)
            )
            // Twine wrapping
            for (i in 1..3) {
                drawLine(
                    color = Color(0xFF1B5E20),
                    start = Offset(center.x - bombWidth / 2, bombTop + i * (bombHeight / 4)),
                    end = Offset(center.x + bombWidth / 2, bombTop + i * (bombHeight / 4)),
                    strokeWidth = 3f
                )
            }
            // Red sticker tag
            drawRect(
                color = Color(0xFFD50000),
                topLeft = Offset(center.x - 16f, bombTop + 24f),
                size = Size(32f, 22f)
            )

            // Fuse on top
            drawFuse(
                base = Offset(center.x, bombTop),
                tipTarget = Offset(center.x + 18f, bombTop - 28f),
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
                    style = Stroke(width = 4f)
                )
                // Center hub
                drawCircle(
                    color = Color(0xFFD50000),
                    radius = 12f,
                    center = Offset(center.x, center.y - 12f)
                )
            }

            // Fuse at outer edge of chakri
            drawFuse(
                base = Offset(center.x + 28f, center.y - 16f),
                tipTarget = Offset(center.x + 44f, center.y - 36f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.ANAR -> {
            val potWidth = 72f
            val potHeight = 85f
            val potTop = center.y - potHeight

            val path = Path().apply {
                moveTo(center.x - 12f, potTop)
                lineTo(center.x + 12f, potTop)
                lineTo(center.x + potWidth / 2, center.y)
                lineTo(center.x - potWidth / 2, center.y)
                close()
            }
            drawPath(path, Color(0xFFC2185B))
            // Decorative gold band
            drawLine(
                color = GoldYellow,
                start = Offset(center.x - 24f, center.y - 34f),
                end = Offset(center.x + 24f, center.y - 34f),
                strokeWidth = 6f
            )

            // Fuse sticking out of top nozzle
            drawFuse(
                base = Offset(center.x, potTop),
                tipTarget = Offset(center.x + 14f, potTop - 26f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.ROCKET -> {
            val bodyTop = center.y - 75f
            // Guide stick
            drawLine(
                color = Color(0xFFBCAAA4),
                start = Offset(center.x, bodyTop + 20f),
                end = Offset(center.x, center.y + 40f),
                strokeWidth = 3.5f
            )
            // Rocket body
            drawRoundRect(
                color = Color(0xFF6A1B9A),
                topLeft = Offset(center.x - 12f, bodyTop),
                size = Size(24f, 48f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Nose cone
            val cone = Path().apply {
                moveTo(center.x, bodyTop - 22f)
                lineTo(center.x + 15f, bodyTop)
                lineTo(center.x - 15f, bodyTop)
                close()
            }
            drawPath(cone, GoldYellow)

            // Fuse at bottom of rocket body
            drawFuse(
                base = Offset(center.x, bodyTop + 48f),
                tipTarget = Offset(center.x + 18f, bodyTop + 68f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }

        CrackerType.PHOOLJHADI -> {
            // Sparkler stick held up
            drawLine(
                color = Color(0xFF90A4AE),
                start = Offset(center.x - 15f, center.y + 20f),
                end = Offset(center.x + 15f, center.y - 80f),
                strokeWidth = 6f,
                cap = StrokeCap.Round
            )

            drawFuse(
                base = Offset(center.x + 15f, center.y - 80f),
                tipTarget = Offset(center.x + 24f, center.y - 95f),
                stage = stage,
                fuseProgress = fuseProgress
            )
        }
    }
}

// Fuse with sparks when burning
private fun DrawScope.drawFuse(
    base: Offset,
    tipTarget: Offset,
    stage: BurstStage,
    fuseProgress: Float
) {
    // Current burning tip interpolates from tipTarget down to base
    val currentTip = if (stage == BurstStage.FUSE_BURNING) {
        Offset(
            tipTarget.x + (base.x - tipTarget.x) * fuseProgress,
            tipTarget.y + (base.y - tipTarget.y) * fuseProgress
        )
    } else {
        tipTarget
    }

    if (fuseProgress < 0.98f) {
        drawLine(
            color = Color(0xFF8D6E63),
            start = base,
            end = currentTip,
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }

    // Sparks and glow if lighting or burning
    if (stage == BurstStage.LIGHTING || stage == BurstStage.FUSE_BURNING) {
        // Glowing orange spark at the burning tip
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.White, GoldYellow, FestiveOrange, Color.Transparent),
                center = currentTip,
                radius = 16f
            ),
            radius = 16f,
            center = currentTip
        )
        drawCircle(
            color = Color.White,
            radius = 4f,
            center = currentTip
        )

        // Flying sparks around fuse tip
        val rand = Random(System.nanoTime().toInt())
        repeat(6) {
            val angle = rand.nextDouble() * 2 * Math.PI
            val dist = rand.nextFloat() * 18f + 4f
            drawCircle(
                color = if (rand.nextBoolean()) GoldYellow else Color.White,
                radius = rand.nextFloat() * 2.5f + 1f,
                center = Offset(
                    (currentTip.x + dist * cos(angle)).toFloat(),
                    (currentTip.y + dist * sin(angle)).toFloat()
                )
            )
        }
    }
}

package com.example.diwali.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.FestiveRed
import com.example.diwali.ui.theme.GoldDark
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class SplashParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var color: Color,
    var radius: Float,
    var alpha: Float,
    var decay: Float
)

@Composable
fun SplashScreen(
    soundManager: SoundManager,
    isSoundEnabled: Boolean,
    onSplashFinished: () -> Unit
) {
    var rocketY by remember { mutableFloatStateOf(1.1f) } // Fraction of screen height
    var hasExploded by remember { mutableStateOf(false) }
    var flashAlpha by remember { mutableFloatStateOf(0f) }

    val logoScale = remember { Animatable(0.88f) }
    val logoAlpha = remember { Animatable(0f) }

    val particles = remember { mutableStateListOf<SplashParticle>() }

    // Rocket ascent and explosion sequence
    LaunchedEffect(Unit) {
        soundManager.playFuseBurning(isSoundEnabled)

        // Rocket flight upward: starts slow and accelerates with roaring thrust
        val ascentAnim = Animatable(1.1f)
        ascentAnim.animateTo(
            targetValue = 0.28f,
            animationSpec = tween(durationMillis = 1500, easing = androidx.compose.animation.core.CubicBezierEasing(0.35f, 0.05f, 0.25f, 1.0f))
        ) {
            rocketY = value
        }

        // Apex Reached: Detonate!
        hasExploded = true
        soundManager.playBurstSound("rocket", isSoundEnabled)

        // Generate burst particles
        val rand = Random(42)
        val colors = listOf(
            GoldYellow, GoldLight, FestiveOrange, FestiveRed,
            Color(0xFF00E5FF), Color(0xFF76FF03), Color(0xFFE040FB), Color.White
        )
        for (i in 0 until 140) {
            val angle = rand.nextDouble() * 2 * Math.PI
            val speed = rand.nextFloat() * 14f + 3f
            particles.add(
                SplashParticle(
                    x = 0f, // will offset to center in draw
                    y = 0f,
                    vx = (cos(angle) * speed).toFloat(),
                    vy = (sin(angle) * speed).toFloat(),
                    color = colors[rand.nextInt(colors.size)],
                    radius = rand.nextFloat() * 4f + 2.5f,
                    alpha = 1f,
                    decay = rand.nextFloat() * 0.015f + 0.012f
                )
            )
        }

        // Ambient flash
        flashAlpha = 0.85f

        // Reveal Logo smoothly in parallel without jerk
        launch {
            logoAlpha.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        }
        launch {
            logoScale.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
        }

        // Wait, then advance to Home
        delay(1200)
        onSplashFinished()
    }

    // Particle physics loop
    LaunchedEffect(hasExploded) {
        if (hasExploded) {
            var lastTime = 0L
            while (true) {
                withFrameNanos { now ->
                    if (lastTime == 0L) lastTime = now
                    val dt = ((now - lastTime) / 1_000_000f).coerceIn(1f, 32f)
                    lastTime = now

                    flashAlpha = (flashAlpha - 0.03f).coerceAtLeast(0f)

                    val it = particles.iterator()
                    while (it.hasNext()) {
                        val p = it.next()
                        p.x += p.vx * (dt / 16f)
                        p.y += p.vy * (dt / 16f)
                        p.vy += 0.22f * (dt / 16f) // Gravity
                        p.vx *= 0.985f
                        p.vy *= 0.985f
                        p.alpha -= p.decay * (dt / 16f)
                        if (p.alpha <= 0f) {
                            it.remove()
                        }
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF070414),
                        Color(0xFF14082B),
                        Color(0xFF200C45)
                    )
                )
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onSplashFinished
            ),
        contentAlignment = Alignment.Center
    ) {
        // Night Sky Stars & Rocket / Particles Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerX = width / 2f
            val burstCenterY = height * 0.32f

            // Twinkling stars
            val starRand = Random(123)
            for (i in 0 until 60) {
                val sx = starRand.nextFloat() * width
                val sy = starRand.nextFloat() * height
                val sr = starRand.nextFloat() * 1.8f + 0.6f
                drawCircle(Color.White.copy(alpha = 0.5f), radius = sr, center = Offset(sx, sy))
            }

            // Flash overlay
            if (flashAlpha > 0f) {
                drawRect(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = flashAlpha * 0.7f),
                            GoldYellow.copy(alpha = flashAlpha * 0.4f),
                            Color.Transparent
                        ),
                        center = Offset(centerX, burstCenterY),
                        radius = width * 0.85f
                    )
                )
            }

            // Draw rocket while ascending (Significantly enlarged & detailed)
            if (!hasExploded) {
                val curRocketY = height * rocketY
                val rocketWidth = 34f
                val rocketHeight = 105f
                val coneHeight = 32f

                // Rocket trail fire & sparks plume
                drawOval(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.White, GoldYellow, FestiveOrange, Color.Transparent)
                    ),
                    topLeft = Offset(centerX - 16f, curRocketY + rocketHeight),
                    size = Size(32f, 85f)
                )

                // Bamboo stick
                drawLine(
                    color = Color(0xFFBCAAA4),
                    start = Offset(centerX, curRocketY + 20f),
                    end = Offset(centerX, curRocketY + rocketHeight + 95f),
                    strokeWidth = 5f
                )

                // Rocket Body Cylinder
                drawRect(
                    brush = Brush.horizontalGradient(
                        colors = listOf(FestiveRed, FestiveOrange, FestiveRed)
                    ),
                    topLeft = Offset(centerX - rocketWidth / 2f, curRocketY + coneHeight),
                    size = Size(rocketWidth, rocketHeight - coneHeight)
                )
                // Decorative golden bands
                drawLine(
                    color = GoldYellow,
                    start = Offset(centerX - rocketWidth / 2f, curRocketY + coneHeight + 18f),
                    end = Offset(centerX + rocketWidth / 2f, curRocketY + coneHeight + 18f),
                    strokeWidth = 4f
                )
                drawLine(
                    color = GoldYellow,
                    start = Offset(centerX - rocketWidth / 2f, curRocketY + rocketHeight - 14f),
                    end = Offset(centerX + rocketWidth / 2f, curRocketY + rocketHeight - 14f),
                    strokeWidth = 4f
                )

                // Aerodynamic Nose cone
                val conePath = Path().apply {
                    moveTo(centerX, curRocketY)
                    lineTo(centerX - rocketWidth / 2f - 2f, curRocketY + coneHeight)
                    lineTo(centerX + rocketWidth / 2f + 2f, curRocketY + coneHeight)
                    close()
                }
                drawPath(conePath, GoldYellow)
            }

            // Draw burst particles
            if (hasExploded) {
                particles.forEach { p ->
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                        radius = p.radius,
                        center = Offset(centerX + p.x, burstCenterY + p.y)
                    )
                }
            }
        }

        // Logo & Title in the center
        if (hasExploded) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = "DHAMAKA",
                    fontSize = 46.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    color = GoldLight,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Fireworks Simulator",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Tap to Start",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = GoldYellow.copy(alpha = 0.7f)
                )
            }
        }
    }
}

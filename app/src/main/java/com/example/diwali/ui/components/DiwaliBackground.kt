package com.example.diwali.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import com.example.diwali.ui.theme.DiwaliNightBottom
import com.example.diwali.ui.theme.DiwaliNightMid
import com.example.diwali.ui.theme.DiwaliNightTop
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldYellow
import kotlin.random.Random

private data class Star(
    val xRatio: Float,
    val yRatio: Float,
    val radius: Float,
    val alphaMin: Float,
    val phaseOffset: Float
)

@Composable
fun DiwaliBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "DiwaliBgTransition")

    val starTwinkle by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "StarTwinkle"
    )

    val diyaFlicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "DiyaFlicker"
    )

    val lanternSway by infiniteTransition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LanternSway"
    )

    val stars = remember {
        val rand = Random(42)
        List(40) {
            Star(
                xRatio = rand.nextFloat(),
                yRatio = rand.nextFloat() * 0.75f,
                radius = 1.5f + rand.nextFloat() * 2.5f,
                alphaMin = 0.2f + rand.nextFloat() * 0.4f,
                phaseOffset = rand.nextFloat()
            )
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        DiwaliNightTop,
                        DiwaliNightMid,
                        DiwaliNightBottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // 1. Draw twinkling stars
            stars.forEach { star ->
                val currentAlpha = ((starTwinkle + star.phaseOffset) % 1.0f)
                    .coerceIn(star.alphaMin, 1.0f)
                drawCircle(
                    color = Color.White.copy(alpha = currentAlpha),
                    radius = star.radius,
                    center = Offset(star.xRatio * width, star.yRatio * height)
                )
            }

            // 2. Draw hanging celebratory lanterns (Kandils) at top
            drawLantern(
                center = Offset(width * 0.18f + lanternSway, height * 0.045f),
                color = FestiveOrange,
                scale = 0.85f
            )
            drawLantern(
                center = Offset(width * 0.50f - lanternSway * 0.5f, height * 0.035f),
                color = Color(0xFFFF4081),
                scale = 1.0f
            )
            drawLantern(
                center = Offset(width * 0.82f + lanternSway * 0.8f, height * 0.045f),
                color = Color(0xFFFFD54F),
                scale = 0.85f
            )

            // 3. Draw festive Diyas (દીવા) at the bottom floor
            val bottomDiyaY = height * 0.94f
            drawDiya(
                center = Offset(width * 0.12f, bottomDiyaY),
                flicker = diyaFlicker,
                scale = 0.85f
            )
            drawDiya(
                center = Offset(width * 0.37f, bottomDiyaY + 4),
                flicker = 2.0f - diyaFlicker,
                scale = 0.95f
            )
            drawDiya(
                center = Offset(width * 0.63f, bottomDiyaY + 4),
                flicker = diyaFlicker * 0.95f,
                scale = 0.95f
            )
            drawDiya(
                center = Offset(width * 0.88f, bottomDiyaY),
                flicker = 2.0f - diyaFlicker,
                scale = 0.85f
            )
        }
    }
}

private fun DrawScope.drawLantern(center: Offset, color: Color, scale: Float) {
    val r = 20f * scale
    // Hanging thread
    drawLine(
        color = Color(0x88FFD54F),
        start = Offset(center.x, 0f),
        end = Offset(center.x, center.y - r),
        strokeWidth = 2f
    )

    // Diamond shaped kandil body
    val path = Path().apply {
        moveTo(center.x, center.y - r)
        lineTo(center.x + r * 1.1f, center.y)
        lineTo(center.x, center.y + r)
        lineTo(center.x - r * 1.1f, center.y)
        close()
    }
    drawPath(path, color)

    // Inner bright glow
    drawCircle(
        color = GoldYellow.copy(alpha = 0.8f),
        radius = r * 0.45f,
        center = center
    )

    // Bottom decorative tassels
    for (i in -2..2) {
        drawLine(
            color = GoldYellow.copy(alpha = 0.7f),
            start = Offset(center.x + i * 4 * scale, center.y + r),
            end = Offset(center.x + i * 6 * scale, center.y + r + 18f * scale),
            strokeWidth = 2f
        )
    }
}

private fun DrawScope.drawDiya(center: Offset, flicker: Float, scale: Float) {
    val clayColor = Color(0xFFD84315)
    val width = 44f * scale
    val height = 18f * scale

    // Diya clay base
    val diyaPath = Path().apply {
        moveTo(center.x - width / 2, center.y)
        cubicTo(
            center.x - width / 3, center.y + height,
            center.x + width / 3, center.y + height,
            center.x + width / 2, center.y
        )
        close()
    }
    drawPath(diyaPath, clayColor)

    // Diya rim
    drawOval(
        color = Color(0xFFFFAB91),
        topLeft = Offset(center.x - width / 2, center.y - 4f * scale),
        size = Size(width, 7f * scale)
    )

    // Flame outer glow
    val flameRadius = 16f * scale * flicker
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                GoldYellow.copy(alpha = 0.6f),
                FestiveOrange.copy(alpha = 0.2f),
                Color.Transparent
            ),
            center = Offset(center.x, center.y - 10f * scale),
            radius = flameRadius * 1.8f
        ),
        radius = flameRadius * 1.8f,
        center = Offset(center.x, center.y - 10f * scale)
    )

    // Flame body
    val flameHeight = 24f * scale * flicker
    val flameWidth = 10f * scale
    val flamePath = Path().apply {
        moveTo(center.x - flameWidth / 2, center.y - 2f * scale)
        cubicTo(
            center.x - flameWidth, center.y - flameHeight * 0.4f,
            center.x, center.y - flameHeight * 0.8f,
            center.x, center.y - flameHeight
        )
        cubicTo(
            center.x, center.y - flameHeight * 0.8f,
            center.x + flameWidth, center.y - flameHeight * 0.4f,
            center.x + flameWidth / 2, center.y - 2f * scale
        )
        close()
    }
    drawPath(flamePath, FestiveOrange)

    // Inner bright yellow flame core
    val coreHeight = flameHeight * 0.6f
    val coreWidth = flameWidth * 0.5f
    val corePath = Path().apply {
        moveTo(center.x - coreWidth / 2, center.y - 2f * scale)
        cubicTo(
            center.x - coreWidth, center.y - coreHeight * 0.4f,
            center.x, center.y - coreHeight * 0.8f,
            center.x, center.y - coreHeight
        )
        cubicTo(
            center.x, center.y - coreHeight * 0.8f,
            center.x + coreWidth, center.y - coreHeight * 0.4f,
            center.x + coreWidth / 2, center.y - 2f * scale
        )
        close()
    }
    drawPath(corePath, GoldYellow)
}

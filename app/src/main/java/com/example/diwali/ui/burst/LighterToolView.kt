package com.example.diwali.ui.burst

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.dp
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldYellow

@Composable
fun LighterToolView(
    modifier: Modifier = Modifier,
    isIgniting: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "LighterFlameAnim")

    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameFlicker"
    )

    val flameSway by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameSway"
    )

    Box(modifier = modifier.size(width = 85.dp, height = 180.dp)) {
        Canvas(modifier = Modifier.size(width = 85.dp, height = 180.dp)) {
            val center = Offset(size.width / 2, size.height * 0.58f)
            drawGasLighter(
                center = center,
                flameFlicker = flameFlicker,
                flameSway = flameSway,
                isIgniting = isIgniting
            )
        }
    }
}

private fun DrawScope.drawGasLighter(
    center: Offset,
    flameFlicker: Float,
    flameSway: Float,
    isIgniting: Boolean
) {
    val bodyWidth = 52f
    val bodyHeight = 96f
    val bodyTop = center.y - 10f
    val bodyLeft = center.x - bodyWidth / 2

    // 1. Animated Flame on top
    if (isIgniting) {
        val nozzleTip = Offset(center.x - 8f, bodyTop - 26f)
        val flameHeight = 44f * flameFlicker
        val flameWidth = 14f

        // Outer Flame Glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    GoldYellow.copy(alpha = 0.55f),
                    FestiveOrange.copy(alpha = 0.2f),
                    Color.Transparent
                ),
                center = Offset(nozzleTip.x + flameSway, nozzleTip.y - flameHeight * 0.4f),
                radius = flameHeight * 1.3f
            ),
            radius = flameHeight * 1.3f,
            center = Offset(nozzleTip.x + flameSway, nozzleTip.y - flameHeight * 0.4f)
        )

        // Outer Orange/Yellow Flame
        val outerFlamePath = Path().apply {
            moveTo(nozzleTip.x - flameWidth / 2, nozzleTip.y)
            cubicTo(
                nozzleTip.x - flameWidth, nozzleTip.y - flameHeight * 0.4f,
                nozzleTip.x + flameSway, nozzleTip.y - flameHeight * 0.8f,
                nozzleTip.x + flameSway, nozzleTip.y - flameHeight
            )
            cubicTo(
                nozzleTip.x + flameSway, nozzleTip.y - flameHeight * 0.8f,
                nozzleTip.x + flameWidth, nozzleTip.y - flameHeight * 0.4f,
                nozzleTip.x + flameWidth / 2, nozzleTip.y
            )
            close()
        }
        drawPath(
            path = outerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(GoldYellow, FestiveOrange),
                startY = nozzleTip.y - flameHeight,
                endY = nozzleTip.y
            )
        )

        // Inner Blue Core Flame
        val coreHeight = flameHeight * 0.4f
        val coreWidth = flameWidth * 0.45f
        val innerFlamePath = Path().apply {
            moveTo(nozzleTip.x - coreWidth / 2, nozzleTip.y)
            cubicTo(
                nozzleTip.x - coreWidth, nozzleTip.y - coreHeight * 0.5f,
                nozzleTip.x, nozzleTip.y - coreHeight * 0.8f,
                nozzleTip.x, nozzleTip.y - coreHeight
            )
            cubicTo(
                nozzleTip.x, nozzleTip.y - coreHeight * 0.8f,
                nozzleTip.x + coreWidth, nozzleTip.y - coreHeight * 0.5f,
                nozzleTip.x + coreWidth / 2, nozzleTip.y
            )
            close()
        }
        drawPath(
            path = innerFlamePath,
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, Color(0xFF00E5FF), Color(0xFF2979FF)),
                startY = nozzleTip.y - coreHeight,
                endY = nozzleTip.y
            )
        )
    }

    // 2. Metallic Hood & Spark Wheel
    // Spark wheel (right side)
    drawRoundRect(
        color = Color(0xFF78909C),
        topLeft = Offset(center.x + 4f, bodyTop - 24f),
        size = Size(18f, 24f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    for (i in 0..4) {
        val y = bodyTop - 22f + i * 4.5f
        drawLine(
            color = Color(0xFF37474F),
            start = Offset(center.x + 4f, y),
            end = Offset(center.x + 22f, y),
            strokeWidth = 1.5f
        )
    }

    // Metal wind guard / hood (left side where flame emerges)
    drawRoundRect(
        color = Color(0xFFB0BEC5),
        topLeft = Offset(center.x - 22f, bodyTop - 26f),
        size = Size(26f, 26f),
        cornerRadius = CornerRadius(4f, 4f)
    )
    // Wind guard ventilation holes
    drawCircle(
        color = Color(0xFF455A64),
        radius = 3f,
        center = Offset(center.x - 12f, bodyTop - 14f)
    )
    drawCircle(
        color = Color(0xFF455A64),
        radius = 3f,
        center = Offset(center.x - 12f, bodyTop - 6f)
    )

    // Metal band collar
    drawRect(
        color = Color(0xFF90A4AE),
        topLeft = Offset(bodyLeft, bodyTop - 2f),
        size = Size(bodyWidth, 10f)
    )

    // 3. Transparent Blue Lighter Body
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(
                Color(0xEE00B0FF),
                Color(0x9940C4FF),
                Color(0xEE0091EA)
            )
        ),
        topLeft = Offset(bodyLeft, bodyTop + 8f),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(8f, 8f)
    )

    // Lighter fuel liquid inside
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color(0xAA00E5FF),
                Color(0xDD0091EA)
            )
        ),
        topLeft = Offset(bodyLeft + 4f, bodyTop + 34f),
        size = Size(bodyWidth - 8f, bodyHeight - 28f),
        cornerRadius = CornerRadius(6f, 6f)
    )

    // Internal siphon tube
    drawLine(
        color = Color(0x88FFFFFF),
        start = Offset(center.x - 4f, bodyTop + 10f),
        end = Offset(center.x - 4f, bodyTop + bodyHeight - 4f),
        strokeWidth = 3f
    )

    // Glossy glass reflection shine
    drawLine(
        color = Color(0x66FFFFFF),
        start = Offset(bodyLeft + 6f, bodyTop + 14f),
        end = Offset(bodyLeft + 6f, bodyTop + bodyHeight - 6f),
        strokeWidth = 3f
    )
}

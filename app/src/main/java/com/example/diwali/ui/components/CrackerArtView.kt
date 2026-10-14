package com.example.diwali.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.example.diwali.model.CrackerType
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CrackerArtView(
    type: CrackerType,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CrackerArtAnim")

    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(2800, easing = LinearEasing)
        ),
        label = "RotationAnim"
    )

    val sparkPulse by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "SparkPulse"
    )

    val floatAnim by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FloatAnim"
    )

    Box(
        modifier = modifier.size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(175.dp)) {
            val center = Offset(size.width / 2, size.height / 2 + floatAnim)

            when (type) {
                CrackerType.CHAKRI -> drawChakri(center, rotationAnim, sparkPulse)
                CrackerType.ANAR -> drawAnar(center, sparkPulse)
                CrackerType.PHOOLJHADI -> drawPhooljhadi(center, sparkPulse)
                CrackerType.SUTLI_BOMB -> drawSutliBomb(center, sparkPulse)
                CrackerType.ROCKET -> drawRocket(center, sparkPulse)
            }
        }
    }
}

// 1. Chakri (Ground Spinner)
private fun DrawScope.drawChakri(center: Offset, rotation: Float, sparkPulse: Float) {
    val radius = 58f

    // Outer sparkling aura
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                GoldYellow.copy(alpha = 0.45f * sparkPulse),
                Color.Transparent
            ),
            center = center,
            radius = radius * 1.5f
        ),
        radius = radius * 1.5f,
        center = center
    )

    // Shooting spark rays around the spinning perimeter
    for (i in 0 until 8) {
        val angle = Math.toRadians((rotation + i * 45).toDouble())
        val startR = radius * 1.05f
        val endR = radius * (1.2f + 0.15f * sparkPulse)
        drawLine(
            color = GoldYellow,
            start = Offset(
                (center.x + startR * cos(angle)).toFloat(),
                (center.y + startR * sin(angle)).toFloat()
            ),
            end = Offset(
                (center.x + endR * cos(angle)).toFloat(),
                (center.y + endR * sin(angle)).toFloat()
            ),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
    }

    // Outer wheel
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFB300), Color(0xFFE65100)),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Golden rim border
    drawCircle(
        color = Color(0xFFFFD54F),
        radius = radius,
        center = center,
        style = Stroke(width = 6f)
    )

    // Spiral spokes
    rotate(rotation, pivot = center) {
        for (i in 0 until 4) {
            val spokeAngle = Math.toRadians((i * 90).toDouble())
            drawLine(
                color = Color.White.copy(alpha = 0.9f),
                start = center,
                end = Offset(
                    (center.x + radius * 0.85f * cos(spokeAngle)).toFloat(),
                    (center.y + radius * 0.85f * sin(spokeAngle)).toFloat()
                ),
                strokeWidth = 5f,
                cap = StrokeCap.Round
            )
        }
    }

    // Inner core hub
    drawCircle(
        color = Color(0xFFD50000),
        radius = 16f,
        center = center
    )
    drawCircle(
        color = Color.White,
        radius = 8f,
        center = center
    )
}

// 2. Anar (Flowerpot)
private fun DrawScope.drawAnar(center: Offset, sparkPulse: Float) {
    val potWidth = 84f
    val potHeight = 90f
    val potTopY = center.y - potHeight * 0.35f
    val potBottomY = center.y + potHeight * 0.65f

    // 1. Spouting spark fountain from top nozzle
    val fountainTop = potTopY - 48f * sparkPulse
    for (i in -4..4) {
        val spread = i * 8f
        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color.White, GoldYellow, FestiveOrange)
            ),
            start = Offset(center.x, potTopY - 6f),
            end = Offset(center.x + spread, fountainTop + (i * i) * 1.5f),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )
        // Little sparkling stars at the fountain head
        drawCircle(
            color = Color.White,
            radius = 3.5f * sparkPulse,
            center = Offset(center.x + spread, fountainTop + (i * i) * 1.5f)
        )
    }

    // 2. Anar Cone Body (Clay pot)
    val potPath = Path().apply {
        moveTo(center.x - 14f, potTopY)
        lineTo(center.x + 14f, potTopY)
        lineTo(center.x + potWidth / 2, potBottomY)
        lineTo(center.x - potWidth / 2, potBottomY)
        close()
    }
    drawPath(
        path = potPath,
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFFF1744), Color(0xFF880E4F)),
            startY = potTopY,
            endY = potBottomY
        )
    )

    // Golden decorative bands across pot
    val bandY = center.y + 10f
    drawLine(
        color = GoldYellow,
        start = Offset(center.x - potWidth * 0.32f, bandY),
        end = Offset(center.x + potWidth * 0.32f, bandY),
        strokeWidth = 8f
    )
    drawLine(
        color = Color.White,
        start = Offset(center.x - potWidth * 0.22f, bandY - 14f),
        end = Offset(center.x + potWidth * 0.22f, bandY - 14f),
        strokeWidth = 4f
    )

    // Base rim
    drawOval(
        color = Color(0xFFFFB300),
        topLeft = Offset(center.x - potWidth / 2 - 2, potBottomY - 5),
        size = Size(potWidth + 4, 12f)
    )

    // Top nozzle
    drawRect(
        color = Color(0xFFFFC107),
        topLeft = Offset(center.x - 16f, potTopY - 6f),
        size = Size(32f, 8f)
    )
}

// 3. Phooljhadi (Magic Sparkler)
private fun DrawScope.drawPhooljhadi(center: Offset, sparkPulse: Float) {
    val stickLength = 110f
    val tipX = center.x + 36f
    val tipY = center.y - 40f
    val handleX = center.x - 44f
    val handleY = center.y + 54f

    // 1. Metal wire
    drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(handleX, handleY),
        end = Offset(tipX, tipY),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // 2. Chemical coated upper section
    val midX = (handleX + tipX) / 2 - 6f
    val midY = (handleY + tipY) / 2 + 6f
    drawLine(
        color = Color(0xFF455A64),
        start = Offset(midX, midY),
        end = Offset(tipX, tipY),
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )

    // 3. Glowing burning tip
    drawCircle(
        color = GoldYellow,
        radius = 8f * sparkPulse,
        center = Offset(tipX, tipY)
    )
    drawCircle(
        color = Color.White,
        radius = 5f,
        center = Offset(tipX, tipY)
    )

    // 4. Sparkler starburst rays!
    val sparkColors = listOf(Color.White, GoldYellow, Color(0xFF00E5FF), Color(0xFFFF4081))
    for (i in 0 until 12) {
        val angle = Math.toRadians((i * 30.0) + (sparkPulse * 20.0))
        val len = (22f + (i % 3) * 12f) * sparkPulse
        val ex = (tipX + len * cos(angle)).toFloat()
        val ey = (tipY + len * sin(angle)).toFloat()

        drawLine(
            color = sparkColors[i % sparkColors.size],
            start = Offset(tipX, tipY),
            end = Offset(ex, ey),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
        // Little diamond spark at the tip
        drawCircle(
            color = Color.White,
            radius = 2.5f,
            center = Offset(ex, ey)
        )
    }
}

// 4. Sutli Bomb (Green Atom Bomb)
private fun DrawScope.drawSutliBomb(center: Offset, sparkPulse: Float) {
    val bombWidth = 80f
    val bombHeight = 84f
    val bombTop = center.y - bombHeight / 2 + 8f

    // 1. Burning fuse rope
    val fuseTip = Offset(center.x + 24f, bombTop - 34f)
    val fuseBase = Offset(center.x, bombTop)
    val fusePath = Path().apply {
        moveTo(fuseBase.x, fuseBase.y)
        cubicTo(
            center.x + 8f, bombTop - 12f,
            center.x + 12f, bombTop - 24f,
            fuseTip.x, fuseTip.y
        )
    }
    drawPath(
        path = fusePath,
        color = Color(0xFF8D6E63),
        style = Stroke(width = 5f, cap = StrokeCap.Round)
    )

    // Fuse burning spark
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, GoldYellow, FestiveOrange, Color.Transparent),
            center = fuseTip,
            radius = 16f * sparkPulse
        ),
        radius = 16f * sparkPulse,
        center = fuseTip
    )
    drawCircle(
        color = Color.White,
        radius = 4f * sparkPulse,
        center = fuseTip
    )

    // 2. Green bomb block (Twine wound texture)
    val bombRect = Size(bombWidth, bombHeight)
    val bombTopLeft = Offset(center.x - bombWidth / 2, bombTop)

    // Main green body
    drawRoundRect(
        color = Color(0xFF2E7D32),
        topLeft = bombTopLeft,
        size = bombRect,
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(14f, 14f)
    )

    // Wound string/twine lines
    for (i in 1..4) {
        val y = bombTop + (i * bombHeight / 5)
        drawLine(
            color = Color(0xFF1B5E20),
            start = Offset(bombTopLeft.x, y),
            end = Offset(bombTopLeft.x + bombWidth, y),
            strokeWidth = 3f
        )
    }
    for (i in 1..3) {
        val x = bombTopLeft.x + (i * bombWidth / 4)
        drawLine(
            color = Color(0xFF1B5E20),
            start = Offset(x, bombTop),
            end = Offset(x, bombTop + bombHeight),
            strokeWidth = 3f
        )
    }

    // Red festive label tag in center
    drawRect(
        color = Color(0xFFD50000),
        topLeft = Offset(center.x - 22f, center.y - 10f),
        size = Size(44f, 26f)
    )
    drawRect(
        color = GoldYellow,
        topLeft = Offset(center.x - 18f, center.y - 6f),
        size = Size(36f, 18f)
    )
}

// 5. Sky Rocket
private fun DrawScope.drawRocket(center: Offset, sparkPulse: Float) {
    val rocketLength = 85f
    val rocketWidth = 26f

    // 1. Bamboo guide stick
    val stickStartY = center.y - 10f
    val stickEndY = center.y + 70f
    drawLine(
        color = Color(0xFFD7CCC8),
        start = Offset(center.x, stickStartY),
        end = Offset(center.x, stickEndY),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )

    // 2. Rocket thrust sparks at bottom
    val thrustY = center.y + 20f
    for (i in -2..2) {
        drawLine(
            color = GoldYellow,
            start = Offset(center.x + i * 3f, thrustY),
            end = Offset(center.x + i * 7f, thrustY + 18f * sparkPulse),
            strokeWidth = 3.5f,
            cap = StrokeCap.Round
        )
    }

    // 3. Rocket Body Cylinder
    val bodyTop = center.y - 35f
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF7B1FA2), Color(0xFF4A148C))
        ),
        topLeft = Offset(center.x - rocketWidth / 2, bodyTop),
        size = Size(rocketWidth, 54f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    // Decorative striped pattern on rocket body
    drawLine(
        color = GoldYellow,
        start = Offset(center.x - rocketWidth / 2, bodyTop + 16f),
        end = Offset(center.x + rocketWidth / 2, bodyTop + 16f),
        strokeWidth = 4f
    )
    drawLine(
        color = Color(0xFFFF4081),
        start = Offset(center.x - rocketWidth / 2, bodyTop + 32f),
        end = Offset(center.x + rocketWidth / 2, bodyTop + 32f),
        strokeWidth = 4f
    )

    // 4. Pointed Nose Cone (Triangle on top)
    val noseTip = Offset(center.x, bodyTop - 24f)
    val conePath = Path().apply {
        moveTo(noseTip.x, noseTip.y)
        lineTo(center.x + rocketWidth / 2 + 3f, bodyTop)
        lineTo(center.x - rocketWidth / 2 - 3f, bodyTop)
        close()
    }
    drawPath(conePath, Color(0xFFFFD54F))

    // 5. Wings/Fins
    val leftFin = Path().apply {
        moveTo(center.x - rocketWidth / 2, bodyTop + 32f)
        lineTo(center.x - rocketWidth / 2 - 12f, bodyTop + 54f)
        lineTo(center.x - rocketWidth / 2, bodyTop + 50f)
        close()
    }
    drawPath(leftFin, Color(0xFFFF5722))

    val rightFin = Path().apply {
        moveTo(center.x + rocketWidth / 2, bodyTop + 32f)
        lineTo(center.x + rocketWidth / 2 + 12f, bodyTop + 54f)
        lineTo(center.x + rocketWidth / 2, bodyTop + 50f)
        close()
    }
    drawPath(rightFin, Color(0xFFFF5722))
}

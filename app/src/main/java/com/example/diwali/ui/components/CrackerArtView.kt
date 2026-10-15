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
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.unit.dp
import com.example.diwali.model.CrackerType
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldYellow
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun CrackerArtView(
    type: CrackerType,
    modifier: Modifier = Modifier.fillMaxSize()
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
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val minDim = minOf(size.width, size.height)
            if (minDim <= 0f) return@Canvas

            // Scale drawings so they never clip in any container size
            val refSize = 145f
            val scaleFactor = (minDim * 0.84f) / refSize
            val center = Offset(size.width / 2f, size.height / 2f + floatAnim * (scaleFactor.coerceIn(0.2f, 1f)))

            scale(scaleFactor, pivot = center) {
                when (type) {
                CrackerType.SUTLI_BOMB -> drawSutliBomb(center, sparkPulse)
                CrackerType.ROCKET -> drawRocket(center, sparkPulse)
                CrackerType.PHOOLJHADI -> drawPhooljhadi(center, sparkPulse)
                CrackerType.CHAKRI -> drawChakri(center, rotationAnim, sparkPulse)
                CrackerType.ANAR -> drawAnar(center, sparkPulse)
                CrackerType.PENCIL_WHIP -> drawPencilWhip(center, sparkPulse)
                CrackerType.PAPER_BOMB -> drawLaxmiBomb(center, sparkPulse)
                CrackerType.LADI -> drawLadi(center, sparkPulse)
                CrackerType.SNAKE_TABLET -> drawSnakeTablet(center, sparkPulse)
                CrackerType.COLOR_SPARKLER -> drawColorSparkler(center, sparkPulse)
                CrackerType.AERIAL_SHELL -> drawAerialShell(center, sparkPulse)
                CrackerType.WHIZ_WHEEL -> drawWhizWheel(center, rotationAnim, sparkPulse)
                CrackerType.ELECTRIC_FOUNTAIN -> drawElectricFountain(center, sparkPulse)
                CrackerType.SKY_SHOTS -> drawSkyShots(center, sparkPulse)
            }
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

// 6. Magic Whip / Pencil (Crackling Stick)
private fun DrawScope.drawPencilWhip(center: Offset, sparkPulse: Float) {
    val stickHeight = 110f
    val stickWidth = 14f
    val stickTop = center.y - stickHeight / 2

    // Glowing aura around tip
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFEA80FC).copy(alpha = 0.5f * sparkPulse), Color.Transparent),
            center = Offset(center.x, stickTop),
            radius = 35f * sparkPulse
        ),
        radius = 35f * sparkPulse,
        center = Offset(center.x, stickTop)
    )

    // Pencil Body Cylinder
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF8E24AA), Color(0xFF4A148C))
        ),
        topLeft = Offset(center.x - stickWidth / 2, stickTop + 14f),
        size = Size(stickWidth, stickHeight - 14f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Striped spiral rings on pencil
    for (i in 0 until 5) {
        val y = stickTop + 24f + i * 16f
        drawLine(
            color = GoldYellow,
            start = Offset(center.x - stickWidth / 2, y),
            end = Offset(center.x + stickWidth / 2, y + 6f),
            strokeWidth = 3f
        )
    }

    // Pointed pencil tip
    val tipPath = Path().apply {
        moveTo(center.x, stickTop)
        lineTo(center.x + stickWidth / 2, stickTop + 14f)
        lineTo(center.x - stickWidth / 2, stickTop + 14f)
        close()
    }
    drawPath(tipPath, Color(0xFFFFD54F))

    // Tiny crackling sparks at tip
    val sparkColors = listOf(Color.White, GoldYellow, Color(0xFFEA80FC))
    for (i in 0 until 6) {
        val angle = Math.toRadians((i * 60.0))
        val dist = 18f * sparkPulse
        drawLine(
            color = sparkColors[i % sparkColors.size],
            start = Offset(center.x, stickTop),
            end = Offset((center.x + dist * cos(angle)).toFloat(), (stickTop + dist * sin(angle)).toFloat()),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }
}

// 7. Laxmi Bomb (Red Paper Packet Bomb)
private fun DrawScope.drawLaxmiBomb(center: Offset, sparkPulse: Float) {
    val bombSize = 68f
    val bombTop = center.y - bombSize / 2

    // Red packet bomb body
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFFE53935), Color(0xFFB71C1C))
        ),
        topLeft = Offset(center.x - bombSize / 2, bombTop),
        size = Size(bombSize, bombSize),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
    )

    // Golden frame border
    drawRoundRect(
        color = GoldYellow,
        topLeft = Offset(center.x - bombSize / 2 + 5f, bombTop + 5f),
        size = Size(bombSize - 10f, bombSize - 10f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f),
        style = Stroke(width = 2.5f)
    )

    // Golden tying ribbon across center
    drawLine(
        color = Color(0xFFFFD54F),
        start = Offset(center.x, bombTop),
        end = Offset(center.x, bombTop + bombSize),
        strokeWidth = 4f
    )
    drawLine(
        color = Color(0xFFFFD54F),
        start = Offset(center.x - bombSize / 2, center.y),
        end = Offset(center.x + bombSize / 2, center.y),
        strokeWidth = 4f
    )

    // Golden Lakshmi emblem circle in center
    drawCircle(
        color = GoldYellow,
        radius = 14f,
        center = center
    )
    drawCircle(
        color = Color(0xFFD50000),
        radius = 11f,
        center = center
    )

    // Top fuse with spark
    drawLine(
        color = Color(0xFF8D6E63),
        start = Offset(center.x, bombTop),
        end = Offset(center.x + 14f, bombTop - 22f),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, GoldYellow, Color.Transparent),
            center = Offset(center.x + 14f, bombTop - 22f),
            radius = 12f * sparkPulse
        ),
        radius = 12f * sparkPulse,
        center = Offset(center.x + 14f, bombTop - 22f)
    )
}

// 8. 1000 Ladi (Series Garland of Crackers)
private fun DrawScope.drawLadi(center: Offset, sparkPulse: Float) {
    val garlandWidth = 50f
    val garlandHeight = 110f
    val topY = center.y - garlandHeight / 2

    // Main central string
    drawLine(
        color = Color(0xFF8D6E63),
        start = Offset(center.x, topY - 14f),
        end = Offset(center.x, topY + garlandHeight),
        strokeWidth = 3.5f
    )

    // Rows of miniature red crackers tied to string
    val rows = 6
    for (r in 0 until rows) {
        val y = topY + r * 16f
        // Left cracker
        drawRoundRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(center.x - garlandWidth / 2, y),
            size = Size(garlandWidth / 2 - 3f, 10f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        // Yellow tip
        drawRect(
            color = GoldYellow,
            topLeft = Offset(center.x - garlandWidth / 2, y + 2f),
            size = Size(5f, 6f)
        )

        // Right cracker
        drawRoundRect(
            color = Color(0xFFD32F2F),
            topLeft = Offset(center.x + 3f, y),
            size = Size(garlandWidth / 2 - 3f, 10f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        // Yellow tip
        drawRect(
            color = GoldYellow,
            topLeft = Offset(center.x + garlandWidth / 2 - 5f, y + 2f),
            size = Size(5f, 6f)
        )
    }

    // Top braided fuse with sizzling spark
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, GoldYellow, Color.Transparent),
            center = Offset(center.x, topY - 14f),
            radius = 14f * sparkPulse
        ),
        radius = 14f * sparkPulse,
        center = Offset(center.x, topY - 14f)
    )
}

// 9. Snake Tablet (Saap ni Goli)
private fun DrawScope.drawSnakeTablet(center: Offset, sparkPulse: Float) {
    // Ceramic Dish / Plate base
    drawOval(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF78909C), Color(0xFF37474F)),
            center = center,
            radius = 60f
        ),
        topLeft = Offset(center.x - 56f, center.y + 4f),
        size = Size(112f, 28f)
    )
    drawOval(
        color = Color(0xFFB0BEC5),
        topLeft = Offset(center.x - 52f, center.y + 6f),
        size = Size(104f, 22f),
        style = Stroke(width = 2.5f)
    )

    // Black Snake Tablet (solid cylinder)
    val tabRadius = 26f
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFF424242), Color(0xFF212121), Color(0xFF000000)),
            center = Offset(center.x, center.y - 2f),
            radius = tabRadius
        ),
        radius = tabRadius,
        center = Offset(center.x, center.y - 2f)
    )

    // Hot glowing chemical center
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFD54F), Color(0xFFFF5722), Color.Transparent),
            center = Offset(center.x, center.y - 2f),
            radius = 14f * sparkPulse
        ),
        radius = 14f * sparkPulse,
        center = Offset(center.x, center.y - 2f)
    )

    // Wisp of curly dark ash emerging
    val ashPath = Path().apply {
        moveTo(center.x, center.y - 12f)
        cubicTo(
            center.x - 14f, center.y - 30f,
            center.x + 18f, center.y - 48f,
            center.x + 2f, center.y - 66f
        )
    }
    drawPath(
        ashPath,
        color = Color(0xCC37474F),
        style = Stroke(width = 9f, cap = StrokeCap.Round)
    )
}

// 10. Color Sparkler (Rainbow Neon Sparkler)
private fun DrawScope.drawColorSparkler(center: Offset, sparkPulse: Float) {
    // Metallic wire handle
    drawLine(
        color = Color(0xFFB0BEC5),
        start = Offset(center.x - 24f, center.y + 54f),
        end = Offset(center.x + 24f, center.y - 54f),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )

    // Rainbow neon chemical coating on upper section
    drawLine(
        brush = Brush.linearGradient(
            colors = listOf(Color(0xFF00E676), Color(0xFFFF1744), Color(0xFF00E5FF)),
            start = Offset(center.x - 4f, center.y + 8f),
            end = Offset(center.x + 24f, center.y - 54f)
        ),
        start = Offset(center.x - 4f, center.y + 8f),
        end = Offset(center.x + 24f, center.y - 54f),
        strokeWidth = 10f,
        cap = StrokeCap.Round
    )

    // Brilliant multicolored sparkling starburst at the tip
    val tip = Offset(center.x + 24f, center.y - 54f)
    val colors = listOf(Color(0xFF00E676), Color(0xFFFF1744), Color(0xFF00E5FF), Color(0xFFFFEA00), Color.White)
    for (i in 0 until 8) {
        val angle = Math.toRadians(i * 45.0)
        val len = (22f + (i % 2) * 8f) * sparkPulse
        drawLine(
            color = colors[i % colors.size],
            start = tip,
            end = Offset((tip.x + len * cos(angle)).toFloat(), (tip.y + len * sin(angle)).toFloat()),
            strokeWidth = 3f,
            cap = StrokeCap.Round
        )
    }
}

// 11. Aerial Shell (Sky Blast Mortar Pipe)
private fun DrawScope.drawAerialShell(center: Offset, sparkPulse: Float) {
    val mortarWidth = 54f
    val mortarHeight = 84f
    val baseTop = center.y + 24f

    // Heavy wooden launch base
    drawRoundRect(
        color = Color(0xFF4E342E),
        topLeft = Offset(center.x - 42f, baseTop),
        size = Size(84f, 20f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f)
    )

    // Mortar Launch Tube Cylinder
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFF1565C0), Color(0xFF0D47A1), Color(0xFF1976D2))
        ),
        topLeft = Offset(center.x - mortarWidth / 2, center.y - 48f),
        size = Size(mortarWidth, mortarHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    // Metal reinforcement rings
    drawLine(
        color = GoldYellow,
        start = Offset(center.x - mortarWidth / 2, center.y - 36f),
        end = Offset(center.x + mortarWidth / 2, center.y - 36f),
        strokeWidth = 4f
    )
    drawLine(
        color = GoldYellow,
        start = Offset(center.x - mortarWidth / 2, center.y + 10f),
        end = Offset(center.x + mortarWidth / 2, center.y + 10f),
        strokeWidth = 4f
    )

    // Spherical Shell inside top rim
    drawCircle(
        color = Color(0xFFFFB300),
        radius = 18f,
        center = Offset(center.x, center.y - 50f)
    )

    // Fuse emerging from mortar
    drawLine(
        color = Color(0xFF8D6E63),
        start = Offset(center.x, center.y - 50f),
        end = Offset(center.x + 20f, center.y - 74f),
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color.White, GoldYellow, Color.Transparent),
            center = Offset(center.x + 20f, center.y - 74f),
            radius = 12f * sparkPulse
        ),
        radius = 12f * sparkPulse,
        center = Offset(center.x + 20f, center.y - 74f)
    )
}

// 12. Whiz Wheel (Aerodynamic Aerial Spinner)
private fun DrawScope.drawWhizWheel(center: Offset, rotation: Float, sparkPulse: Float) {
    rotate(rotation, pivot = center) {
        // Outer glow
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF00B0FF).copy(alpha = 0.4f * sparkPulse), Color.Transparent),
                center = center,
                radius = 65f
            ),
            radius = 65f,
            center = center
        )

        // Central disc
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF40C4FF), Color(0xFF0288D1)),
                center = center,
                radius = 32f
            ),
            radius = 32f,
            center = center
        )

        // 3 Aerodynamic Propeller Wings
        for (i in 0 until 3) {
            val angle = Math.toRadians(i * 120.0)
            val wingEnd = Offset(
                (center.x + 50f * cos(angle)).toFloat(),
                (center.y + 50f * sin(angle)).toFloat()
            )
            drawLine(
                color = Color(0xFFFFAB00),
                start = center,
                end = wingEnd,
                strokeWidth = 9f,
                cap = StrokeCap.Round
            )
            drawCircle(
                color = Color.White,
                radius = 6f,
                center = wingEnd
            )
        }

        // Center hub
        drawCircle(color = Color(0xFFD50000), radius = 10f, center = center)
    }
}

// 13. Electric Fountain (Cold Pyro Silver Geyser)
private fun DrawScope.drawElectricFountain(center: Offset, sparkPulse: Float) {
    val fountainWidth = 48f
    val fountainHeight = 88f
    val fountainTop = center.y - fountainHeight / 2

    // Sleek Silver Metallic Cylinder
    drawRoundRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFB0BEC5), Color(0xFFECEFF1), Color(0xFFCFD8DC), Color(0xFF90A4AE))
        ),
        topLeft = Offset(center.x - fountainWidth / 2, fountainTop),
        size = Size(fountainWidth, fountainHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
    )

    // Electric Cyan Festive Banner
    drawRect(
        color = Color(0xFF00E5FF),
        topLeft = Offset(center.x - fountainWidth / 2, center.y - 12f),
        size = Size(fountainWidth, 24f)
    )
    drawRect(
        color = Color(0xFF00B0FF),
        topLeft = Offset(center.x - fountainWidth / 2, center.y - 4f),
        size = Size(fountainWidth, 8f)
    )

    // Star / Electric Spark symbol on cylinder
    drawCircle(color = Color.White, radius = 6f, center = center)

    // Nozzle Cone on top
    val conePath = Path().apply {
        moveTo(center.x, fountainTop - 12f)
        lineTo(center.x + fountainWidth / 2 - 4f, fountainTop)
        lineTo(center.x - fountainWidth / 2 + 4f, fountainTop)
        close()
    }
    drawPath(conePath, Color(0xFFECEFF1))

    // Crystalline Silver-White Spark Fountain at Nozzle
    for (i in 0 until 7) {
        val angle = Math.toRadians(270.0 + (i - 3) * 18.0)
        val len = (24f + (i % 3) * 8f) * sparkPulse
        drawLine(
            color = if (i % 2 == 0) Color.White else Color(0xFF80D8FF),
            start = Offset(center.x, fountainTop - 12f),
            end = Offset((center.x + len * cos(angle)).toFloat(), (fountainTop - 12f + len * sin(angle)).toFloat()),
            strokeWidth = 2.8f,
            cap = StrokeCap.Round
        )
    }
}

// 14. Sky Shots Multi-Tube Aerial Battery Cake
private fun DrawScope.drawSkyShots(center: Offset, sparkPulse: Float) {
    val boxWidth = 92f
    val boxHeight = 84f
    val boxTop = center.y - boxHeight / 2f + 8f

    // Ambient Golden Violet Glow
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Color(0xFFEA80FC).copy(alpha = 0.35f * sparkPulse), Color.Transparent),
            center = center,
            radius = boxWidth * 1.1f
        ),
        radius = boxWidth * 1.1f,
        center = center
    )

    // Base Shadow
    drawOval(
        color = Color(0x77000000),
        topLeft = Offset(center.x - boxWidth * 0.52f, boxTop + boxHeight - 8f),
        size = Size(boxWidth * 1.04f, 22f)
    )

    // Main Battery Box Body (Multi-layer gradient)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF6A1B9A), Color(0xFF4A148C), Color(0xFF311B92))
        ),
        topLeft = Offset(center.x - boxWidth / 2f, boxTop),
        size = Size(boxWidth, boxHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
    )

    // Outer Border
    drawRoundRect(
        color = GoldYellow,
        topLeft = Offset(center.x - boxWidth / 2f, boxTop),
        size = Size(boxWidth, boxHeight),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f),
        style = Stroke(width = 2.2f)
    )

    // Gold Foil Festive Ribbon Wrap
    drawRect(
        brush = Brush.horizontalGradient(
            colors = listOf(Color(0xFFFFD54F), Color(0xFFFFB300), Color(0xFFFFE082))
        ),
        topLeft = Offset(center.x - boxWidth / 2f, boxTop + 24f),
        size = Size(boxWidth, 20f)
    )
    drawRect(
        color = Color(0xFFD50000),
        topLeft = Offset(center.x - boxWidth / 2f, boxTop + 30f),
        size = Size(boxWidth, 8f)
    )

    // Decorative Stars on Gold Ribbon
    for (i in -2..2) {
        val sx = center.x + i * 18f
        val sy = boxTop + 34f
        drawCircle(color = Color.White, radius = 2.5f, center = Offset(sx, sy))
    }

    // Top Tube Battery Plate (Bevel perspective showing mortar tube mouths)
    val plateHeight = 24f
    drawOval(
        brush = Brush.verticalGradient(
            colors = listOf(Color(0xFF263238), Color(0xFF37474F))
        ),
        topLeft = Offset(center.x - boxWidth / 2f + 4f, boxTop - plateHeight / 2f),
        size = Size(boxWidth - 8f, plateHeight)
    )
    drawOval(
        color = GoldYellow,
        topLeft = Offset(center.x - boxWidth / 2f + 4f, boxTop - plateHeight / 2f),
        size = Size(boxWidth - 8f, plateHeight),
        style = Stroke(width = 1.5f)
    )

    // 7 Mortar Tube Bores (1 center + 6 hexagonal rosette)
    val tubeRadius = 5.2f
    // Center tube
    drawCircle(color = Color(0xFF111111), radius = tubeRadius, center = Offset(center.x, boxTop))
    drawCircle(color = Color(0xFFFFD54F), radius = tubeRadius, center = Offset(center.x, boxTop), style = Stroke(width = 1.2f))

    // 6 outer tubes
    val rosetteRadiusX = 26f
    val rosetteRadiusY = 7.5f
    for (i in 0 until 6) {
        val rad = Math.toRadians(i * 60.0)
        val tx = (center.x + rosetteRadiusX * cos(rad)).toFloat()
        val ty = (boxTop + rosetteRadiusY * sin(rad)).toFloat()
        drawCircle(color = Color(0xFF111111), radius = tubeRadius, center = Offset(tx, ty))
        drawCircle(color = Color(0xFFFFAB00), radius = tubeRadius, center = Offset(tx, ty), style = Stroke(width = 1.2f))
    }

    // Master Green Ignition Fuse sticking out from top-right
    val fuseBase = Offset(center.x + boxWidth / 2f - 14f, boxTop - 2f)
    val fuseTip = Offset(center.x + boxWidth / 2f + 10f, boxTop - 24f)
    drawLine(
        color = Color(0xFF43A047),
        start = fuseBase,
        end = fuseTip,
        strokeWidth = 3.5f,
        cap = StrokeCap.Round
    )
    // Sparkling fuse ember tip
    drawCircle(
        color = Color(0xFFFF6D00),
        radius = 4.5f * sparkPulse,
        center = fuseTip
    )
    drawCircle(
        color = Color.White,
        radius = 2.2f,
        center = fuseTip
    )
}



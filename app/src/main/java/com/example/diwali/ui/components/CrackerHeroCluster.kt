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
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp
import com.example.diwali.model.CrackerType
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow

/**
 * CrackerHeroCluster ("Fatakda no Dhaglo")
 *
 * Rich festive cluster of the game's actual animated vector crackers
 * (Anar, Rocket, Sky Shots, Sutli Bomb, Paper Bomb, Chakri, Sparkler, Ladi)
 * assembled together with a golden Diwali aura and twin traditional Diyas.
 */
@Composable
fun CrackerHeroCluster(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "HeroClusterAnim")

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseGlow"
    )

    val flameFlicker by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(220, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "FlameFlicker"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(265.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF220C38),
                        Color(0xFF140722),
                        Color(0xFF090212)
                    )
                )
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        GoldYellow.copy(alpha = 0.75f),
                        FestiveOrange.copy(alpha = 0.4f),
                        GoldLight.copy(alpha = 0.8f)
                    )
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Warm festive radial aura behind the cracker heap
        Canvas(modifier = Modifier.fillMaxSize()) {
            val centerOffset = Offset(size.width / 2f, size.height * 0.52f)
            // Golden halo glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        GoldYellow.copy(alpha = 0.32f * pulseGlow),
                        FestiveOrange.copy(alpha = 0.18f * pulseGlow),
                        Color.Transparent
                    ),
                    center = centerOffset,
                    radius = size.width * 0.52f
                ),
                radius = size.width * 0.52f,
                center = centerOffset
            )

            // Sparkle dust specks around the heap
            val sparkPoints = listOf(
                Offset(size.width * 0.15f, size.height * 0.14f),
                Offset(size.width * 0.32f, size.height * 0.08f),
                Offset(size.width * 0.50f, size.height * 0.06f),
                Offset(size.width * 0.68f, size.height * 0.09f),
                Offset(size.width * 0.85f, size.height * 0.15f),
                Offset(size.width * 0.08f, size.height * 0.45f),
                Offset(size.width * 0.92f, size.height * 0.48f),
                Offset(size.width * 0.12f, size.height * 0.78f),
                Offset(size.width * 0.88f, size.height * 0.80f)
            )
            for ((idx, pt) in sparkPoints.withIndex()) {
                val pPhase = ((pulseGlow + idx * 0.12f) % 1f)
                drawCircle(
                    color = Color.White.copy(alpha = (pPhase * 0.85f).coerceIn(0.1f, 0.95f)),
                    radius = 2.4f * pPhase + 1.2f,
                    center = pt
                )
            }
        }

        // ==========================================
        // 2. TOP / BACK LAYER: Standing Rockets, Sky Shots, and Sparklers
        // ==========================================

        // Center Tall Rocket (peaks high in the center back)
        Box(
            modifier = Modifier
                .size(108.dp)
                .offset(x = 0.dp, y = (-72).dp)
                .rotate(2f)
                .scale(0.95f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.ROCKET)
        }

        // Left Tall Rocket (angled left)
        Box(
            modifier = Modifier
                .size(102.dp)
                .offset(x = (-98).dp, y = (-62).dp)
                .rotate(-22f)
                .scale(0.92f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.ROCKET)
        }

        // Right Tall Rocket (angled right)
        Box(
            modifier = Modifier
                .size(102.dp)
                .offset(x = 98.dp, y = (-62).dp)
                .rotate(22f)
                .scale(0.92f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.ROCKET)
        }

        // Sky Shots Cake Left
        Box(
            modifier = Modifier
                .size(92.dp)
                .offset(x = (-54).dp, y = (-48).dp)
                .rotate(-8f)
                .scale(0.88f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.SKY_SHOTS)
        }

        // Sky Shots Cake Right
        Box(
            modifier = Modifier
                .size(92.dp)
                .offset(x = 54.dp, y = (-48).dp)
                .rotate(8f)
                .scale(0.88f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.SKY_SHOTS)
        }

        // Left Far Sparkler (Phooljhadi radiating outward)
        Box(
            modifier = Modifier
                .size(86.dp)
                .offset(x = (-122).dp, y = (-12).dp)
                .rotate(-32f)
                .scale(0.82f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.PHOOLJHADI)
        }

        // Right Far Sparkler (Phooljhadi radiating outward)
        Box(
            modifier = Modifier
                .size(86.dp)
                .offset(x = 122.dp, y = (-12).dp)
                .rotate(32f)
                .scale(0.82f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.PHOOLJHADI)
        }

        // ==========================================
        // 3. MID LAYER: Royal Anars, Ladi Strings & Bomb Clusters
        // ==========================================

        // Center Royal Anar (Majestic cone standing tall)
        Box(
            modifier = Modifier
                .size(126.dp)
                .offset(x = 0.dp, y = (-10).dp)
                .scale(1.02f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.ANAR)
        }

        // Left Supporting Anar
        Box(
            modifier = Modifier
                .size(98.dp)
                .offset(x = (-72).dp, y = 2.dp)
                .rotate(-10f)
                .scale(0.90f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.ANAR)
        }

        // Right Supporting Anar
        Box(
            modifier = Modifier
                .size(98.dp)
                .offset(x = 72.dp, y = 2.dp)
                .rotate(10f)
                .scale(0.90f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.ANAR)
        }

        // Red Ladi Draped Left
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = (-116).dp, y = 42.dp)
                .rotate(-24f)
                .scale(0.88f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.LADI)
        }

        // Red Ladi Draped Right
        Box(
            modifier = Modifier
                .size(90.dp)
                .offset(x = 116.dp, y = 42.dp)
                .rotate(24f)
                .scale(0.88f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.LADI)
        }

        // ==========================================
        // 4. FRONT / FOREGROUND LAYER: Packed Sutli Bombs, Paper Bombs & Spinning Chakris
        // ==========================================

        // Mid-Left Classic Sutli Bomb
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = (-56).dp, y = 46.dp)
                .rotate(-14f)
                .scale(0.92f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.SUTLI_BOMB)
        }

        // Mid-Right Crisp Paper Bomb
        Box(
            modifier = Modifier
                .size(80.dp)
                .offset(x = 56.dp, y = 46.dp)
                .rotate(14f)
                .scale(0.92f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.PAPER_BOMB)
        }

        // Near-Center Heavy Sutli Bomb
        Box(
            modifier = Modifier
                .size(86.dp)
                .offset(x = (-26).dp, y = 62.dp)
                .rotate(6f)
                .scale(0.96f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.SUTLI_BOMB)
        }

        // Near-Center Red Paper Bomb
        Box(
            modifier = Modifier
                .size(86.dp)
                .offset(x = 26.dp, y = 62.dp)
                .rotate(-6f)
                .scale(0.96f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.PAPER_BOMB)
        }

        // Left Spinning Ground Chakri
        Box(
            modifier = Modifier
                .size(68.dp)
                .offset(x = (-84).dp, y = 74.dp)
                .scale(0.88f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.CHAKRI)
        }

        // Right Spinning Ground Chakri
        Box(
            modifier = Modifier
                .size(68.dp)
                .offset(x = 84.dp, y = 74.dp)
                .scale(0.88f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.CHAKRI)
        }

        // Centerpiece Spinning Chakri (Sitting right at the front base)
        Box(
            modifier = Modifier
                .size(84.dp)
                .offset(x = 0.dp, y = 76.dp)
                .scale(1.02f),
            contentAlignment = Alignment.Center
        ) {
            CrackerArtView(type = CrackerType.CHAKRI)
        }

        // ==========================================
        // 5. TRADITIONAL DECORATIVE DIYAS (Bottom corners)
        // ==========================================
        // Left Diya
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 10.dp, bottom = 8.dp)
                .size(width = 38.dp, height = 28.dp)
        ) {
            DiyaCanvas(flameFlicker = flameFlicker)
        }

        // Right Diya
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 10.dp, bottom = 8.dp)
                .size(width = 38.dp, height = 28.dp)
        ) {
            DiyaCanvas(flameFlicker = flameFlicker)
        }
    }
}

/**
 * Hand-drawn golden clay Diya with glowing flickering flame.
 */
@Composable
private fun DiyaCanvas(flameFlicker: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        // Base clay lamp bowl
        val bowlPath = Path().apply {
            moveTo(w * 0.12f, h * 0.58f)
            cubicTo(
                w * 0.12f, h * 0.95f,
                w * 0.88f, h * 0.95f,
                w * 0.88f, h * 0.58f
            )
            close()
        }
        drawPath(
            path = bowlPath,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFE65100), Color(0xFFBF360C)),
                startY = h * 0.58f,
                endY = h * 0.95f
            )
        )

        // Golden rim
        drawOval(
            brush = Brush.horizontalGradient(
                listOf(GoldYellow, GoldLight, GoldYellow)
            ),
            topLeft = Offset(w * 0.10f, h * 0.50f),
            size = Size(w * 0.80f, h * 0.18f)
        )

        // Glowing aura around flame
        drawCircle(
            brush = Brush.radialGradient(
                listOf(
                    GoldYellow.copy(alpha = 0.55f * flameFlicker),
                    Color.Transparent
                ),
                center = Offset(w * 0.5f, h * 0.32f),
                radius = w * 0.35f * flameFlicker
            ),
            radius = w * 0.35f * flameFlicker,
            center = Offset(w * 0.5f, h * 0.32f)
        )

        // Diya Flame (teardrop)
        val flamePath = Path().apply {
            moveTo(w * 0.5f, h * (0.05f / flameFlicker))
            cubicTo(
                w * 0.70f, h * 0.28f,
                w * 0.65f, h * 0.54f,
                w * 0.5f, h * 0.54f
            )
            cubicTo(
                w * 0.35f, h * 0.54f,
                w * 0.30f, h * 0.28f,
                w * 0.5f, h * (0.05f / flameFlicker)
            )
            close()
        }
        drawPath(
            path = flamePath,
            brush = Brush.verticalGradient(
                listOf(Color.White, GoldYellow, FestiveOrange),
                startY = h * 0.05f,
                endY = h * 0.54f
            )
        )
    }
}

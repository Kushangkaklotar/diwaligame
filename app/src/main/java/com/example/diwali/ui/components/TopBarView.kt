package com.example.diwali.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.FestiveRed
import com.example.diwali.ui.theme.GoldDark
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow

@Composable
fun TopBarView(
    coins: Int,
    isSoundEnabled: Boolean,
    onSoundToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coinScaleAnim = remember { Animatable(1f) }

    LaunchedEffect(coins) {
        coinScaleAnim.animateTo(
            targetValue = 1.35f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessHigh
            )
        )
        coinScaleAnim.animateTo(
            targetValue = 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        // 1. Animated Diwali Fairy Lights Garland across the top
        GarlandFairyLights()

        // 2. Main Top Row: Game Title & Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Title & Festive Arcade Pill
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DHAMAKA",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp,
                        color = GoldYellow
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(FestiveOrange, FestiveRed)
                                )
                            )
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "FESTIVAL",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
                Text(
                    text = "3D Fireworks Arcade",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

        // Right side controls: Sound + Shiny Coin Pill Counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sound button (Vector speaker)
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0x55000000))
                    .border(1.5.dp, GoldYellow.copy(alpha = 0.4f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onSoundToggle
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                    val w = size.width
                    val h = size.height
                    val body = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.15f, h * 0.35f)
                        lineTo(w * 0.40f, h * 0.35f)
                        lineTo(w * 0.70f, h * 0.15f)
                        lineTo(w * 0.70f, h * 0.85f)
                        lineTo(w * 0.40f, h * 0.65f)
                        lineTo(w * 0.15f, h * 0.65f)
                        close()
                    }
                    drawPath(body, color = if (isSoundEnabled) GoldYellow else Color.White.copy(alpha = 0.5f))
                    if (!isSoundEnabled) {
                        drawLine(
                            color = com.example.diwali.ui.theme.FestiveRed,
                            start = Offset(w * 0.18f, h * 0.82f),
                            end = Offset(w * 0.86f, h * 0.18f),
                            strokeWidth = 2.4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    } else {
                        drawArc(
                            color = GoldYellow,
                            startAngle = -45f,
                            sweepAngle = 90f,
                            useCenter = false,
                            topLeft = Offset(w * 0.52f, h * 0.25f),
                            size = androidx.compose.ui.geometry.Size(w * 0.38f, h * 0.5f),
                            style = androidx.compose.ui.graphics.drawscope.Stroke(
                                width = 2.dp.toPx(),
                                cap = androidx.compose.ui.graphics.StrokeCap.Round
                            )
                        )
                    }
                }
            }

            // Shiny 3D Coin Badge (Display Only - No Cheat Increment on Click)
            Box(
                modifier = Modifier
                    .scale(coinScaleAnim.value)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF2E1906),
                                Color(0xFF563009)
                            )
                        )
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(GoldLight, GoldDark, GoldLight)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vector Gold Coin
                    androidx.compose.foundation.Canvas(modifier = Modifier.size(18.dp)) {
                        val r = size.minDimension / 2f
                        drawCircle(
                            brush = Brush.radialGradient(listOf(GoldLight, GoldYellow, GoldDark)),
                            radius = r,
                            center = center
                        )
                        drawCircle(
                            color = Color.White.copy(alpha = 0.7f),
                            radius = r * 0.72f,
                            center = center,
                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.4.dp.toPx())
                        )
                        drawCircle(
                            color = Color(0xFFFFD54F),
                            radius = r * 0.32f,
                            center = center
                        )
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    Text(
                        text = "$coins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldLight
                    )
                }
            }
            }
        }
    }
}

@Composable
fun GarlandFairyLights(modifier: Modifier = Modifier) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "FairyLights")
    val lightGlow by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1200, easing = androidx.compose.animation.core.FastOutSlowInEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "LightGlow"
    )

    androidx.compose.foundation.Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(18.dp)
    ) {
        val w = size.width
        val bulbColors = listOf(
            Color(0xFFFF1744), // Ruby Red
            Color(0xFFFFD600), // Gold
            Color(0xFF00E676), // Emerald
            Color(0xFF00E5FF), // Cyan
            Color(0xFFFF4081), // Pink
            Color(0xFF7C4DFF), // Purple
            Color(0xFFFF9100), // Orange
            Color(0xFF76FF03), // Lime
            Color(0xFFFFD600)  // Gold
        )
        val bulbCount = bulbColors.size
        val step = w / (bulbCount + 1)

        // Draw drooped hanging string wire
        val wirePath = androidx.compose.ui.graphics.Path().apply {
            moveTo(0f, 2f)
            for (i in 0..bulbCount) {
                val x1 = i * step
                val x2 = (i + 1) * step
                val midX = (x1 + x2) / 2f
                quadraticTo(midX, 10f, x2, 2f)
            }
        }
        drawPath(
            path = wirePath,
            color = Color(0x66FFFFFF),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 1.2.dp.toPx())
        )

        // Draw glowing bulb at each junction
        for (i in 0 until bulbCount) {
            val bx = (i + 1) * step
            val by = 9f
            val color = bulbColors[i]
            val pulse = if (i % 2 == 0) lightGlow else (1.5f - lightGlow)

            // Soft glow halo
            drawCircle(
                color = color.copy(alpha = 0.35f * pulse),
                radius = 8.dp.toPx() * pulse,
                center = Offset(bx, by)
            )
            // Bulb socket
            drawRect(
                color = Color(0xFF37474F),
                topLeft = Offset(bx - 1.5.dp.toPx(), by - 4.dp.toPx()),
                size = androidx.compose.ui.geometry.Size(3.dp.toPx(), 3.dp.toPx())
            )
            // Vibrant bulb core
            drawCircle(
                brush = Brush.radialGradient(listOf(Color.White, color)),
                radius = 3.6.dp.toPx(),
                center = Offset(bx, by + 1.dp.toPx())
            )
        }
    }
}

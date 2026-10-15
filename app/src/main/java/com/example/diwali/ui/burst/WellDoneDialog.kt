package com.example.diwali.ui.burst

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.model.CrackerItem
import com.example.diwali.ui.theme.GoldDark
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow

@Composable
fun WellDoneDialog(
    cracker: CrackerItem,
    earnedCoins: Int,
    canPlayNext: Boolean = true,
    coinsNeededForNext: Int = 0,
    onNextClick: () -> Unit,
    onRetryClick: () -> Unit,
    onHomeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(0.4f) }
    val trophyBounce = remember { Animatable(0.8f) }

    LaunchedEffect(Unit) {
        scaleAnim.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
        trophyBounce.animateTo(
            targetValue = 1.15f,
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        )
        trophyBounce.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xCC000000)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scaleAnim.value)
                .padding(horizontal = 32.dp)
        ) {
            // "WELL DONE!" Celebration Title (like in the video)
            Text(
                text = "WELL DONE!",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = GoldYellow,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Vector Golden Trophy
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(trophyBounce.value),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(76.dp)) {
                    val w = size.width
                    val h = size.height
                    // Handles
                    drawArc(
                        color = GoldYellow,
                        startAngle = 90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.12f, h * 0.22f),
                        size = Size(w * 0.25f, h * 0.28f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    drawArc(
                        color = GoldYellow,
                        startAngle = -90f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = Offset(w * 0.63f, h * 0.22f),
                        size = Size(w * 0.25f, h * 0.28f),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    // Cup
                    val cup = Path().apply {
                        moveTo(w * 0.25f, h * 0.15f)
                        lineTo(w * 0.75f, h * 0.15f)
                        lineTo(w * 0.65f, h * 0.55f)
                        cubicTo(w * 0.65f, h * 0.70f, w * 0.35f, h * 0.70f, w * 0.35f, h * 0.55f)
                        close()
                    }
                    drawPath(cup, brush = Brush.verticalGradient(listOf(GoldLight, GoldYellow, Color(0xFFC67C00))))
                    // Stem
                    drawRect(color = GoldYellow, topLeft = Offset(w * 0.44f, h * 0.68f), size = Size(w * 0.12f, h * 0.14f))
                    // Base
                    drawRoundRect(
                        color = Color(0xFFFFB300),
                        topLeft = Offset(w * 0.28f, h * 0.82f),
                        size = Size(w * 0.44f, h * 0.10f),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                    // Center Star
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = Offset(w * 0.5f, h * 0.36f))
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Coins Awarded Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2E1906), Color(0xFF4E2B08))
                        )
                    )
                    .border(2.dp, GoldLight, RoundedCornerShape(20.dp))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val r = size.minDimension / 2f
                        drawCircle(brush = Brush.radialGradient(listOf(GoldLight, GoldYellow, Color(0xFFC67C00))), radius = r, center = center)
                        drawCircle(color = Color.White.copy(alpha = 0.7f), radius = r * 0.7f, center = center, style = Stroke(width = 1.4.dp.toPx()))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+$earnedCoins COINS EARNED!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (canPlayNext) {
                // NEXT Button (Vibrant Green) - Visible ONLY when player has enough coins or already unlocked
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.72f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF76FF03), Color(0xFF00E676))
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onNextClick
                        )
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "NEXT",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF1B5E20)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            } else {
                // When NEXT is not affordable, completely hide NEXT and guide player to replay
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.85f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xBB261405))
                        .border(1.5.dp, Color(0xFFFFB300).copy(alpha = 0.75f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "NEXT STAGE LOCKED",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldYellow
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Need $coinsNeededForNext more coins to unlock next stage.\nReplay this stage to earn more coins!",
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.90f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // RETRY & HOME Buttons Row (Purple & Blue)
            Row(
                modifier = Modifier.fillMaxWidth(0.72f),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // RETRY Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF7E57C2), Color(0xFF512DA8))
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onRetryClick
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RETRY",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // HOME Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF42A5F5), Color(0xFF1565C0))
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onHomeClick
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "HOME",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

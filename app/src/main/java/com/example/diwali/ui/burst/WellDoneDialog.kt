package com.example.diwali.ui.burst

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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

            // Trophy 🏆
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .scale(trophyBounce.value),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🏆",
                    fontSize = 72.sp
                )
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
                    Text(text = "🪙", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "+$earnedCoins COINS EARNED!",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // NEXT Button (Vibrant Green)
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

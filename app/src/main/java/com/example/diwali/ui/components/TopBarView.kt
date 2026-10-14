package com.example.diwali.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
    onBonusClick: () -> Unit,
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

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // App Title & Festive Greeting
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "🎆 દિવાળી ધમાકા",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
            Text(
                text = "Diwali Fireworks Simulator",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = GoldLight.copy(alpha = 0.85f)
            )
        }

        // Right side controls: Sound + Shiny Coin Pill Counter
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sound button
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
                Text(
                    text = if (isSoundEnabled) "🔊" else "🔇",
                    fontSize = 17.sp
                )
            }

            // Shiny 3D Coin Badge
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
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBonusClick
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 3D Coin Icon
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(GoldLight, GoldYellow, GoldDark)
                                )
                            )
                            .border(1.dp, Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🪙",
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = "$coins",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldLight
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Plus button for quick bonus
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(FestiveOrange, FestiveRed)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

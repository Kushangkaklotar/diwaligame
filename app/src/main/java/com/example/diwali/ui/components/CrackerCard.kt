package com.example.diwali.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.model.CrackerItem
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.FestiveRed
import com.example.diwali.ui.theme.GoldDark
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow

@Composable
fun CrackerCard(
    cracker: CrackerItem,
    isUnlocked: Boolean,
    userCoins: Int,
    onBurstClick: (CrackerItem) -> Unit,
    onUnlockClick: (CrackerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CardButtonAnim")
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ButtonPulse"
    )

    val canAfford = userCoins >= cracker.price

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xF0221646),
                        Color(0xF0120B28)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        cracker.glowColor.copy(alpha = 0.85f),
                        Color(0x44FFD54F),
                        cracker.glowColor.copy(alpha = 0.5f)
                    )
                ),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Header Row: Badge & Star Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(cracker.primaryColor.copy(alpha = 0.35f))
                        .border(1.dp, cracker.primaryColor, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = cracker.badgeText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Boom Stars
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(cracker.boomStars) {
                        Text(
                            text = "⭐",
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Animated Art Illustration of the Firecracker (Enlarged)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(205.dp)
                    .scale(1.08f)
            ) {
                CrackerArtView(type = cracker.type)
            }

            Spacer(modifier = Modifier.height(4.dp))

            // English Name & Category
            Text(
                text = cracker.name,
                fontSize = 19.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = cracker.category,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = GoldLight,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // English Description
            Text(
                text = cracker.description,
                fontSize = 11.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFFDCDCDC),
                textAlign = TextAlign.Center,
                lineHeight = 15.sp,
                maxLines = 2,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reward pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x33000000))
                    .border(1.dp, GoldYellow.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(brush = Brush.radialGradient(listOf(GoldLight, GoldYellow, Color(0xFFC67C00))))
                    }
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Earn +${cracker.rewardCoins} Coins on Burst!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldYellow
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Action Button (Burst or Unlock)
            if (isUnlocked) {
                // BIG BURST BUTTON
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(buttonPulse)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    FestiveOrange,
                                    FestiveRed,
                                    Color(0xFFFF1744)
                                )
                            )
                        )
                        .border(1.5.dp, GoldLight, RoundedCornerShape(20.dp))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onBurstClick(cracker) }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "BURST NOW",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            } else {
                // UNLOCK BUTTON WITH PRICE
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            brush = if (canAfford) {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF2E7D32),
                                        Color(0xFF43A047)
                                    )
                                )
                            } else {
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xFF424242),
                                        Color(0xFF616161)
                                    )
                                )
                            }
                        )
                        .border(
                            width = 1.5.dp,
                            color = if (canAfford) GoldYellow else Color.Gray,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .clickable(
                            enabled = canAfford,
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onUnlockClick(cracker) }
                        )
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (canAfford) {
                            "UNLOCK (${cracker.price} COINS)"
                        } else {
                            "NEED ${cracker.price} COINS TO UNLOCK"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color.White else Color(0xFFBDBDBD)
                    )
                }
            }
        }
    }
}

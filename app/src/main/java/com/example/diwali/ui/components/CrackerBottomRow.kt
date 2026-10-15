package com.example.diwali.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.model.CrackerItem
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.FestiveRed
import com.example.diwali.ui.theme.GoldDark
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow

@Composable
fun CrackerBottomRow(
    crackers: List<CrackerItem>,
    unlockedIds: Set<String>,
    userCoins: Int,
    selectedCrackerId: String,
    listState: LazyListState,
    onCrackerClick: (CrackerItem) -> Unit,
    onUnlockClick: (CrackerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    // Width calibrated so 2 full cards + half of 3rd card are visible on any screen width
    val cardWidth = ((configuration.screenWidthDp.dp - 32.dp - 18.dp) / 2.32f).coerceAtLeast(130.dp)

    LazyRow(
        state = listState,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        items(crackers, key = { it.id }) { cracker ->
            val isUnlocked = unlockedIds.contains(cracker.id)
            val isSelected = cracker.id == selectedCrackerId

            CrackerCardCompact(
                cracker = cracker,
                isUnlocked = isUnlocked,
                isSelected = isSelected,
                canAfford = userCoins >= cracker.price,
                cardWidth = cardWidth,
                onClick = {
                    if (isUnlocked) {
                        onCrackerClick(cracker)
                    } else {
                        onUnlockClick(cracker)
                    }
                }
            )
        }
    }
}

@Composable
private fun CrackerCardCompact(
    cracker: CrackerItem,
    isUnlocked: Boolean,
    isSelected: Boolean,
    canAfford: Boolean,
    cardWidth: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SelectedPulse")
    val borderPulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BorderPulse"
    )

    val borderColor = when {
        isSelected -> GoldYellow.copy(alpha = borderPulse)
        isUnlocked -> cracker.glowColor.copy(alpha = 0.5f)
        else -> Color.Gray.copy(alpha = 0.35f)
    }

    val borderWidth = if (isSelected) 2.dp else 1.2.dp

    Box(
        modifier = Modifier
            .width(cardWidth)
            .height(184.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (isSelected) {
                        listOf(Color(0xF03B1E6D), Color(0xF01D0F38))
                    } else {
                        listOf(Color(0xEE241445), Color(0xEE140A26))
                    }
                )
            )
            .border(borderWidth, borderColor, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Accent Ribbon Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(26.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                cracker.primaryColor.copy(alpha = 0.85f),
                                cracker.secondaryColor.copy(alpha = 0.70f)
                            )
                        )
                    )
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = cracker.badgeText.uppercase(),
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                    // 3 Tiny Vector Golden Stars
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(cracker.boomStars.coerceAtMost(3)) {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(6.dp)) {
                                drawCircle(color = GoldYellow)
                            }
                        }
                    }
                }
            }

            // Cracker Art Illustration with Radial Spotlight Glow
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(104.dp)
            ) {
                // Radial Glow
                androidx.compose.foundation.Canvas(modifier = Modifier.size(80.dp)) {
                    drawCircle(
                        brush = Brush.radialGradient(
                            listOf(
                                cracker.glowColor.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        ),
                        radius = size.width / 2f
                    )
                }

                CrackerArtView(type = cracker.type)

                // Lock Overlay if not unlocked yet
                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x99000000)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Vector Padlock
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                                val w = size.width
                                val h = size.height
                                // Shackle
                                drawArc(
                                    color = GoldYellow,
                                    startAngle = 180f,
                                    sweepAngle = 180f,
                                    useCenter = false,
                                    topLeft = Offset(w * 0.25f, h * 0.12f),
                                    size = androidx.compose.ui.geometry.Size(w * 0.50f, h * 0.45f),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.5.dp.toPx())
                                )
                                // Body
                                drawRoundRect(
                                    color = if (canAfford) GoldYellow else Color(0xFFB0BEC5),
                                    topLeft = Offset(w * 0.18f, h * 0.40f),
                                    size = androidx.compose.ui.geometry.Size(w * 0.64f, h * 0.48f),
                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx(), 4.dp.toPx())
                                )
                                // Keyhole
                                drawCircle(
                                    color = Color(0xFF1A1A1A),
                                    radius = 2.dp.toPx(),
                                    center = Offset(w * 0.5f, h * 0.62f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (canAfford) FestiveOrange else Color.DarkGray)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${cracker.price} COINS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // English Name
            Text(
                text = cracker.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = if (isSelected) GoldLight else Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Category Subtitle & Action Tag
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isUnlocked) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                if (isSelected) {
                                    Brush.horizontalGradient(listOf(FestiveOrange, FestiveRed))
                                } else {
                                    Brush.horizontalGradient(listOf(Color(0xFF2E7D32), Color(0xFF1B5E20)))
                                }
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isSelected) "SELECTED" else "BLAST ▶",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                } else {
                    Text(
                        text = cracker.category,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Normal,
                        color = GoldYellow.copy(alpha = 0.85f),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

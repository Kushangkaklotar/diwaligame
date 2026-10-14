package com.example.diwali.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.model.CrackerItem
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldYellow
import kotlinx.coroutines.launch
import kotlin.math.absoluteValue

@Composable
fun CrackerPager(
    crackers: List<CrackerItem>,
    unlockedIds: Set<String>,
    userCoins: Int,
    onBurstClick: (CrackerItem) -> Unit,
    onUnlockClick: (CrackerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(pageCount = { crackers.size })
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Horizontal Pager with 3D scaling card effect
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 42.dp),
            pageSpacing = 16.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val cracker = crackers[page]
            val isUnlocked = unlockedIds.contains(cracker.id)

            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)

            val cardScale = 1f - (pageOffset * 0.12f)
            val cardAlpha = 1f - (pageOffset * 0.40f)

            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = cardScale
                        scaleY = cardScale
                        alpha = cardAlpha
                    }
            ) {
                CrackerCard(
                    cracker = cracker,
                    isUnlocked = isUnlocked,
                    userCoins = userCoins,
                    onBurstClick = onBurstClick,
                    onUnlockClick = onUnlockClick
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pager Controls: Left Arrow, Diya Dots, Right Arrow
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            // Left Quick Arrow Button for young children
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x44000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = pagerState.currentPage > 0
                    ) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage - 1)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "◀",
                    fontSize = 14.sp,
                    color = if (pagerState.currentPage > 0) GoldYellow else Color.Gray.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Diya / Indicator Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(crackers.size) { index ->
                    val isSelected = pagerState.currentPage == index
                    if (isSelected) {
                        // Elongated glowing pill for selected page
                        Box(
                            modifier = Modifier
                                .width(28.dp)
                                .height(9.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(FestiveOrange)
                                .border(1.dp, GoldYellow, RoundedCornerShape(6.dp))
                        )
                    } else {
                        // Inactive dot
                        Box(
                            modifier = Modifier
                                .size(9.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.35f))
                                .clickable {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Right Quick Arrow Button
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0x44000000))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = pagerState.currentPage < crackers.size - 1
                    ) {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▶",
                    fontSize = 14.sp,
                    color = if (pagerState.currentPage < crackers.size - 1) GoldYellow else Color.Gray.copy(alpha = 0.5f)
                )
            }
        }
    }
}

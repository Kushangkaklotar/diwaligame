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
        // Horizontal Pager showing ~3 cards in one line (focused center + adjacent peeks)
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 64.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val cracker = crackers[page]
            val isUnlocked = unlockedIds.contains(cracker.id)

            val pageOffset = (
                (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction
            ).absoluteValue.coerceIn(0f, 1f)

            val cardScale = 1f - (pageOffset * 0.14f)
            val cardAlpha = 1f - (pageOffset * 0.35f)

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

        // Diya / Indicator Dots (Centered without arrow buttons)
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(crackers.size) { index ->
                val isSelected = pagerState.currentPage == index
                if (isSelected) {
                    // Elongated glowing pill for selected page
                    Box(
                        modifier = Modifier
                            .width(22.dp)
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(FestiveOrange)
                            .border(1.dp, GoldYellow, RoundedCornerShape(4.dp))
                    )
                } else {
                    // Inactive dot
                    Box(
                        modifier = Modifier
                            .size(6.dp)
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
    }
}

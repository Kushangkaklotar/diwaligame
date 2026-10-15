package com.example.diwali.ui.levels

import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerLevelConfig
import com.example.diwali.model.CrackerRepository
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.components.CrackerArtView
import com.example.diwali.ui.theme.DiwaliNightBottom
import com.example.diwali.ui.theme.DiwaliNightMid
import com.example.diwali.ui.theme.DiwaliNightTop
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.FestiveRed
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow
import kotlinx.coroutines.launch

@Composable
fun LevelsScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    initialCracker: CrackerItem? = null,
    onBack: () -> Unit,
    onPlayLevel: (CrackerItem, Int) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val coins by preferences.coinsFlow.collectAsState()
    val isSoundEnabled by preferences.isSoundEnabled.collectAsState()
    val levelsRevision by preferences.levelsRevision.collectAsState()

    val allCrackers = CrackerRepository.crackers
    val rightListState = rememberLazyListState()
    val leftRailState = rememberLazyListState()

    // Handle system back gesture / button to return to Home Screen
    BackHandler {
        onBack()
    }

    var selectedIndex by remember {
        val initialIdx = allCrackers.indexOfFirst { it.id == initialCracker?.id }.takeIf { it >= 0 } ?: 0
        mutableStateOf(initialIdx)
    }
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Scroll to initial cracker on entry
    LaunchedEffect(Unit) {
        if (selectedIndex > 0) {
            rightListState.scrollToItem(selectedIndex)
            leftRailState.scrollToItem(selectedIndex)
        }
    }

    // Synchronize left rail highlight with right list visible section strictly during user drag
    LaunchedEffect(rightListState) {
        snapshotFlow {
            Pair(rightListState.firstVisibleItemIndex, rightListState.isScrollInProgress)
        }.collect { (visibleIdx, isScrolling) ->
            if (isScrolling && !isProgrammaticScroll && visibleIdx in allCrackers.indices) {
                if (selectedIndex != visibleIdx) {
                    selectedIndex = visibleIdx
                    leftRailState.animateScrollToItem((visibleIdx - 1).coerceAtLeast(0))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(DiwaliNightTop, DiwaliNightMid, DiwaliNightBottom)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // 1. Top Bar: Back Button, Title, and Coins Display
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vector Back Arrow (never cut off)
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0x77000000))
                        .border(1.5.dp, GoldYellow.copy(alpha = 0.6f), CircleShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                soundManager.playClickSound(isSoundEnabled)
                                onBack()
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(18.dp)) {
                        val stroke = 2.6.dp.toPx()
                        val arrowColor = Color.White
                        drawLine(
                            color = arrowColor,
                            start = Offset(size.width * 0.85f, size.height * 0.5f),
                            end = Offset(size.width * 0.18f, size.height * 0.5f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = arrowColor,
                            start = Offset(size.width * 0.52f, size.height * 0.18f),
                            end = Offset(size.width * 0.18f, size.height * 0.5f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                        drawLine(
                            color = arrowColor,
                            start = Offset(size.width * 0.52f, size.height * 0.82f),
                            end = Offset(size.width * 0.18f, size.height * 0.5f),
                            strokeWidth = stroke,
                            cap = StrokeCap.Round
                        )
                    }
                }

                // Screen Title
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "CRACKER LEVELS",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldYellow,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "25 Stages Per Firework • 3x Unlock Economy",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.75f)
                    )
                }

                // Coins Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0x77000000))
                        .border(1.5.dp, GoldYellow.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Vector Gold Coin
                        Canvas(modifier = Modifier.size(14.dp)) {
                            val r = size.minDimension / 2f
                            drawCircle(
                                brush = Brush.radialGradient(listOf(GoldLight, GoldYellow, Color(0xFFC67C00))),
                                radius = r,
                                center = center
                            )
                            drawCircle(
                                color = Color.White.copy(alpha = 0.7f),
                                radius = r * 0.72f,
                                center = center,
                                style = Stroke(width = 1.2.dp.toPx())
                            )
                        }
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "$coins",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow
                        )
                    }
                }
            }

            // 2. Main Body: Left Vertical Sidebar + Right Continuous Vertical Level Scroll
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp)
            ) {
                // LEFT SIDEBAR: Vertical Category Rail with all Crackers (Expanded width to prevent clipping)
                Box(
                    modifier = Modifier
                        .width(96.dp)
                        .fillMaxHeight()
                        .background(Color(0x550C061A))
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                listOf(
                                    GoldYellow.copy(alpha = 0.5f),
                                    GoldYellow.copy(alpha = 0.15f),
                                    GoldYellow.copy(alpha = 0.4f)
                                )
                            ),
                            shape = RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
                        )
                        .clip(RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp))
                ) {
                    LazyColumn(
                        state = leftRailState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 54.dp, start = 4.dp, end = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        itemsIndexed(allCrackers) { index, cracker ->
                            val isSelected = index == selectedIndex
                            val crackerLvl = preferences.getUnlockedLevel(cracker.id)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) {
                                            Brush.verticalGradient(
                                                listOf(
                                                    FestiveOrange.copy(alpha = 0.95f),
                                                    FestiveRed.copy(alpha = 0.95f)
                                                )
                                            )
                                        } else {
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color(0x44261747),
                                                    Color(0x44140B28)
                                                )
                                            )
                                        }
                                    )
                                    .border(
                                        width = if (isSelected) 1.8.dp else 0.8.dp,
                                        color = if (isSelected) GoldYellow else Color.White.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            soundManager.playClickSound(isSoundEnabled)
                                            selectedIndex = index
                                            coroutineScope.launch {
                                                isProgrammaticScroll = true
                                                rightListState.animateScrollToItem(index)
                                                isProgrammaticScroll = false
                                            }
                                        }
                                    )
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    // Cracker Icon (Dynamically scaled inside 38dp box without clipping)
                                    Box(
                                        modifier = Modifier.size(38.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CrackerArtView(type = cracker.type)
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    // Short Name with 2-line wrap
                                    Text(
                                        text = cracker.name.replace(" (Anar)", "").replace(" (Pencil)", ""),
                                        fontSize = 9.sp,
                                        lineHeight = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Level Pill
                                    Box(
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0x88000000))
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text(
                                            text = "L$crackerLvl/25",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) GoldLight else GoldYellow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // RIGHT CONTENT AREA: Continuous Vertical Scroll of All Cracker Sections
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .padding(start = 8.dp, end = 10.dp)
                ) {
                    LazyColumn(
                        state = rightListState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        itemsIndexed(allCrackers) { crackerIndex, cracker ->
                            val maxUnlockedLevel = remember(cracker.id, levelsRevision) {
                                preferences.getUnlockedLevel(cracker.id)
                            }
                            val completedLevel = remember(cracker.id, levelsRevision) {
                                preferences.getCompletedLevel(cracker.id)
                            }

                            CrackerLevelsSection(
                                cracker = cracker,
                                maxUnlockedLevel = maxUnlockedLevel,
                                completedLevel = completedLevel,
                                coins = coins,
                                onPlayLevel = { lvl ->
                                    soundManager.playClickSound(isSoundEnabled)
                                    onPlayLevel(cracker, lvl)
                                },
                                onUnlockLevel = { lvl, cost ->
                                    if (coins >= cost) {
                                        val success = preferences.unlockNextLevel(cracker.id, cost)
                                        if (success) {
                                            soundManager.playUnlockSuccessSound(isSoundEnabled)
                                            soundManager.playCoinSound(isSoundEnabled)
                                            Toast.makeText(
                                                context,
                                                "Unlocked ${cracker.name} Level $lvl!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        soundManager.playClickSound(isSoundEnabled)
                                        val needed = cost - coins
                                        Toast.makeText(
                                            context,
                                            "Need $needed more coins! Burst earlier levels to earn more coins.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CrackerLevelsSection(
    cracker: CrackerItem,
    maxUnlockedLevel: Int,
    completedLevel: Int,
    coins: Int,
    onPlayLevel: (Int) -> Unit,
    onUnlockLevel: (Int, Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xF021123D),
                        Color(0xF0130924)
                    )
                )
            )
            .border(
                1.2.dp,
                cracker.glowColor.copy(alpha = 0.45f),
                RoundedCornerShape(18.dp)
            )
            .padding(10.dp)
    ) {
        // Section Header Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier.size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CrackerArtView(type = cracker.type)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = cracker.name,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${cracker.category} • ${cracker.badgeText}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = GoldYellow,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Current Stage Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x99000000))
                    .border(1.dp, GoldYellow.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Stage $maxUnlockedLevel/25",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            }
        }

        // Sutli Bomb Phase & Level Details Banner
        if (cracker.type == com.example.diwali.model.CrackerType.SUTLI_BOMB) {
            val currentDetail = com.example.diwali.model.SutliBombLevelConfig.getDetail(maxUnlockedLevel)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x77000000))
                    .border(1.dp, GoldYellow.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Level $maxUnlockedLevel: ${currentDetail.name}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldLight
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${currentDetail.phaseName} • ${currentDetail.description}",
                        fontSize = 9.sp,
                        lineHeight = 13.sp,
                        color = Color.White.copy(alpha = 0.90f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 25 Level Grid in 4 columns
        val totalLevels = CrackerLevelConfig.TOTAL_LEVELS_PER_CRACKER
        val columns = 4
        val rows = (totalLevels + columns - 1) / columns

        for (rowIndex in 0 until rows) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (colIndex in 0 until columns) {
                    val levelIndex = rowIndex * columns + colIndex
                    if (levelIndex < totalLevels) {
                        val level = levelIndex + 1
                        val isCompleted = level <= completedLevel
                        val isCurrent = (level <= maxUnlockedLevel && !isCompleted) || (level == maxUnlockedLevel && isCompleted)
                        val isNextUnlockable = level == maxUnlockedLevel + 1
                        val unlockCost = CrackerLevelConfig.getLevelUnlockCost(cracker.rewardCoins, level)
                        val rewardCoins = CrackerLevelConfig.getLevelRewardCoins(cracker.rewardCoins, level)

                        Box(modifier = Modifier.weight(1f)) {
                            LevelNodeCard(
                                level = level,
                                isCompleted = isCompleted,
                                isCurrent = isCurrent,
                                isNextUnlockable = isNextUnlockable,
                                unlockCost = unlockCost,
                                rewardCoins = rewardCoins,
                                canAfford = coins >= unlockCost,
                                crackerColor = cracker.primaryColor,
                                onPlayClick = { onPlayLevel(level) },
                                onUnlockClick = { onUnlockLevel(level, unlockCost) }
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun LevelNodeCard(
    level: Int,
    isCompleted: Boolean,
    isCurrent: Boolean,
    isNextUnlockable: Boolean,
    unlockCost: Int,
    rewardCoins: Int,
    canAfford: Boolean,
    crackerColor: Color,
    onPlayClick: () -> Unit,
    onUnlockClick: () -> Unit
) {
    when {
        // COMPLETED LEVEL: Green node with stars + replayable
        isCompleted -> {
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF1B5E20), Color(0xFF0D3311))
                        )
                    )
                    .border(1.2.dp, GoldYellow.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onPlayClick
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$level",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = GoldYellow
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) {
                            Canvas(modifier = Modifier.size(4.dp)) {
                                drawCircle(color = GoldYellow)
                            }
                        }
                    }
                    Text(
                        text = "PLAY",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // CURRENT ACTIVE LEVEL: Glowing vibrant orange/red with PLAY
        isCurrent -> {
            CurrentActiveLevelNode(
                level = level,
                onPlayClick = onPlayClick
            )
        }

        // NEXT LEVEL TO UNLOCK (Requires 3x burst cost from previous stage)
        isNextUnlockable -> {
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (canAfford) {
                            Brush.verticalGradient(
                                listOf(Color(0xFF283593), Color(0xFF1A237E))
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(Color(0xFF261C38), Color(0xFF140D22))
                            )
                        }
                    )
                    .border(
                        width = 1.2.dp,
                        color = if (canAfford) GoldYellow else Color(0x66FFA000),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onUnlockClick
                    )
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$level",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (canAfford) Color.White else Color.White.copy(alpha = 0.6f)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Canvas(modifier = Modifier.size(7.dp)) {
                            drawCircle(color = GoldYellow)
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "$unlockCost",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow
                        )
                    }
                    Text(
                        text = if (canAfford) "UNLOCK" else "LOCKED",
                        fontSize = 7.sp,
                        fontWeight = FontWeight.Black,
                        color = if (canAfford) GoldLight else Color(0xFFFFAB91)
                    )
                }
            }
        }

        // FUTURE LOCKED LEVEL
        else -> {
            Box(
                modifier = Modifier
                    .height(64.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x33120824))
                    .border(0.6.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "$level",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.35f)
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Canvas(modifier = Modifier.size(10.dp)) {
                        val w = size.width
                        val h = size.height
                        drawArc(
                            color = Color.White.copy(alpha = 0.35f),
                            startAngle = 180f,
                            sweepAngle = 180f,
                            useCenter = false,
                            topLeft = Offset(w * 0.25f, h * 0.12f),
                            size = Size(w * 0.50f, h * 0.45f),
                            style = Stroke(width = 1.2.dp.toPx())
                        )
                        drawRoundRect(
                            color = Color.White.copy(alpha = 0.35f),
                            topLeft = Offset(w * 0.18f, h * 0.40f),
                            size = Size(w * 0.64f, h * 0.48f),
                            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentActiveLevelNode(
    level: Int,
    onPlayClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "CurrentPulse")
    val pulseBorder by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse"
    )

    Box(
        modifier = Modifier
            .height(64.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    listOf(FestiveOrange, FestiveRed)
                )
            )
            .border(
                width = 2.dp,
                color = GoldYellow.copy(alpha = pulseBorder),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onPlayClick
            )
            .padding(3.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$level",
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = Color.White
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0x99000000))
                    .padding(horizontal = 6.dp, vertical = 1.dp)
            ) {
                Text(
                    text = "PLAY",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = GoldYellow
                )
            }
        }
    }
}

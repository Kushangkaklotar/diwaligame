package com.example.diwali.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerLevelConfig
import com.example.diwali.model.CrackerRepository
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.burst.CrackerBurstScreen
import com.example.diwali.ui.components.CrackerArtView
import com.example.diwali.ui.components.CrackerBottomRow
import com.example.diwali.ui.components.CrackerHeroCluster
import com.example.diwali.ui.components.DiwaliBackground
import com.example.diwali.ui.components.ExitConfirmDialog
import com.example.diwali.ui.components.TopBarView
import com.example.diwali.ui.levels.LevelsScreen
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.FestiveRed
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow

@Composable
fun HomeScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    onExitApp: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val coins by preferences.coinsFlow.collectAsState()
    val unlockedIds by preferences.unlockedCrackerIds.collectAsState()
    val isSoundEnabled by preferences.isSoundEnabled.collectAsState()
    val levelsRevision by preferences.levelsRevision.collectAsState()

    var activeBurstScreenCracker by remember { mutableStateOf<CrackerItem?>(null) }
    var activeBurstLevel by remember { mutableIntStateOf(1) }
    var showLevelsScreen by remember { mutableStateOf(false) }
    var showExitDialog by remember { mutableStateOf(false) }

    // Persistent LazyListState so scroll position is preserved when returning from burst screen
    val crackerListState = rememberLazyListState()
    val allCrackers = remember { CrackerRepository.crackers }
    var selectedCrackerId by remember { mutableStateOf(allCrackers.first().id) }
    val selectedCracker = remember(selectedCrackerId) {
        allCrackers.find { it.id == selectedCrackerId } ?: allCrackers.first()
    }

    val heroScale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    val infiniteTransition = rememberInfiniteTransition(label = "HomePulse")
    val buttonPulse by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.032f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ButtonPulse"
    )

    // Intercept back press when on Home Screen to prompt with Exit Dialog
    if (activeBurstScreenCracker == null && !showLevelsScreen) {
        BackHandler {
            showExitDialog = true
        }
    }

    if (showLevelsScreen) {
        LevelsScreen(
            preferences = preferences,
            soundManager = soundManager,
            initialCracker = selectedCracker,
            onBack = {
                showLevelsScreen = false
            },
            onPlayLevel = { cracker, level ->
                showLevelsScreen = false
                selectedCrackerId = cracker.id
                activeBurstLevel = level
                activeBurstScreenCracker = cracker
            }
        )
        return
    }

    AnimatedContent(
        targetState = activeBurstScreenCracker,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition"
    ) { burstCracker ->
        if (burstCracker != null) {
            // Full Interactive Burst Screen with Lighter & Realistic Physics
            CrackerBurstScreen(
                initialCracker = burstCracker,
                initialLevel = activeBurstLevel,
                preferences = preferences,
                soundManager = soundManager,
                onBackToHome = {
                    activeBurstScreenCracker = null
                }
            )
        } else {
            // Main Home Screen
            Box(modifier = modifier.fillMaxSize()) {
                // 1. Diwali Festive Animated Background (Sky, Kandil, Flickering Diyas)
                DiwaliBackground(modifier = Modifier.fillMaxSize())

                // 2. Main Content Column
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar with Coins and Sound Controls + Fairy Lights
                    TopBarView(
                        coins = coins,
                        isSoundEnabled = isSoundEnabled,
                        onSoundToggle = {
                            preferences.toggleSound()
                            soundManager.playClickSound(isSoundEnabled)
                        }
                    )

                    // 3. Hero Animated Cracker Showcase on Ornate Diwali Rangoli Altar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Interactive Hero Cluster with Squash-and-Stretch Tap Feedback
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(heroScale.value)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            soundManager.playClickSound(isSoundEnabled)
                                            coroutineScope.launch {
                                                heroScale.animateTo(1.06f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                                heroScale.animateTo(1.0f, spring(dampingRatio = Spring.DampingRatioLowBouncy))
                                            }
                                            val currentLvl = preferences.getUnlockedLevel(selectedCracker.id)
                                            activeBurstLevel = currentLvl
                                            activeBurstScreenCracker = selectedCracker
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                // Festive Rangoli Altar Glow
                                androidx.compose.foundation.Canvas(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(76.dp)
                                        .align(Alignment.BottomCenter)
                                ) {
                                    val w = size.width
                                    val h = size.height
                                    drawOval(
                                        brush = Brush.radialGradient(
                                            listOf(
                                                Color(0x66FFB300),
                                                Color(0x22FF6F00),
                                                Color.Transparent
                                            )
                                        ),
                                        topLeft = Offset(w * 0.08f, h * 0.15f),
                                        size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.85f)
                                    )
                                    drawOval(
                                        color = GoldYellow.copy(alpha = 0.35f),
                                        topLeft = Offset(w * 0.14f, h * 0.28f),
                                        size = androidx.compose.ui.geometry.Size(w * 0.72f, h * 0.65f),
                                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                                            width = 1.8.dp.toPx(),
                                            pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                                        )
                                    )
                                }

                                CrackerHeroCluster()
                            }

                            // Playful Hero Firework Banner Pill
                            val currentSelectedLvl = remember(selectedCracker.id, levelsRevision) {
                                preferences.getUnlockedLevel(selectedCracker.id)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xDD3A1C0A),
                                                Color(0xDD5C2E0E),
                                                Color(0xDD3A1C0A)
                                            )
                                        )
                                    )
                                    .border(
                                        1.5.dp,
                                        Brush.horizontalGradient(listOf(GoldYellow, GoldLight, GoldYellow)),
                                        RoundedCornerShape(16.dp)
                                    )
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = {
                                            soundManager.playClickSound(isSoundEnabled)
                                            activeBurstLevel = currentSelectedLvl
                                            activeBurstScreenCracker = selectedCracker
                                        }
                                    )
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
                                        drawCircle(color = GoldYellow)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${selectedCracker.name.uppercase()} • STAGE $currentSelectedLvl/25",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GoldLight,
                                        letterSpacing = 0.6.sp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "TAP TO BLAST ▶",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // 4. Action Buttons: Juicy 3D Arcade START BLASTING + 3D Adventure LEVELS
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        // Juicy 3D Arcade START BLASTING Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(58.dp)
                                .scale(buttonPulse)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        soundManager.playClickSound(isSoundEnabled)
                                        val campaignCracker = allCrackers.firstOrNull { cracker ->
                                            preferences.getUnlockedLevel(cracker.id) < CrackerLevelConfig.TOTAL_LEVELS_PER_CRACKER
                                        } ?: selectedCracker
                                        selectedCrackerId = campaignCracker.id
                                        activeBurstLevel = preferences.getUnlockedLevel(campaignCracker.id)
                                        activeBurstScreenCracker = campaignCracker
                                    }
                                ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Dark 3D Bottom Bevel Shadow
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(29.dp))
                                    .background(Color(0xFF8A0000))
                            )
                            // Raised Glossy Button Face
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0xFFFF9100),
                                                Color(0xFFFF3D00),
                                                Color(0xFFD50000)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 2.dp,
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                GoldLight,
                                                GoldYellow,
                                                GoldLight
                                            )
                                        ),
                                        shape = RoundedCornerShape(26.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    // Vector Flame Icon
                                    androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                                        val w = size.width
                                        val h = size.height
                                        val flame = androidx.compose.ui.graphics.Path().apply {
                                            moveTo(w * 0.5f, 0f)
                                            cubicTo(w * 0.85f, h * 0.35f, w * 0.95f, h * 0.70f, w * 0.5f, h)
                                            cubicTo(w * 0.05f, h * 0.70f, w * 0.15f, h * 0.35f, w * 0.5f, 0f)
                                            close()
                                        }
                                        drawPath(flame, color = GoldYellow)
                                        drawCircle(color = Color.White, radius = 3.dp.toPx(), center = Offset(w * 0.5f, h * 0.65f))
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "START BLASTING!",
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        letterSpacing = 1.0.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Juicy 3D Arcade LEVELS Button
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = {
                                        soundManager.playClickSound(isSoundEnabled)
                                        showLevelsScreen = true
                                    }
                                ),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Dark 3D Bottom Bevel Shadow
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(25.dp))
                                    .background(Color(0xFF220A45))
                            )
                            // Raised Face
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(45.dp)
                                    .clip(RoundedCornerShape(23.dp))
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                Color(0xFF4A148C),
                                                Color(0xFF6A1B9A),
                                                Color(0xFF4A148C)
                                            )
                                        )
                                    )
                                    .border(
                                        width = 1.8.dp,
                                        brush = Brush.horizontalGradient(
                                            listOf(GoldYellow, GoldLight, GoldYellow)
                                        ),
                                        shape = RoundedCornerShape(23.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "25 STAGES ROADMAP",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = GoldYellow,
                                        letterSpacing = 0.8.sp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0x99000000))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "VIEW ALL ▶",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 5. Bottom Horizontal Cracker Row (2 full cards + half 3rd card visible)
                    CrackerBottomRow(
                        crackers = allCrackers,
                        unlockedIds = unlockedIds,
                        userCoins = coins,
                        selectedCrackerId = selectedCrackerId,
                        listState = crackerListState,
                        onCrackerClick = { cracker ->
                            selectedCrackerId = cracker.id
                            soundManager.playClickSound(isSoundEnabled)
                            activeBurstLevel = preferences.getUnlockedLevel(cracker.id)
                            activeBurstScreenCracker = cracker
                        },
                        onUnlockClick = { cracker ->
                            selectedCrackerId = cracker.id
                            val success = preferences.unlockCracker(cracker.id)
                            if (success) {
                                soundManager.playUnlockSuccessSound(isSoundEnabled)
                            } else {
                                soundManager.playClickSound(isSoundEnabled)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )
                }
            }
        }
    }

    // Exit Confirmation Dialog
    if (showExitDialog) {
        ExitConfirmDialog(
            onDismiss = { showExitDialog = false },
            onConfirmExit = {
                showExitDialog = false
                onExitApp()
            }
        )
    }
}

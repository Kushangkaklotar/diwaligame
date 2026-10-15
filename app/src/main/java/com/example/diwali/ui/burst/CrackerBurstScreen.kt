package com.example.diwali.ui.burst

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerRepository
import com.example.diwali.model.CrackerType
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import com.example.diwali.model.CrackerLevelConfig
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CrackerBurstScreen(
    initialCracker: CrackerItem,
    initialLevel: Int = 1,
    preferences: GamePreferences,
    soundManager: SoundManager,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept hardware/system back press to return to Home screen
    BackHandler {
        onBackToHome()
    }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isSoundEnabled by preferences.isSoundEnabled.collectAsState()

    var currentCracker by remember { mutableStateOf(initialCracker) }
    var currentLevel by remember { mutableIntStateOf(initialLevel) }
    var stage by remember { mutableStateOf(BurstStage.READY) }
    var fuseProgress by remember { mutableFloatStateOf(0f) }
    var burstSessionId by remember { mutableIntStateOf(0) }
    var ignitionJob by remember { mutableStateOf<Job?>(null) }

    // Dynamic Cinematic Camera Zoom: Starts zoomed in close (1.6x), zooms out to wide angle (1.0x) when lit!
    val cameraScale = remember { Animatable(1.6f) }

    LaunchedEffect(stage) {
        when (stage) {
            BurstStage.READY, BurstStage.LIGHTING -> {
                cameraScale.animateTo(1.6f, tween(300, easing = FastOutSlowInEasing))
            }
            BurstStage.FUSE_BURNING -> {
                // Smooth cinematic pull-back as fuse burns down
                cameraScale.animateTo(1.0f, tween(1500, easing = FastOutSlowInEasing))
            }
            BurstStage.BURSTING, BurstStage.COMPLETED -> {
                cameraScale.snapTo(1.0f)
            }
        }
    }

    // Lighter offset coordinates for dragging
    val lighterX = remember { Animatable(0f) }
    val lighterY = remember { Animatable(0f) }
    var initialFlameTipInRoot by remember { mutableStateOf<Offset?>(null) }

    // Function to ignite fuse
    fun igniteFuse() {
        if (stage != BurstStage.READY) return
        stage = BurstStage.LIGHTING
        soundManager.playLighterClick(isSoundEnabled)

        // Detached lighter visual animation: touches fuse and retracts
        coroutineScope.launch {
            try {
                lighterX.animateTo(-160f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                lighterY.animateTo(80f, animationSpec = tween(280, easing = FastOutSlowInEasing))
                delay(120)
                lighterX.animateTo(0f, animationSpec = tween(380, easing = FastOutSlowInEasing))
                lighterY.animateTo(0f, animationSpec = tween(380, easing = FastOutSlowInEasing))
            } catch (_: Exception) {
                // Ignore animation interruption
            }
        }

        ignitionJob?.cancel()
        ignitionJob = coroutineScope.launch {
            // Momentary pause for lighting spark
            delay(280)

            // Start burning fuse
            stage = BurstStage.FUSE_BURNING
            soundManager.playFuseBurning(isSoundEnabled)

            // Fuse burns down over 1.5s
            val burnAnim = Animatable(0f)
            burnAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(1500, easing = LinearEasing)
            ) {
                fuseProgress = value
            }

            // BOOM! Detonation or Liftoff!
            stage = BurstStage.BURSTING
            if (currentCracker.type == CrackerType.ROCKET) {
                soundManager.playRocketLaunchSound(isSoundEnabled)
            } else {
                soundManager.playBurstSound(currentCracker.id, isSoundEnabled, currentLevel)
            }
        }
    }

    // Reset sequence for Retry or Next Cracker
    fun resetSequence(newCracker: CrackerItem = currentCracker) {
        ignitionJob?.cancel()
        ignitionJob = null
        currentCracker = newCracker
        burstSessionId++
        stage = BurstStage.READY
        fuseProgress = 0f
        initialFlameTipInRoot = null
        coroutineScope.launch {
            lighterX.snapTo(0f)
            lighterY.snapTo(0f)
            cameraScale.snapTo(1.6f)
        }
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val screenWidthPx = constraints.maxWidth.toFloat()
        val screenHeightPx = constraints.maxHeight.toFloat()
        val density = androidx.compose.ui.platform.LocalDensity.current

        val lighterWidthPx = with(density) { 54.dp.toPx() }
        val lighterHeightPx = with(density) { 100.dp.toPx() }
        val lighterMarginEndPx = with(density) { 20.dp.toPx() }

        val initialLighterLeft = screenWidthPx - lighterWidthPx - lighterMarginEndPx
        val initialLighterTop = (screenHeightPx - lighterHeightPx) / 2f
        val fuseTipThresholdPx = with(density) { 62.dp.toPx() }

        // 1. Simulation Engine (Sky, Pedestal, Cracker, Fuse & Particles) with Cinematic Camera Scale
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = cameraScale.value
                    scaleY = cameraScale.value
                    transformOrigin = TransformOrigin(0.5f, 0.76f) // Centered on cracker stage
                }
        ) {
            key(currentCracker.id, burstSessionId) {
                CrackerSimulationView(
                    cracker = currentCracker,
                    stage = stage,
                    fuseBurnProgress = fuseProgress,
                    level = currentLevel,
                    onDetonate = {
                        if (currentCracker.type == CrackerType.ROCKET || currentCracker.type == CrackerType.AERIAL_SHELL) {
                            soundManager.playBurstSound(currentCracker.id, isSoundEnabled, currentLevel)
                        }
                    },
                    onBurstComplete = {
                        val earnedReward = com.example.diwali.model.CrackerLevelConfig.getLevelRewardCoins(currentCracker.rewardCoins, currentLevel)
                        preferences.addCoins(earnedReward)
                        preferences.completeLevel(currentCracker.id, currentLevel)
                        soundManager.playWellDoneFanfare(isSoundEnabled)
                        stage = BurstStage.COMPLETED
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // 2. Top Navigation Bar (Proper statusBarsPadding, Back Button, Cracker Title, No Pause Button)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button (Crisp vector arrow, never clipped on curved screens)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x77000000))
                    .border(1.5.dp, GoldYellow.copy(alpha = 0.6f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackToHome
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.size(20.dp)) {
                    val stroke = 2.8.dp.toPx()
                    val arrowColor = Color.White
                    // Center shaft
                    drawLine(
                        color = arrowColor,
                        start = Offset(size.width * 0.85f, size.height * 0.5f),
                        end = Offset(size.width * 0.18f, size.height * 0.5f),
                        strokeWidth = stroke,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    // Upper arrowhead wing
                    drawLine(
                        color = arrowColor,
                        start = Offset(size.width * 0.52f, size.height * 0.18f),
                        end = Offset(size.width * 0.18f, size.height * 0.5f),
                        strokeWidth = stroke,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                    // Lower arrowhead wing
                    drawLine(
                        color = arrowColor,
                        start = Offset(size.width * 0.52f, size.height * 0.82f),
                        end = Offset(size.width * 0.18f, size.height * 0.5f),
                        strokeWidth = stroke,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }

            // Cracker Title Pill (English)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x88000000))
                    .border(1.5.dp, currentCracker.glowColor.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 20.dp, vertical = 7.dp)
            ) {
                Text(
                    text = "${currentCracker.name}  •  Level $currentLevel",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            }

            // Empty spacer matching back button width for balanced centering
            Spacer(modifier = Modifier.size(44.dp))
        }

        // 3. Interactive Draggable Lighter (Free movement across screen, ignites ONLY at fuse tip string)
        if (stage == BurstStage.READY || stage == BurstStage.LIGHTING) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 20.dp)
                    .offset {
                        IntOffset(
                            lighterX.value.roundToInt(),
                            lighterY.value.roundToInt()
                        )
                    }
                    .pointerInput(stage) {
                        if (stage == BurstStage.READY) {
                            detectDragGestures(
                                onDragEnd = {
                                    if (stage == BurstStage.READY) {
                                        // If released without reaching fuse tip, spring back smoothly
                                        coroutineScope.launch {
                                            lighterX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                            lighterY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                        }
                                    }
                                },
                                onDrag = { change, dragAmount ->
                                    if (stage == BurstStage.READY) {
                                        change.consume()
                                        coroutineScope.launch {
                                            // Allow completely free dragging anywhere across the screen
                                            val newX = (lighterX.value + dragAmount.x).coerceIn(-initialLighterLeft + 15f, 60f)
                                            val newY = (lighterY.value + dragAmount.y).coerceIn(-initialLighterTop + 15f, screenHeightPx - initialLighterTop - lighterHeightPx - 15f)
                                            lighterX.snapTo(newX)
                                            lighterY.snapTo(newY)

                                            // Calculate current lighter flame tip in absolute screen coordinates
                                            val flameTipX = (initialFlameTipInRoot?.x ?: (initialLighterLeft + (lighterWidthPx / 2f))) + newX
                                            val flameTipY = (initialFlameTipInRoot?.y ?: (initialLighterTop + with(density) { 72.dp.toPx() })) + newY

                                            // Calculate cracker fuse tip target in absolute coordinates
                                            val fuseTip = getFuseTipCoordinates(currentCracker.type, currentLevel, screenWidthPx, screenHeightPx)
                                            val distToFuse = kotlin.math.hypot(flameTipX - fuseTip.x, flameTipY - fuseTip.y)

                                            // IGNITION: ONLY when the flame tip touches the fuse tip string!
                                            if (distToFuse < fuseTipThresholdPx) {
                                                igniteFuse()
                                            }
                                        }
                                    }
                                }
                            )
                        }
                    }
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LighterToolView(
                        modifier = Modifier.onGloballyPositioned { coords ->
                            if (initialFlameTipInRoot == null) {
                                val rootPos = coords.positionInRoot()
                                val flameTipLocalX = (coords.size.width / 2f) - 8f
                                val flameTipLocalY = (coords.size.height * 0.58f) - 10f - 26f - 20f
                                initialFlameTipInRoot = Offset(rootPos.x + flameTipLocalX, rootPos.y + flameTipLocalY)
                            }
                        },
                        isIgniting = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xCC000000))
                            .border(1.dp, GoldYellow.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Drag to Fuse",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldYellow
                        )
                    }
                }
            }
        }

        // 4. "WELL DONE! 🏆" Celebration Dialog upon completion
        if (stage == BurstStage.COMPLETED) {
            val earnedReward = CrackerLevelConfig.getLevelRewardCoins(currentCracker.rewardCoins, currentLevel)
            val totalLevels = CrackerLevelConfig.TOTAL_LEVELS_PER_CRACKER
            val nextLevel = currentLevel + 1
            val nextUnlockCost = if (currentLevel < totalLevels) {
                CrackerLevelConfig.getLevelUnlockCost(currentCracker.rewardCoins, nextLevel)
            } else 0
            val isNextUnlocked = if (currentLevel < totalLevels) {
                preferences.isLevelUnlocked(currentCracker.id, nextLevel)
            } else true
            val currentCoins = preferences.getCoins()
            val canPlayNext = isNextUnlocked || (currentCoins >= nextUnlockCost)
            val coinsNeeded = (nextUnlockCost - currentCoins).coerceAtLeast(0)

            WellDoneDialog(
                cracker = currentCracker,
                earnedCoins = earnedReward,
                canPlayNext = canPlayNext,
                coinsNeededForNext = coinsNeeded,
                onNextClick = {
                    if (currentLevel < totalLevels) {
                        if (isNextUnlocked) {
                            currentLevel = nextLevel
                            resetSequence(currentCracker)
                        } else if (currentCoins >= nextUnlockCost) {
                            preferences.unlockNextLevel(currentCracker.id, nextUnlockCost)
                            soundManager.playUnlockSuccessSound(isSoundEnabled)
                            currentLevel = nextLevel
                            resetSequence(currentCracker)
                        } else {
                            val needed = nextUnlockCost - currentCoins
                            Toast.makeText(
                                context,
                                "Need $needed more coins to unlock Level $nextLevel! Replay to earn more coins.",
                                Toast.LENGTH_LONG
                            ).show()
                            resetSequence(currentCracker)
                        }
                    } else {
                        val all = CrackerRepository.crackers
                        val currentIndex = all.indexOfFirst { it.id == currentCracker.id }
                        val nextIndex = (currentIndex + 1) % all.size
                        val nextCracker = all[nextIndex]
                        currentLevel = preferences.getUnlockedLevel(nextCracker.id)
                        resetSequence(nextCracker)
                    }
                },
                onRetryClick = {
                    resetSequence()
                },
                onHomeClick = onBackToHome
            )
        }
    }
}

private fun getFuseTipCoordinates(type: CrackerType, level: Int, screenWidthPx: Float, screenHeightPx: Float): Offset {
    val pedestalX = screenWidthPx / 2f
    val pedestalY = screenHeightPx * 0.76f
    val cameraScale = 1.6f // Visual camera zoom scale during READY state
    val levelScale = CrackerLevelConfig.getLevelScale(level)
    val totalScale = cameraScale * levelScale
    val (dx, dy) = when (type) {
        CrackerType.SUTLI_BOMB -> {
            val detail = com.example.diwali.model.SutliBombLevelConfig.getDetail(level)
            if (detail.isSpherical) {
                val w = detail.widthDp * 1.35f
                Pair(18f, -(w + 30f))
            } else {
                val h = detail.heightDp * 1.35f
                val cone = if (detail.hasRocketCone) 26f else 0f
                Pair(20f, -(h + cone + 34f))
            }
        }
        CrackerType.ROCKET -> Pair(18f, -6f)
        CrackerType.SKY_SHOTS -> Pair(56f, -58f)
        CrackerType.PAPER_BOMB, CrackerType.LAXMI_BOMB -> Pair(16f, -94f)
        CrackerType.CHAKRI -> Pair(46f, -38f)
        CrackerType.ANAR -> Pair(16f, -116f)
        CrackerType.PHOOLJHADI -> Pair(24f, -96f)
        CrackerType.COLOR_SPARKLER -> Pair(22f, -94f)
        CrackerType.PENCIL_WHIP -> Pair(14f, -134f)
        CrackerType.LADI -> Pair(18f, -150f)
        CrackerType.SNAKE_TABLET -> Pair(16f, -28f)
        CrackerType.AERIAL_SHELL -> Pair(24f, -116f)
        CrackerType.WHIZ_WHEEL -> Pair(44f, -36f)
        CrackerType.ELECTRIC_FOUNTAIN -> Pair(18f, -130f)
    }
    return Offset(pedestalX + (dx * totalScale), pedestalY + (dy * totalScale))
}

package com.example.diwali.ui.burst

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerRepository
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun CrackerBurstScreen(
    initialCracker: CrackerItem,
    preferences: GamePreferences,
    soundManager: SoundManager,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val isSoundEnabled by preferences.isSoundEnabled.collectAsState()

    var currentCracker by remember { mutableStateOf(initialCracker) }
    var stage by remember { mutableStateOf(BurstStage.READY) }
    var fuseProgress by remember { mutableFloatStateOf(0f) }

    // Lighter offset coordinates
    val lighterX = remember { Animatable(0f) }
    val lighterY = remember { Animatable(0f) }

    // Function to ignite fuse
    fun igniteFuse() {
        if (stage != BurstStage.READY) return
        coroutineScope.launch {
            stage = BurstStage.LIGHTING
            soundManager.playLighterClick(isSoundEnabled)

            // Move lighter to touch fuse tip
            lighterX.animateTo(-140f, animationSpec = tween(350, easing = FastOutSlowInEasing))
            lighterY.animateTo(60f, animationSpec = tween(350, easing = FastOutSlowInEasing))

            // Start burning fuse
            stage = BurstStage.FUSE_BURNING
            soundManager.playFuseBurning(isSoundEnabled)

            // Retract lighter back to initial holding area
            launch {
                lighterX.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
                lighterY.animateTo(0f, animationSpec = tween(400, easing = FastOutSlowInEasing))
            }

            // Fuse burns down over 1.6s
            val burnAnim = Animatable(0f)
            burnAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(1600, easing = LinearEasing)
            ) {
                fuseProgress = value
            }

            // BOOM! Bursting starts!
            stage = BurstStage.BURSTING
            soundManager.playBurstSound(currentCracker.id, isSoundEnabled)
        }
    }

    // Reset sequence for Retry or Next Cracker
    fun resetSequence(newCracker: CrackerItem = currentCracker) {
        currentCracker = newCracker
        stage = BurstStage.READY
        fuseProgress = 0f
        coroutineScope.launch {
            lighterX.snapTo(0f)
            lighterY.snapTo(0f)
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        // 1. Simulation Engine (Sky, Pedestal, Cracker, Fuse & Particles)
        CrackerSimulationView(
            cracker = currentCracker,
            stage = stage,
            fuseBurnProgress = fuseProgress,
            onBurstComplete = {
                // Completed burst!
                preferences.addCoins(currentCracker.rewardCoins)
                soundManager.playWellDoneFanfare(isSoundEnabled)
                stage = BurstStage.COMPLETED
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Navigation Bar (Back, Cracker Name, Pause Button like video)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 38.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(1.dp, GoldYellow.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackToHome
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "←", fontSize = 22.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }

            // Cracker Title Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x88000000))
                    .border(1.dp, currentCracker.glowColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = currentCracker.nameGujarati,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldLight
                )
            }

            // Pause button (as seen in the video at 00:25)
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Color(0x66000000))
                    .border(1.dp, GoldYellow.copy(alpha = 0.5f), CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBackToHome
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "⏸", fontSize = 18.sp, color = Color.White)
            }
        }

        // 3. Interactive Lighter / Match (Right side, draggable or tappable)
        if (stage == BurstStage.READY || stage == BurstStage.LIGHTING) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 24.dp)
                    .offset {
                        IntOffset(
                            lighterX.value.roundToInt(),
                            lighterY.value.roundToInt()
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragEnd = {
                                // If dragged close to the center, ignite!
                                if (lighterX.value < -60f) {
                                    igniteFuse()
                                } else {
                                    coroutineScope.launch {
                                        lighterX.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                        lighterY.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy))
                                    }
                                }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                coroutineScope.launch {
                                    lighterX.snapTo((lighterX.value + dragAmount.x).coerceIn(-220f, 40f))
                                    lighterY.snapTo((lighterY.value + dragAmount.y).coerceIn(-100f, 150f))
                                }
                            }
                        )
                    }
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { igniteFuse() }
                    )
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    LighterToolView(
                        isIgniting = true
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xAA000000))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "ટેપ કરો / સળગાવો 🔥",
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
            WellDoneDialog(
                cracker = currentCracker,
                earnedCoins = currentCracker.rewardCoins,
                onNextClick = {
                    val all = CrackerRepository.crackers
                    val currentIndex = all.indexOfFirst { it.id == currentCracker.id }
                    val nextIndex = (currentIndex + 1) % all.size
                    resetSequence(all[nextIndex])
                },
                onRetryClick = {
                    resetSequence()
                },
                onHomeClick = onBackToHome
            )
        }
    }
}

package com.example.diwali.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerRepository
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.burst.CrackerBurstScreen
import com.example.diwali.ui.components.CrackerPager
import com.example.diwali.ui.components.DiwaliBackground
import com.example.diwali.ui.components.TopBarView

@Composable
fun HomeScreen(
    preferences: GamePreferences,
    soundManager: SoundManager,
    modifier: Modifier = Modifier
) {
    val coins by preferences.coinsFlow.collectAsState()
    val unlockedIds by preferences.unlockedCrackerIds.collectAsState()
    val isSoundEnabled by preferences.isSoundEnabled.collectAsState()

    var activeBurstScreenCracker by remember { mutableStateOf<CrackerItem?>(null) }

    AnimatedContent(
        targetState = activeBurstScreenCracker,
        transitionSpec = {
            fadeIn() togetherWith fadeOut()
        },
        label = "ScreenTransition"
    ) { burstCracker ->
        if (burstCracker != null) {
            // Full Interactive Burst Screen with Lighter & Realistic Physics (from WhatsApp Video)
            CrackerBurstScreen(
                initialCracker = burstCracker,
                preferences = preferences,
                soundManager = soundManager,
                onBackToHome = {
                    activeBurstScreenCracker = null
                }
            )
        } else {
            // Main Home Screen Carousel
            Box(modifier = modifier.fillMaxSize()) {
                // 1. Diwali Festive Animated Background (Sky, Kandil, Flickering Diyas)
                DiwaliBackground(modifier = Modifier.fillMaxSize())

                // 2. Main Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar with Initial Coins and Sound Controls
                    TopBarView(
                        coins = coins,
                        isSoundEnabled = isSoundEnabled,
                        onSoundToggle = {
                            preferences.toggleSound()
                            soundManager.playClickSound(isSoundEnabled)
                        },
                        onBonusClick = {
                            // Daily bonus / fun gift for kids
                            preferences.addCoins(20)
                            soundManager.playCoinSound(isSoundEnabled)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Cracker Pager Showcase
                    CrackerPager(
                        crackers = CrackerRepository.crackers,
                        unlockedIds = unlockedIds,
                        userCoins = coins,
                        onBurstClick = { cracker ->
                            soundManager.playClickSound(isSoundEnabled)
                            activeBurstScreenCracker = cracker
                        },
                        onUnlockClick = { cracker ->
                            val success = preferences.unlockCracker(cracker.id)
                            if (success) {
                                soundManager.playUnlockSuccessSound(isSoundEnabled)
                            } else {
                                soundManager.playClickSound(isSoundEnabled)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    )

                    Spacer(modifier = Modifier.height(36.dp))
                }
            }
        }
    }
}

package com.example.diwali

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.diwali.data.GamePreferences
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.HomeScreen
import com.example.diwali.ui.splash.SplashScreen

enum class AppDestination {
    SPLASH,
    HOME
}

class MainActivity : ComponentActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = GamePreferences(applicationContext)
        soundManager = SoundManager(applicationContext)

        setContent {
            var currentScreen by remember { mutableStateOf(AppDestination.SPLASH) }
            val isSoundEnabled by preferences.isSoundEnabled.collectAsState()

            Crossfade(
                targetState = currentScreen,
                label = "AppScreenTransition"
            ) { destination ->
                when (destination) {
                    AppDestination.SPLASH -> {
                        SplashScreen(
                            soundManager = soundManager,
                            isSoundEnabled = isSoundEnabled,
                            onSplashFinished = {
                                currentScreen = AppDestination.HOME
                            }
                        )
                    }
                    AppDestination.HOME -> {
                        HomeScreen(
                            preferences = preferences,
                            soundManager = soundManager,
                            onExitApp = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }
}
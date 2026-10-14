package com.example.diwali

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.diwali.data.GamePreferences
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.HomeScreen

class MainActivity : ComponentActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        preferences = GamePreferences(applicationContext)
        soundManager = SoundManager(applicationContext)

        setContent {
            HomeScreen(
                preferences = preferences,
                soundManager = soundManager
            )
        }
    }
}
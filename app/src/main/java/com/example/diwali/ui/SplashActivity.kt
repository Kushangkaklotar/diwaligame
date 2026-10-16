package com.example.diwali.ui

import android.content.Intent
import android.os.Bundle
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import com.example.diwali.R
import com.example.diwali.model.CrackerType
import com.example.diwali.ui.components.CrackerArtView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // STRICTLY using findViewById (No ViewBinding)
        val composeRocket = findViewById<ComposeView>(R.id.composeSplashRocket)
        val flRocketContainer = findViewById<FrameLayout>(R.id.flRocketContainer)
        val tvSplashTitle = findViewById<TextView>(R.id.tvSplashTitle)
        val tvSplashSubtitle = findViewById<TextView>(R.id.tvSplashSubtitle)

        // Render animated Rocket
        composeRocket.setContent {
            CrackerArtView(type = CrackerType.ROCKET)
        }

        // Smooth liftoff animation for rocket
        flRocketContainer.translationY = 60f
        flRocketContainer.animate()
            .translationY(-40f)
            .setDuration(1600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Alpha fade in for titles
        tvSplashTitle.alpha = 0f
        tvSplashSubtitle.alpha = 0f
        tvSplashTitle.animate().alpha(1f).setDuration(800).setStartDelay(200).start()
        tvSplashSubtitle.animate().alpha(1f).setDuration(800).setStartDelay(400).start()

        // Transition to HomeActivity
        lifecycleScope.launch {
            delay(1800)
            val intent = Intent(this@SplashActivity, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}

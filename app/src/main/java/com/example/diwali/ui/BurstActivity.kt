package com.example.diwali.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.compose.ui.platform.ComposeView
import com.example.diwali.R
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerLevelConfig
import com.example.diwali.model.CrackerRepository
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.burst.CrackerBurstScreen

class BurstActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CRACKER_ID = "extra_cracker_id"
        const val EXTRA_LEVEL = "extra_level"
    }

    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager

    private lateinit var currentCracker: CrackerItem
    private var currentLevel: Int = 1

    // STRICTLY using findViewById (No ViewBinding)
    private lateinit var btnBurstBack: ImageButton
    private lateinit var tvBurstTitle: TextView
    private lateinit var composeBurstSimulation: ComposeView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_burst)

        preferences = GamePreferences(this)
        soundManager = SoundManager(this)

        val crackerId = intent.getStringExtra(EXTRA_CRACKER_ID) ?: CrackerRepository.crackers.first().id
        currentCracker = CrackerRepository.getCrackerById(crackerId) ?: CrackerRepository.crackers.first()
        currentLevel = intent.getIntExtra(EXTRA_LEVEL, 1)

        initViews()
        setupTopBar()
        setupSimulation()
    }

    private fun initViews() {
        btnBurstBack = findViewById(R.id.btnBurstBack)
        tvBurstTitle = findViewById(R.id.tvBurstTitle)
        composeBurstSimulation = findViewById(R.id.composeBurstSimulation)
    }

    private fun setupTopBar() {
        updateTitle()
        btnBurstBack.setOnClickListener {
            finish()
        }
    }

    private fun updateTitle() {
        tvBurstTitle.text = "${currentCracker.name} • Level $currentLevel"
    }

    private fun setupSimulation() {
        composeBurstSimulation.setContent {
            CrackerBurstScreen(
                initialCracker = currentCracker,
                initialLevel = currentLevel,
                preferences = preferences,
                soundManager = soundManager,
                onBackToHome = { finish() },
                showTopBar = false,
                showInternalDialog = false,
                onBurstCompleteCallback = { reward, level, nextAction, retryAction ->
                    runOnUiThread {
                        showWellDoneDialog(reward, level, nextAction, retryAction)
                    }
                }
            )
        }
    }

    private fun showWellDoneDialog(
        earnedReward: Int,
        levelCompleted: Int,
        nextAction: (CrackerItem, Int) -> Unit,
        retryAction: () -> Unit
    ) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_well_done)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        // STRICTLY using findViewById (No ViewBinding)
        val tvReward = dialog.findViewById<TextView>(R.id.tvRewardEarned)
        val tvLockedNotice = dialog.findViewById<TextView>(R.id.tvNextLockedBanner)
        val btnNext = dialog.findViewById<AppCompatButton>(R.id.btnNextLevel)
        val btnRetry = dialog.findViewById<AppCompatButton>(R.id.btnRetry)
        val btnHome = dialog.findViewById<AppCompatButton>(R.id.btnHome)

        tvReward.text = "+$earnedReward COINS EARNED!"

        val totalLevels = CrackerLevelConfig.TOTAL_LEVELS_PER_CRACKER
        val nextLevel = levelCompleted + 1

        val hasNextLevelInCracker = levelCompleted < totalLevels
        val nextUnlockCost = if (hasNextLevelInCracker) {
            CrackerLevelConfig.getLevelUnlockCost(currentCracker.rewardCoins, nextLevel)
        } else 0

        val isNextUnlocked = if (hasNextLevelInCracker) {
            preferences.isLevelUnlocked(currentCracker.id, nextLevel)
        } else true

        val currentCoins = preferences.getCoins()
        val canPlayNext = isNextUnlocked || (currentCoins >= nextUnlockCost)
        val coinsNeeded = (nextUnlockCost - currentCoins).coerceAtLeast(0)

        // Strict coin gate: if not enough coins for next round, hide the NEXT option!
        if (!canPlayNext) {
            btnNext.visibility = View.GONE
            tvLockedNotice.visibility = View.VISIBLE
            tvLockedNotice.text = "NEXT STAGE LOCKED\nNeed $coinsNeeded more coins to unlock Level $nextLevel"
        } else {
            btnNext.visibility = View.VISIBLE
            tvLockedNotice.visibility = View.GONE

            btnNext.setOnClickListener {
                if (!isNextUnlocked && hasNextLevelInCracker) {
                    preferences.unlockNextLevel(currentCracker.id, nextUnlockCost)
                    val isSoundEnabled = preferences.isSoundEnabled.value
                    soundManager.playUnlockSuccessSound(isSoundEnabled)
                }

                if (hasNextLevelInCracker) {
                    currentLevel = nextLevel
                    updateTitle()
                    nextAction(currentCracker, nextLevel)
                } else {
                    // All 25 levels of this cracker done, loop to next cracker
                    val all = CrackerRepository.crackers
                    val curIdx = all.indexOfFirst { it.id == currentCracker.id }
                    val nextCracker = all[(curIdx + 1) % all.size]
                    currentCracker = nextCracker
                    currentLevel = preferences.getUnlockedLevel(nextCracker.id)
                    updateTitle()
                    nextAction(currentCracker, currentLevel)
                }
                dialog.dismiss()
            }
        }

        btnRetry.setOnClickListener {
            retryAction()
            dialog.dismiss()
        }

        btnHome.setOnClickListener {
            dialog.dismiss()
            finish()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

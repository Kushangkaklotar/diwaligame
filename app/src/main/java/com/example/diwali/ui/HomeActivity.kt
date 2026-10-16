package com.example.diwali.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diwali.R
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerItem
import com.example.diwali.model.CrackerRepository
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.adapter.CrackerShelfAdapter
import com.example.diwali.ui.components.CrackerHeroCluster
import com.example.diwali.ui.components.GarlandFairyLights
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager

    private val allCrackers = CrackerRepository.crackers
    private var selectedCracker: CrackerItem = allCrackers.first()

    // STRICTLY using findViewById (No ViewBinding)
    private lateinit var composeFairyLights: ComposeView
    private lateinit var composeHeroCluster: ComposeView
    private lateinit var btnSoundToggle: ImageButton
    private lateinit var tvCoinCount: TextView
    private lateinit var tvActiveStageBadge: TextView
    private lateinit var btnStartBlasting: AppCompatButton
    private lateinit var btnLevelsRoadmap: AppCompatButton
    private lateinit var rvCrackerShelf: RecyclerView
    private lateinit var shelfAdapter: CrackerShelfAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        preferences = GamePreferences(this)
        soundManager = SoundManager(this)

        initViews()
        setupFairyLightsAndHero()
        setupSoundToggle()
        setupCoinsObserver()
        setupShelfRecyclerView()
        setupActionButtons()
        setupBackPressHandler()
    }

    override fun onResume() {
        super.onResume()
        updateActiveStageBadge()
        val currentCoins = preferences.getCoins()
        tvCoinCount.text = currentCoins.toString()
        shelfAdapter.notifyDataSetChanged()
    }

    private fun initViews() {
        composeFairyLights = findViewById(R.id.composeFairyLights)
        composeHeroCluster = findViewById(R.id.composeHeroCluster)
        btnSoundToggle = findViewById(R.id.btnSoundToggle)
        tvCoinCount = findViewById(R.id.tvCoinCount)
        tvActiveStageBadge = findViewById(R.id.tvActiveStageBadge)
        btnStartBlasting = findViewById(R.id.btnStartBlasting)
        btnLevelsRoadmap = findViewById(R.id.btnLevelsRoadmap)
        rvCrackerShelf = findViewById(R.id.rvCrackerShelf)
    }

    private fun setupFairyLightsAndHero() {
        composeFairyLights.setContent {
            GarlandFairyLights()
        }

        composeHeroCluster.setContent {
            CrackerHeroCluster()
        }
    }

    private fun setupSoundToggle() {
        lifecycleScope.launch {
            preferences.isSoundEnabled.collectLatest { isEnabled ->
                btnSoundToggle.setImageResource(
                    if (isEnabled) R.drawable.ic_sound_on else R.drawable.ic_sound_off
                )
            }
        }

        btnSoundToggle.setOnClickListener {
            val current = preferences.isSoundEnabled.value
            preferences.setSoundEnabled(!current)
        }
    }

    private fun setupCoinsObserver() {
        lifecycleScope.launch {
            preferences.coinsFlow.collectLatest { coins ->
                tvCoinCount.text = coins.toString()
            }
        }
    }

    private fun setupShelfRecyclerView() {
        shelfAdapter = CrackerShelfAdapter(
            crackers = allCrackers,
            selectedCrackerId = selectedCracker.id
        ) { clickedCracker ->
            selectedCracker = clickedCracker
            shelfAdapter.updateData(allCrackers, selectedCracker.id)
            updateActiveStageBadge()
        }

        rvCrackerShelf.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        rvCrackerShelf.adapter = shelfAdapter
        updateActiveStageBadge()
    }

    private fun updateActiveStageBadge() {
        val level = preferences.getUnlockedLevel(selectedCracker.id)
        tvActiveStageBadge.text = "${selectedCracker.name.uppercase()} • STAGE $level/25  TAP TO BLAST ▶"
    }

    private fun setupActionButtons() {
        btnStartBlasting.setOnClickListener {
            val level = preferences.getUnlockedLevel(selectedCracker.id)
            val intent = Intent(this, BurstActivity::class.java).apply {
                putExtra(BurstActivity.EXTRA_CRACKER_ID, selectedCracker.id)
                putExtra(BurstActivity.EXTRA_LEVEL, level)
            }
            startActivity(intent)
        }

        tvActiveStageBadge.setOnClickListener {
            btnStartBlasting.performClick()
        }

        btnLevelsRoadmap.setOnClickListener {
            val intent = Intent(this, LevelsActivity::class.java).apply {
                putExtra(LevelsActivity.EXTRA_CRACKER_ID, selectedCracker.id)
            }
            startActivity(intent)
        }
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        })
    }

    private fun showExitConfirmationDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_exit)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true)

        // STRICTLY using findViewById (No ViewBinding)
        val btnCancel = dialog.findViewById<AppCompatButton>(R.id.btnCancelExit)
        val btnConfirm = dialog.findViewById<AppCompatButton>(R.id.btnConfirmExit)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

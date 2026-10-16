package com.example.diwali.ui

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.diwali.R
import com.example.diwali.data.GamePreferences
import com.example.diwali.model.CrackerLevelConfig
import com.example.diwali.model.CrackerRepository
import com.example.diwali.sound.SoundManager
import com.example.diwali.ui.adapter.CrackerRailAdapter
import com.example.diwali.ui.adapter.StageRoadmapAdapter
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class LevelsActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_CRACKER_ID = "extra_cracker_id"
    }

    private lateinit var preferences: GamePreferences
    private lateinit var soundManager: SoundManager

    private val allCrackers = CrackerRepository.crackers
    private var selectedIndex: Int = 0
    private var isProgrammaticScroll = false

    // STRICTLY using findViewById (No ViewBinding)
    private lateinit var btnLevelsBack: ImageButton
    private lateinit var tvLevelsCoinCount: TextView
    private lateinit var rvCrackerRail: RecyclerView
    private lateinit var rvStageRoadmap: RecyclerView

    private lateinit var railAdapter: CrackerRailAdapter
    private lateinit var stageAdapter: StageRoadmapAdapter
    private lateinit var stageLayoutManager: LinearLayoutManager
    private lateinit var railLayoutManager: LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels)

        preferences = GamePreferences(this)
        soundManager = SoundManager(this)

        val crackerId = intent.getStringExtra(EXTRA_CRACKER_ID)
        if (crackerId != null) {
            val idx = allCrackers.indexOfFirst { it.id == crackerId }
            if (idx >= 0) selectedIndex = idx
        }

        initViews()
        setupTopBar()
        setupCoinsObserver()
        setupAdapters()
        setupScrollSync()
    }

    override fun onResume() {
        super.onResume()
        tvLevelsCoinCount.text = preferences.getCoins().toString()
        railAdapter.notifyDataSetChanged()
        stageAdapter.notifyDataSetChanged()
    }

    private fun initViews() {
        btnLevelsBack = findViewById(R.id.btnLevelsBack)
        tvLevelsCoinCount = findViewById(R.id.tvLevelsCoinCount)
        rvCrackerRail = findViewById(R.id.rvCrackerRail)
        rvStageRoadmap = findViewById(R.id.rvStageRoadmap)
    }

    private fun setupTopBar() {
        // Back button returns to HomeActivity, never closes app
        btnLevelsBack.setOnClickListener {
            finish()
        }
    }

    private fun setupCoinsObserver() {
        lifecycleScope.launch {
            preferences.coinsFlow.collectLatest { coins ->
                tvLevelsCoinCount.text = coins.toString()
            }
        }
    }

    private fun setupAdapters() {
        // Left Vertical Rail
        railLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvCrackerRail.layoutManager = railLayoutManager
        railAdapter = CrackerRailAdapter(
            crackers = allCrackers,
            selectedIndex = selectedIndex,
            getLevelForCracker = { cid -> preferences.getUnlockedLevel(cid) }
        ) { clickedIndex ->
            selectedIndex = clickedIndex
            railAdapter.updateSelection(clickedIndex)
            isProgrammaticScroll = true
            rvStageRoadmap.smoothScrollToPosition(clickedIndex)
            rvCrackerRail.postDelayed({ isProgrammaticScroll = false }, 400)
        }
        rvCrackerRail.adapter = railAdapter

        // Right 25-Stage Roadmaps
        stageLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvStageRoadmap.layoutManager = stageLayoutManager
        stageAdapter = StageRoadmapAdapter(
            crackers = allCrackers,
            getLevelForCracker = { cid -> preferences.getUnlockedLevel(cid) }
        ) { cracker, chosenLevel ->
            handleLevelSelected(cracker, chosenLevel)
        }
        rvStageRoadmap.adapter = stageAdapter

        if (selectedIndex > 0) {
            rvStageRoadmap.scrollToPosition(selectedIndex)
            rvCrackerRail.scrollToPosition(selectedIndex)
        }
    }

    private fun handleLevelSelected(cracker: com.example.diwali.model.CrackerItem, chosenLevel: Int) {
        val unlockedLevel = preferences.getUnlockedLevel(cracker.id)
        val currentCoins = preferences.getCoins()

        when {
            // Already unlocked stage (current or completed)
            chosenLevel <= unlockedLevel -> {
                launchBurstActivity(cracker.id, chosenLevel)
            }

            // Immediately next unlockable stage
            chosenLevel == unlockedLevel + 1 -> {
                val cost = CrackerLevelConfig.getLevelUnlockCost(cracker.rewardCoins, chosenLevel)
                if (currentCoins >= cost) {
                    preferences.unlockNextLevel(cracker.id, cost)
                    val isSoundEnabled = preferences.isSoundEnabled.value
                    soundManager.playUnlockSuccessSound(isSoundEnabled)
                    Toast.makeText(this, "Unlocked Level $chosenLevel! Ready to blast!", Toast.LENGTH_SHORT).show()

                    val idx = allCrackers.indexOfFirst { it.id == cracker.id }
                    if (idx >= 0) {
                        railAdapter.notifyItemChanged(idx)
                        stageAdapter.notifyItemChanged(idx)
                    }
                    launchBurstActivity(cracker.id, chosenLevel)
                } else {
                    val needed = cost - currentCoins
                    Toast.makeText(
                        this,
                        "Need $needed more coins to unlock Level $chosenLevel! Play earlier stages to earn coins.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            // Locked distant stage
            else -> {
                Toast.makeText(
                    this,
                    "Level $chosenLevel is locked! Complete Level ${chosenLevel - 1} first.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun launchBurstActivity(crackerId: String, level: Int) {
        val intent = Intent(this, BurstActivity::class.java).apply {
            putExtra(BurstActivity.EXTRA_CRACKER_ID, crackerId)
            putExtra(BurstActivity.EXTRA_LEVEL, level)
        }
        startActivity(intent)
    }

    private fun setupScrollSync() {
        rvStageRoadmap.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                if (isProgrammaticScroll) return

                val firstPos = stageLayoutManager.findFirstVisibleItemPosition()
                if (firstPos in allCrackers.indices && firstPos != selectedIndex) {
                    selectedIndex = firstPos
                    railAdapter.updateSelection(firstPos)
                    rvCrackerRail.smoothScrollToPosition(firstPos)
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

package com.example.diwali.data

import android.content.Context
import android.content.SharedPreferences
import com.example.diwali.model.CrackerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GamePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("diwali_game_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_COINS = "key_coins"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_UNLOCKED_CRACKERS = "key_unlocked_crackers"
        private const val KEY_SOUND_ENABLED = "key_sound_enabled"
        private const val KEY_PREFIX_LEVEL = "key_cracker_level_"
        private const val KEY_CAMPAIGN_INDEX = "key_campaign_index"
        private const val INITIAL_COINS = 100
    }

    private val _coinsFlow = MutableStateFlow(getInitialCoins())
    val coinsFlow: StateFlow<Int> = _coinsFlow.asStateFlow()

    private val _unlockedCrackerIds = MutableStateFlow(loadUnlockedIds())
    val unlockedCrackerIds: StateFlow<Set<String>> = _unlockedCrackerIds.asStateFlow()

    private val _levelsRevision = MutableStateFlow(0)
    val levelsRevision: StateFlow<Int> = _levelsRevision.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(prefs.getBoolean(KEY_SOUND_ENABLED, true))
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private fun getInitialCoins(): Int {
        val isFirstLaunch = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        return if (isFirstLaunch) {
            prefs.edit()
                .putBoolean(KEY_FIRST_LAUNCH, false)
                .putInt(KEY_COINS, INITIAL_COINS)
                .apply()
            INITIAL_COINS
        } else {
            prefs.getInt(KEY_COINS, INITIAL_COINS)
        }
    }

    private fun loadUnlockedIds(): Set<String> {
        val defaultUnlocked = CrackerRepository.crackers
            .filter { it.isUnlockedByDefault }
            .map { it.id }
            .toSet()

        val savedSet = prefs.getStringSet(KEY_UNLOCKED_CRACKERS, null)
        return if (savedSet == null) {
            prefs.edit().putStringSet(KEY_UNLOCKED_CRACKERS, defaultUnlocked).apply()
            defaultUnlocked
        } else {
            val combined = (savedSet + defaultUnlocked).toSet()
            if (combined.size != savedSet.size) {
                prefs.edit().putStringSet(KEY_UNLOCKED_CRACKERS, combined).apply()
            }
            combined
        }
    }

    fun addCoins(amount: Int) {
        val newAmount = _coinsFlow.value + amount
        _coinsFlow.value = newAmount
        prefs.edit().putInt(KEY_COINS, newAmount).apply()
    }

    fun spendCoins(amount: Int): Boolean {
        if (_coinsFlow.value >= amount) {
            val newAmount = _coinsFlow.value - amount
            _coinsFlow.value = newAmount
            prefs.edit().putInt(KEY_COINS, newAmount).apply()
            return true
        }
        return false
    }

    fun unlockCracker(crackerId: String): Boolean {
        val cracker = CrackerRepository.crackers.find { it.id == crackerId } ?: return false
        if (spendCoins(cracker.price)) {
            val updated = _unlockedCrackerIds.value.toMutableSet().apply { add(crackerId) }
            _unlockedCrackerIds.value = updated
            prefs.edit().putStringSet(KEY_UNLOCKED_CRACKERS, updated).apply()
            return true
        }
        return false
    }

    fun isCrackerUnlocked(crackerId: String): Boolean {
        return _unlockedCrackerIds.value.contains(crackerId)
    }

    fun getCoins(): Int = _coinsFlow.value

    fun getUnlockedLevel(crackerId: String): Int {
        return prefs.getInt(KEY_PREFIX_LEVEL + crackerId, 1).coerceIn(1, 25)
    }

    fun getCompletedLevel(crackerId: String): Int {
        return prefs.getInt("key_cracker_completed_" + crackerId, 0).coerceIn(0, 25)
    }

    fun unlockNextLevel(crackerId: String, cost: Int): Boolean {
        val current = getUnlockedLevel(crackerId)
        if (current >= 25) return false
        if (spendCoins(cost)) {
            val next = current + 1
            prefs.edit().putInt(KEY_PREFIX_LEVEL + crackerId, next).apply()
            _levelsRevision.value = _levelsRevision.value + 1
            return true
        }
        return false
    }

    fun isLevelUnlocked(crackerId: String, level: Int): Boolean {
        if (level <= 1) return true
        return level <= getUnlockedLevel(crackerId)
    }

    fun completeLevel(crackerId: String, level: Int) {
        val current = getCompletedLevel(crackerId)
        if (level > current) {
            prefs.edit().putInt("key_cracker_completed_" + crackerId, level.coerceIn(1, 25)).apply()
            _levelsRevision.value = _levelsRevision.value + 1
        }
    }

    fun getCampaignProgressIndex(): Int {
        return prefs.getInt(KEY_CAMPAIGN_INDEX, 0)
    }

    fun advanceCampaignProgress() {
        val next = getCampaignProgressIndex() + 1
        prefs.edit().putInt(KEY_CAMPAIGN_INDEX, next).apply()
    }

    fun toggleSound(): Boolean {
        val newValue = !_isSoundEnabled.value
        _isSoundEnabled.value = newValue
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, newValue).apply()
        return newValue
    }
}

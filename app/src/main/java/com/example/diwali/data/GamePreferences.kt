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
        private const val INITIAL_COINS = 100
    }

    private val _coinsFlow = MutableStateFlow(getInitialCoins())
    val coinsFlow: StateFlow<Int> = _coinsFlow.asStateFlow()

    private val _unlockedCrackerIds = MutableStateFlow(loadUnlockedIds())
    val unlockedCrackerIds: StateFlow<Set<String>> = _unlockedCrackerIds.asStateFlow()

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
            savedSet
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

    fun toggleSound(): Boolean {
        val newValue = !_isSoundEnabled.value
        _isSoundEnabled.value = newValue
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, newValue).apply()
        return newValue
    }
}

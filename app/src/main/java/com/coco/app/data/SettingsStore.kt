package com.coco.app.data

import android.content.Context
import com.coco.app.domain.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Preferencias de la app sobre SharedPreferences. La lectura inicial es **síncrona**, así
 * `HomeScreen` conoce el ancla de arranque desde la primera composición (sin "flash").
 */
class SettingsStore(context: Context) : SettingsRepository {

    private val prefs = context.getSharedPreferences("coco_settings", Context.MODE_PRIVATE)

    private val _startInHistory = MutableStateFlow(prefs.getBoolean(KEY_START_IN_HISTORY, false))
    private val _fastMode = MutableStateFlow(prefs.getBoolean(KEY_FAST_MODE, false))
    private val _hasSeenOnboarding = MutableStateFlow(prefs.getBoolean(KEY_HAS_SEEN_ONBOARDING, false))
    private val _enterToSubmit = MutableStateFlow(prefs.getBoolean(KEY_ENTER_TO_SUBMIT, true))

    override val startInHistory: StateFlow<Boolean> = _startInHistory.asStateFlow()
    override val fastMode: StateFlow<Boolean> = _fastMode.asStateFlow()
    override val hasSeenOnboarding: StateFlow<Boolean> = _hasSeenOnboarding.asStateFlow()
    override val enterToSubmit: StateFlow<Boolean> = _enterToSubmit.asStateFlow()

    override fun setStartInHistory(value: Boolean) {
        prefs.edit().putBoolean(KEY_START_IN_HISTORY, value).apply()
        _startInHistory.value = value
    }

    override fun setFastMode(value: Boolean) {
        prefs.edit().putBoolean(KEY_FAST_MODE, value).apply()
        _fastMode.value = value
    }

    override fun completeOnboarding() {
        prefs.edit().putBoolean(KEY_HAS_SEEN_ONBOARDING, true).apply()
        _hasSeenOnboarding.value = true
    }

    override fun setEnterToSubmit(value: Boolean) {
        prefs.edit().putBoolean(KEY_ENTER_TO_SUBMIT, value).apply()
        _enterToSubmit.value = value
    }

    private companion object {
        const val KEY_START_IN_HISTORY = "start_in_history"
        const val KEY_FAST_MODE = "fast_mode"
        const val KEY_HAS_SEEN_ONBOARDING = "has_seen_onboarding"
        const val KEY_ENTER_TO_SUBMIT = "enter_to_submit"
    }
}

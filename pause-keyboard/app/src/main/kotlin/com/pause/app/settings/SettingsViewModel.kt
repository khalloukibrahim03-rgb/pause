package com.pause.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pause.keyboard.settings.SettingsManager
import com.pause.shared.KeyboardSettings
import com.pause.shared.Language
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings screen.
 * Reads/writes [KeyboardSettings] via the keyboard module's [SettingsManager].
 */
data class SettingsUiState(
    val settings: KeyboardSettings? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _settingsState = MutableStateFlow(SettingsUiState())
    val settingsState: StateFlow<SettingsUiState> = _settingsState

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            try {
                settingsManager.settingsFlow.collect { settings ->
                    _settingsState.value = SettingsUiState(
                        settings = settings,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _settingsState.value = SettingsUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun updateLanguage(language: Language) {
        viewModelScope.launch {
            settingsManager.updateActiveLanguage(language)
        }
    }

    fun updateHeightRatio(ratio: Float) {
        viewModelScope.launch {
            settingsManager.updateHeightRatio(ratio)
        }
    }

    fun updateHaptic(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateHapticEnabled(enabled)
        }
    }

    fun updateSound(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateSoundEnabled(enabled)
        }
    }

    fun updateVibration(intensity: Int) {
        viewModelScope.launch {
            settingsManager.updateVibrationIntensity(intensity)
        }
    }

    fun updateTheme(isDark: Boolean) {
        viewModelScope.launch {
            settingsManager.updateDarkTheme(isDark)
        }
    }

    fun updateInterventions(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateInterventionsEnabled(enabled)
        }
    }
}

package com.pause.keyboard.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.pause.shared.KeyboardSettings
import com.pause.shared.Language
import com.pause.shared.ShiftState
import com.pause.shared.KeyboardMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.keyboardDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pause_keyboard_prefs",
    produceFile = { it.preferencesDataStoreFile("pause_keyboard_prefs") }
)

@Singleton
class SettingsManager @Inject constructor(
    @ApplicationContext context: Context
) {

    private val dataStore = context.keyboardDataStore
    private val _current = MutableStateFlow(KeyboardSettings())
    val settingsFlow: Flow<KeyboardSettings> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { prefs ->
            val settings = parseSettings(prefs)
            _current.value = settings
            settings
        }

    fun currentSettings(): KeyboardSettings = _current.value

    suspend fun updateHeightRatio(ratio: Float) { dataStore.edit { it[PrefKeys.HEIGHT_RATIO] = ratio } }
    suspend fun updateHapticEnabled(enabled: Boolean) { dataStore.edit { it[PrefKeys.HAPTIC_ENABLED] = enabled.toString() } }
    suspend fun updateSoundEnabled(enabled: Boolean) { dataStore.edit { it[PrefKeys.SOUND_ENABLED] = enabled.toString() } }
    suspend fun updateVibrationIntensity(intensity: Int) { dataStore.edit { it[PrefKeys.VIBRATION_INTENSITY] = intensity } }
    suspend fun updateDarkTheme(isDark: Boolean) { dataStore.edit { it[PrefKeys.IS_DARK_THEME] = isDark.toString() } }
    suspend fun updateInterventionsEnabled(enabled: Boolean) { dataStore.edit { it[PrefKeys.INTERVENTIONS_ENABLED] = enabled.toString() } }
    suspend fun updateActiveLanguage(language: Language) { dataStore.edit { it[PrefKeys.ACTIVE_LANGUAGE] = language.code } }
    suspend fun updateInterventionCooldown(cooldownMs: Long) { dataStore.edit { it[PrefKeys.INTERVENTION_COOLDOWN_MS] = cooldownMs.toString() } }

    private fun parseSettings(prefs: Preferences): KeyboardSettings {
        val langCode = prefs[PrefKeys.ACTIVE_LANGUAGE] ?: Language.ENGLISH.code
        return KeyboardSettings(
            activeLanguage = Language.fromCode(langCode),
            heightRatio = prefs[PrefKeys.HEIGHT_RATIO] ?: 0.45f,
            hapticEnabled = prefs[PrefKeys.HAPTIC_ENABLED]?.toBoolean() ?: true,
            soundEnabled = prefs[PrefKeys.SOUND_ENABLED]?.toBoolean() ?: false,
            vibrationIntensity = prefs[PrefKeys.VIBRATION_INTENSITY] ?: 40,
            isDarkTheme = prefs[PrefKeys.IS_DARK_THEME]?.toBoolean() ?: false,
            interventionsEnabled = prefs[PrefKeys.INTERVENTIONS_ENABLED]?.toBoolean() ?: true,
            backspaceRepeatDelayMs = prefs[PrefKeys.BACKSPACE_DELAY_MS] ?: 500,
            backspaceRepeatIntervalMs = prefs[PrefKeys.BACKSPACE_INTERVAL_MS] ?: 50,
            interventionCooldownMs = (prefs[PrefKeys.INTERVENTION_COOLDOWN_MS]?.toLongOrNull() ?: 60_000L),
            maxInterventionsPerSession = prefs[PrefKeys.MAX_INTERVENTIONS] ?: 3
        )
    }

    private object PrefKeys {
        val ACTIVE_LANGUAGE = stringPreferencesKey("language")
        val HEIGHT_RATIO = floatPreferencesKey("height_ratio")
        val HAPTIC_ENABLED = stringPreferencesKey("haptic_enabled")
        val SOUND_ENABLED = stringPreferencesKey("sound_enabled")
        val VIBRATION_INTENSITY = intPreferencesKey("vibration_intensity")
        val IS_DARK_THEME = stringPreferencesKey("is_dark_theme")
        val INTERVENTIONS_ENABLED = stringPreferencesKey("interventions_enabled")
        val BACKSPACE_DELAY_MS = intPreferencesKey("backspace_delay_ms")
        val BACKSPACE_INTERVAL_MS = intPreferencesKey("backspace_interval_ms")
        val INTERVENTION_COOLDOWN_MS = stringPreferencesKey("intervention_cooldown_ms")
        val MAX_INTERVENTIONS = intPreferencesKey("max_interventions")
    }
}

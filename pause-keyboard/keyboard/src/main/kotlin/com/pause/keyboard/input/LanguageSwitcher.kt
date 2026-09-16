package com.pause.keyboard.input

import android.content.Intent
import android.provider.Settings
import com.pause.shared.Language
import com.pause.shared.KeyboardSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Cycles between available input languages and updates the shared
 * SettingsManager accordingly.
 */
@Singleton
class LanguageSwitcher @Inject constructor(
    private val settingsManager: com.pause.keyboard.settings.SettingsManager
) {

    private val _current = MutableStateFlow(Language.ENGLISH)
    val current: Flow<Language> = _current.asStateFlow()

    suspend fun switchToNext() {
        val next = Language.next(_current.value)
        _current.value = next
        settingsManager.updateActiveLanguage(next)
    }

    suspend fun switchTo(language: Language) {
        _current.value = language
        settingsManager.updateActiveLanguage(language)
    }

    /** Opens the system input method settings screen. */
    fun openSystemSettings(context: android.content.Context) {
        context.startActivity(
            Intent(Settings.ACTION_INPUT_METHOD_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        )
    }
}

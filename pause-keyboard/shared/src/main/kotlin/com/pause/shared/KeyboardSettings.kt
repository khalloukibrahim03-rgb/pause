package com.pause.shared

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Immutable snapshot of all keyboard user-facing settings.
 *
 * This travels across module boundaries (shared → keyboard, shared → app)
 * via DataStore and Parcelable.
 *
 * **No raw text or message content is ever part of these settings.**
 */
@Parcelize
data class KeyboardSettings(
    val activeLanguage: Language = Language.ENGLISH,
    val shiftState: ShiftState = ShiftState.Lower,
    val mode: KeyboardMode = KeyboardMode.ALPHA,
    val heightRatio: Float = 0.45f,           // fraction of screen height
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val vibrationIntensity: Int = 40,          // 0-255
    val isDarkTheme: Boolean = false,
    val interventionsEnabled: Boolean = true,
    val showKeyLabels: Boolean = true,
    val backspaceRepeatDelayMs: Int = 500,
    val backspaceRepeatIntervalMs: Int = 50,
    val interventionCooldownMs: Long = 60_000L,
    val maxInterventionsPerSession: Int = 3
) : Parcelable

/**
 * Range constants for settings validation.
 */
object SettingsRanges {
    const val MIN_HEIGHT_RATIO = 0.25f
    const val MAX_HEIGHT_RATIO = 0.65f
    const val MIN_VIBRATION_INTENSITY = 0
    const val MAX_VIBRATION_INTENSITY = 255
    const val MIN_BACKSPACE_DELAY_MS = 100
    const val MAX_BACKSPACE_DELAY_MS = 1000
    const val MIN_BACKSPACE_INTERVAL_MS = 10
    const val MAX_BACKSPACE_INTERVAL_MS = 200
    const val MIN_INTERVENTION_COOLDOWN_MS = 10_000L
    const val MAX_INTERVENTION_COOLDOWN_MS = 300_000L
}

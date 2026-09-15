package com.pause.shared

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Classification of keys on the keyboard.
 */
enum class KeyType {
    LETTER,            // Alphabetic characters
    FUNCTION,          // Special function keys (e.g., emoji)
    SYSTEM,            // System keys (e.g., IME switcher)
    MODIFIER,          // Shift / Caps Lock
    SHIFT,             // Shift key (handled specially)
    BACKSPACE,         // Delete key
    ENTER,             // Return key
    SPACE,             // Spacebar
    LANGUAGE,          // Language switch key
    SYMBOL_TOGGLE,     // Toggle to symbols/numbers
    ARROW_LEFT,        // Cursor left
    ARROW_RIGHT,       // Cursor right
    ARROW_UP,          // Cursor up
    ARROW_DOWN,        // Cursor down
    DOT,               // Period key
    COMMA,             // Comma key
    UNKNOWN            // Fallback
}

/**
 * Data-driven key specification — fully detached from rendering.
 * All keyboard layouts are produced as a list of rows of KeySpecs.
 *
 * @param code          KeyEvent-compatible code (use negative custom codes from [KeyCodes])
 * @param label         Text shown on the key (also used for the pressed character on output)
 * @param type          [KeyType] for rendering / behavior dispatch
 * @param widthRatio    Fraction of the standard key width (1.0 = standard, 1.5 = wide, 0.5 = half)
 * @param longPressCodes  Key codes that appear in the long-press popup
 * @param longPressLabels  Labels that correspond to [longPressCodes]
 * @param requiresShift  If true, the letter is uppercase when shift is active
 * @param capsLockAffect If true, this key's output changes when caps lock is on
 * @param rtlAdjust       Additional width offset for RTL layouts (normalized to key width)
 */
@Parcelize
data class KeySpec(
    val code: Int,
    val label: String,
    val type: KeyType,
    val widthRatio: Float = 1f,
    val longPressCodes: List<Int> = emptyList(),
    val longPressLabels: List<String> = emptyList(),
    val requiresShift: Boolean = false,
    val capsLockAffect: Boolean = false,
    val rtlAdjust: Float = 0f
) : Parcelable

package com.pause.keyboard

import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import com.pause.shared.InterventionProposal
import com.pause.shared.IntelligenceObserver
import com.pause.shared.KeyboardMode
import com.pause.shared.KeyboardSettings
import com.pause.shared.KeyCodes
import com.pause.shared.Language
import com.pause.shared.ShiftState
import com.pause.shared.KeyboardDefinition
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central state holder for the keyboard.
 *
 * Manages: language, shift state, keyboard mode, InputConnection reference,
 * and communicates with the Local Intelligence Engine.
 *
 * Thread-safety: All mutable state is backed by StateFlow or is
 * mutated only from the IME's main thread.
 */
@Singleton
class KeyboardState @Inject constructor() {

    // ── Public observable state ───────────────────────────────────────

    val settings: StateFlow<KeyboardSettings> get() = _settings
    val language: StateFlow<Language> get() = _language
    val shiftState: StateFlow<ShiftState> get() = _shiftState
    val mode: StateFlow<KeyboardMode> get() = _mode
    val keyboardDefinition: StateFlow<KeyboardDefinition?> get() = _keyboardDefinition
    val editorInfo: StateFlow<EditorInfo?> get() = _editorInfo

    /** Tracks long-press popup state per key code. */
    val activePopupKeys = mutableMapOf<Int, String>()

    /** True when the language picker popup is open. */
    val languagePickerOpen = MutableStateFlow(false)

    // ── Private mutable state ───────────────────────────────────────────

    private val _settings = MutableStateFlow(KeyboardSettings())
    private val _language = MutableStateFlow(Language.ENGLISH)
    private val _shiftState = MutableStateFlow(ShiftState.Lower)
    private val _mode = MutableStateFlow(KeyboardMode.ALPHA)
    private val _keyboardDefinition = MutableStateFlow<KeyboardDefinition?>(null)
    private val _editorInfo = MutableStateFlow<EditorInfo?>(null)

    private var inputConnection: InputConnection? = null
    var intelligenceObserver: IntelligenceObserver? = null

    // ── Public API ──────────────────────────────────────────────────────

    fun updateSettings(newSettings: KeyboardSettings) {
        _settings.value = newSettings
        _language.value = newSettings.activeLanguage
    }

    fun updateInputConnection(ic: InputConnection?) {
        inputConnection = ic
    }

    fun setEditorInfo(info: EditorInfo?) {
        _editorInfo.value = info
    }

    /**
     * Handles a key press. Dispatches to InputConnection and notifies
     * the intelligence engine.
     */
    fun pressKey(code: Int, label: String, isLongPress: Boolean) {
        val ic = inputConnection ?: return

        val isDeletion = code == KeyCodes.BACKSPACE
        val isModifier = code == KeyCodes.SHIFT || code == KeyCodes.CAPS_LOCK
        val now = System.currentTimeMillis()

        when (code) {
            KeyCodes.SHIFT -> {
                val newState = ShiftState.next(_shiftState.value)
                _shiftState.value = newState
            }
            KeyCodes.CAPS_LOCK -> {
                val newState = ShiftState.toggleCaps(_shiftState.value)
                _shiftState.value = newState
            }
            KeyCodes.BACKSPACE -> {
                ic.deleteSurroundingText(1, 0)
            }
            KeyCodes.ENTER -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            KeyCodes.SPACE -> {
                ic.commitText(" ", 1)
                if (_shiftState.value is ShiftState.Upper) {
                    _shiftState.value = ShiftState.Lower
                }
            }
            KeyCodes.LANGUAGE_SWITCH -> {
                cycleLanguage()
            }
            KeyCodes.MODE_TOGGLE -> {
                switchToSymbols()
            }
            KeyCodes.DOT -> {
                ic.commitText(".", 1)
                resetShiftIfUpper()
            }
            KeyCodes.COMMA -> {
                ic.commitText(",", 1)
                resetShiftIfUpper()
            }
            KeyCodes.ARROW_LEFT -> {
                val (start, _) = getCurrentCursorPosition()
                if (start > 0) ic.setSelection(start - 1, start - 1)
            }
            KeyCodes.ARROW_RIGHT -> {
                val (_, end) = getCurrentCursorPosition()
                ic.setSelection(end + 1, end + 1)
            }
            KeyCodes.EMOJI -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_EMOJI))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_EMOJI))
            }
            else -> {
                val shiftedLabel = if (ShiftState.isUpperCase(_shiftState.value)) {
                    label.uppercase()
                } else {
                    label.lowercase()
                }
                ic.commitText(shiftedLabel, 1)
                resetShiftIfUpper()
            }
        }

        // Notify intelligence engine (NO text labels for deletion/modifier keys)
        intelligenceObserver?.onKeyEvent(
            code = code,
            label = if (isDeletion || isModifier) "" else label,
            isDeletion = isDeletion,
            isModifier = isModifier,
            isLongPress = isLongPress,
            timestamp = now
        )
    }

    fun handleHardwareKey(keyCode: Int) {
        val ic = inputConnection ?: return
        when (keyCode) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                _shiftState.value = ShiftState.next(_shiftState.value)
            }
            KeyEvent.KEYCODE_DEL -> {
                ic.deleteSurroundingText(1, 0)
            }
            KeyEvent.KEYCODE_ENTER -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            KeyEvent.KEYCODE_SPACE -> {
                ic.commitText(" ", 1)
                if (_shiftState.value is ShiftState.Upper) {
                    _shiftState.value = ShiftState.Lower
                }
            }
            else -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, keyCode))
            }
        }
    }

    fun cycleLanguage() {
        val next = Language.next(_language.value)
        _language.value = next
        _shiftState.value = ShiftState.Lower
        intelligenceObserver?.onLanguageChanged(next)
    }

    fun switchToSymbols() {
        _mode.value = if (_mode.value == KeyboardMode.ALPHA) {
            KeyboardMode.SYMBOLS
        } else {
            KeyboardMode.ALPHA
        }
        intelligenceObserver?.onModeChanged(_mode.value)
    }

    fun switchToNumbers() {
        _mode.value = KeyboardMode.NUMBERS
        intelligenceObserver?.onModeChanged(_mode.value)
    }

    fun resetShift() {
        _shiftState.value = ShiftState.Lower
    }

    private fun resetShiftIfUpper() {
        if (_shiftState.value is ShiftState.Upper) {
            _shiftState.value = ShiftState.Lower
        }
    }

    private fun getCurrentCursorPosition(): Pair<Int, Int) {
        val ic = inputConnection ?: return 0 to 0
        val before = ic.getTextBeforeCursor(500, 0)
        val after = ic.getTextAfterCursor(500, 0)
        val beforeLen = before?.length ?: 0
        val afterLen = after?.length ?: 0
        return beforeLen to (beforeLen + afterLen)
    }

    fun setKeyboardDefinition(definition: KeyboardDefinition) {
        _keyboardDefinition.value = definition
    }

    fun dismissPopups() {
        activePopupKeys.clear()
        languagePickerOpen.value = false
    }

    fun onSessionStart(ic: InputConnection?) {
        inputConnection = ic
        _shiftState.value = ShiftState.Lower
    }

    fun clear() {
        inputConnection = null
        activePopupKeys.clear()
    }

    fun getEditorTextLength(): Int {
        val ic = inputConnection ?: return 0
        val before = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)
        val after = ic.getTextAfterCursor(Int.MAX_VALUE, 0)
        val total = (before?.length ?: 0) + (after?.length ?: 0)
        // Clear references — we never store the text
        return total
    }
}

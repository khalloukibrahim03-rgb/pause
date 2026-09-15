package com.pause.keyboard

import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.compose.runtime.mutableStateMapOf
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
 * This is NOT a ViewModel (the IME has no Activity/ViewActivity scope),
 * but it follows the same unidirectional-data-flow principle:
 * Compose UI reads from StateFlows and calls intent methods.
 *
 * Thread-safety: All mutable state is backed by StateFlow or is
 * mutated only from the IME's main thread.
 */
@Singleton
class KeyboardState @Inject constructor() {

    // ── Public observable state ───────────────────────────────────────

    val settings: StateFlow<KeyboardSettings> = MutableStateFlow(KeyboardSettings())

    val language: StateFlow<Language> = MutableStateFlow(Language.ENGLISH)

    val shiftState: StateFlow<ShiftState> = MutableStateFlow(ShiftState.Lower)

    val mode: StateFlow<KeyboardMode> = MutableStateFlow(KeyboardMode.ALPHA)

    /** Current keyboard definition (derived from language + mode). */
    val keyboardDefinition: StateFlow<KeyboardDefinition?> = MutableStateFlow(null)

    /** Tracks long-press popup state per key code. Key code → label of alt char. */
    val activePopupKeys = mutableStateMapOf<Int, String>()

    /** True when the language picker popup is open. */
    val languagePickerOpen = MutableStateFlow(false)

    /** Editor info for enter-key label decisions. */
    val editorInfo: StateFlow<EditorInfo?> = MutableStateFlow(null)

    // ── Private mutable state ───────────────────────────────────────────

    private var inputConnection: InputConnection? = null

    private val settingsMutable = settings as MutableStateFlow<KeyboardSettings>
    private val languageMutable = language as MutableStateFlow<Language>
    private val shiftMutable = shiftState as MutableStateFlow<ShiftState>
    private val modeMutable = mode as MutableStateFlow<KeyboardMode>
    private val definitionMutable = keyboardDefinition as MutableStateFlow<KeyboardDefinition?>
    private val editorInfoMutable = editorInfo as MutableStateFlow<EditorInfo?>

    // ── Intelligence ────────────────────────────────────────────────────

    var intelligenceObserver: IntelligenceObserver? = null
        set(value) {
            field = value
            // Forward observer reference so keys can report events
        }

    // ── Public API ──────────────────────────────────────────────────────

    fun updateSettings(newSettings: KeyboardSettings) {
        settingsMutable.value = newSettings
        languageMutable.value = newSettings.activeLanguage
    }

    fun updateInputConnection(ic: InputConnection?) {
        inputConnection = ic
    }

    fun setEditorInfo(info: EditorInfo?) {
        editorInfoMutable.value = info
    }

    /**
     * Called when a key is pressed. Dispatches to InputConnection
     * and notifies the intelligence engine.
     *
     * @param code Key code (see [KeyCodes] or Unicode code points for letters)
     * @param label The text to commit (for letters) — NOT stored by intelligence
     * @param isLongPress Whether this originated from a long-press
     */
    fun pressKey(code: Int, label: String, isLongPress: Boolean = false) {
        val ic = inputConnection ?: return

        val isDeletion = code == KeyCodes.BACKSPACE
        val isModifier = code == KeyCodes.SHIFT || code == KeyCodes.CAPS_LOCK

        val now = System.currentTimeMillis()

        when (code) {
            KeyCodes.SHIFT -> {
                val newState = ShiftState.next(shiftMutable.value)
                shiftMutable.value = newState
                // Don't commit text for shift
            }
            KeyCodes.CAPS_LOCK -> {
                val newState = ShiftState.toggleCaps(shiftMutable.value)
                shiftMutable.value = newState
            }
            KeyCodes.BACKSPACE -> {
                handleBackspace(ic, isLongPress = isLongPress)
            }
            KeyCodes.ENTER -> {
                val returnLabel = computeReturnLabel()
                if (returnLabel == EditorInfo.IME_ACTION_DONE) {
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                    ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
                } else {
                    ic.commitText("\n", 1)
                }
            }
            KeyCodes.SPACE -> {
                ic.commitText(" ", 1)
                // Auto-reset shift from Upper → Lower after space (not CapsLock)
                if (shiftMutable.value is ShiftState.Upper) {
                    shiftMutable.value = ShiftState.Lower
                }
            }
            KeyCodes.LANGUAGE_SWITCH -> {
                cycleLanguage()
            }
            KeyCodes.MODE_TOGGLE -> {
                switchToSymbols()
            }
            KeyCodes.DOT -> {
                val dot = if (shiftMutable.value.let { ShiftState.isUpperCase(it) }) "." else "."
                ic.commitText(dot, 1)
                resetShiftIfUpper()
            }
            KeyCodes.COMMA -> {
                ic.commitText(",", 1)
                resetShiftIfUpper()
            }
            KeyCodes.ARROW_LEFT -> {
                moveCursorStart()
            }
            KeyCodes.ARROW_RIGHT -> {
                moveCursorEnd()
            }
            KeyCodes.EMOJI -> {
                // Switch to emoji — handled by system
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_EMOJI))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_EMOJI))
            }
            else -> {
                // Regular key (letter, digit, punctuation from Unicode code point or custom code)
                val shiftedLabel = if (ShiftState.isUpperCase(shiftMutable.value)) {
                    label.uppercase()
                } else {
                    label.lowercase()
                }
                ic.commitText(shiftedLabel, 1)
                resetShiftIfUpper()
            }
        }

        // Notify intelligence engine (label is NOT stored for non-text keys)
        intelligenceObserver?.onKeyEvent(
            code = code,
            label = if (isDeletion || isModifier) "" else label,
            isDeletion = isDeletion,
            isModifier = isModifier,
            isLongPress = isLongPress,
            timestamp = now
        )

        // Re-evaluate interventions after significant typing activity
        if (!isDeletion && !isModifier) {
            intelligenceObserver?.submitMetrics(
                (intelligenceObserver as? com.pause.intelligence.TypingSignalCollector)
                    ?.computeMetrics() ?: return
            )
        }
    }

    fun handleHardwareKey(code: Int) {
        val ic = inputConnection ?: return
        when (code) {
            KeyEvent.KEYCODE_SHIFT_LEFT, KeyEvent.KEYCODE_SHIFT_RIGHT -> {
                val newState = ShiftState.next(shiftMutable.value)
                shiftMutable.value = newState
            }
            KeyEvent.KEYCODE_DEL -> {
                handleBackspace(ic, isLongPress = false)
            }
            KeyEvent.KEYCODE_ENTER -> {
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
            }
            KeyEvent.KEYCODE_SPACE -> {
                ic.commitText(" ", 1)
                if (shiftMutable.value is ShiftState.Upper) {
                    shiftMutable.value = ShiftState.Lower
                }
            }
            else -> {
                // Forward to InputConnection as a hardware key event
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, code))
                ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, code))
            }
        }
    }

    /**
     * Handles backspace — single press deletes one char before cursor.
     * Long-press mode repeats deletion (handled by BackspaceHandler).
     */
    private fun handleBackspace(ic: InputConnection, isLongPress: Boolean) {
        // Delete one character before the cursor
        ic.deleteSurroundingText(1, 0)
    }

    fun cycleLanguage() {
        val current = languageMutable.value
        val next = Language.next(current)
        languageMutable.value = next
        // Reset shift when switching languages
        shiftMutable.value = ShiftState.Lower
        intelligenceObserver?.onLanguageChanged(next)
    }

    fun switchToSymbols() {
        if (modeMutable.value == KeyboardMode.ALPHA) {
            modeMutable.value = KeyboardMode.SYMBOLS
        } else {
            modeMutable.value = KeyboardMode.ALPHA
        }
        intelligenceObserver?.onModeChanged(modeMutable.value)
    }

    fun switchToNumbers() {
        modeMutable.value = KeyboardMode.NUMBERS
        intelligenceObserver?.onModeChanged(modeMutable.value)
    }

    fun resetShift() {
        shiftMutable.value = ShiftState.Lower
    }

    fun resetShiftIfUpper() {
        if (shiftMutable.value is ShiftState.Upper) {
            shiftMutable.value = ShiftState.Lower
        }
    }

    private fun computeReturnLabel(): Int {
        val info = editorInfoMutable.value
        return when (info?.imeOptions?.and(android.view.inputmethod.EditorInfo.IME_FLAG_NO_EXTRACT) ?: 0) {
            android.view.inputmethod.EditorInfo.IME_ACTION_DONE -> android.view.inputmethod.EditorInfo.IME_ACTION_DONE
            android.view.inputmethod.EditorInfo.IME_ACTION_GO -> android.view.inputmethod.EditorInfo.IME_ACTION_GO
            android.view.inputmethod.EditorInfo.IME_ACTION_NEXT -> android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
            android.view.inputmethod.EditorInfo.IME_ACTION_SEND -> android.view.inputmethod.EditorInfo.IME_ACTION_SEND
            android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH -> android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH
            else -> android.view.inputmethod.EditorInfo.IME_ACTION_UNSPECIFIED
        }
    }

    private fun moveCursorStart() {
        val ic = inputConnection ?: return
        val (start, end) = getCurrentCursorPosition()
        if (start > 0) {
            ic.setSelection(start - 1, start - 1)
        }
    }

    private fun moveCursorEnd() {
        val ic = inputConnection ?: return
        val (start, end) = getCurrentCursorPosition()
        ic.setSelection(end + 1, end + 1)
    }

    private fun getCurrentCursorPosition(): Pair<Int, Int> {
        val ic = inputConnection ?: return 0 to 0
        val text = ic.getTextBeforeCursor(1, 0)?.toString() ?: ""
        val after = ic.getTextAfterCursor(1, 0)?.toString() ?: ""
        var cursorPos = 0
        if (text.isNotEmpty()) cursorPos += text.length
        return cursorPos to (cursorPos + after.length.coerceAtLeast(0))
    }

    fun setKeyboardDefinition(definition: KeyboardDefinition) {
        definitionMutable.value = definition
    }

    fun dismissPopups() {
        activePopupKeys.clear()
        languagePickerOpen.value = false
    }

    fun onSessionStart(ic: InputConnection?) {
        inputConnection = ic
        shiftMutable.value = ShiftState.Lower
    }

    fun clear() {
        inputConnection = null
        activePopupKeys.clear()
    }

    /**
     * Returns the current message length from the editor's text field
     * WITHOUT reading the text content itself. Used by the intelligence
     * engine for message-length trend analysis.
     */
    fun getEditorTextLength(): Int {
        val ic = inputConnection ?: return 0
        // We only count the text length, not the content
        val before = ic.getTextBeforeCursor(Int.MAX_VALUE, 0)
        val after = ic.getTextAfterCursor(Int.MAX_VALUE, 0)
        val total = (before?.length ?: 0) + (after?.length ?: 0)
        // Clear references immediately — we never store the text
        return total
    }
}

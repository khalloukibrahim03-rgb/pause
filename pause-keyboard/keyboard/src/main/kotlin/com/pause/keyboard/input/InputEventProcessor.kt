package com.pause.keyboard.input

import android.view.KeyEvent
import android.view.inputmethod.InputConnection
import com.pause.shared.KeyCodes
import javax.inject.Inject

/**
 * Translates logical key events into InputConnection operations.
 *
 * This class acts as the single dispatch point for all text production
 * and deletion — the keyboard UI delegates all input to it so that
 * input logic is testable in isolation.
 */
class InputEventProcessor @Inject constructor() {

    fun commitText(ic: InputConnection?, text: String, isLongPress: Boolean) {
        ic ?: return
        val commitText = if (isLongPress) text.uppercase() else text
        ic.commitText(commitText, 1)
    }

    fun commitCharacter(ic: InputConnection?, char: Char, shiftActive: Boolean) {
        ic ?: return
        val text = if (shiftActive) char.uppercase() else char.lowercase()
        ic.commitText(text, 1)
    }

    fun deleteCharacter(ic: InputConnection?, count: Int = 1) {
        ic ?: return
        ic.deleteSurroundingText(count, 0)
    }

    fun sendEnter(ic: InputConnection?) {
        ic ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
    }

    fun sendEmoji(ic: InputConnection?) {
        ic ?: return
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_EMOJI))
        ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_EMOJI))
    }

    fun moveCursor(ic: InputConnection?, delta: Int) {
        ic ?: return
        val (start, end) = getSelection(ic)
        val newStart = (start + delta).coerceAtLeast(0)
        val newEnd = (end + delta).coerceAtLeast(0)
        ic.setSelection(newStart, newEnd)
    }

    fun commitPunctuation(ic: InputConnection?, punctuation: String) {
        ic ?: return
        ic.commitText(punctuation, 1)
    }

    private fun getSelection(ic: InputConnection?): Pair<Int, Int> {
        if (ic == null) return 0 to 0
        val before = ic.getTextBeforeCursor(1, 0)?.length ?: 0
        val after = ic.getTextAfterCursor(1, 0)?.length ?: 0
        return before to (before + after)
    }

    companion object {
        fun isModifierCode(code: Int): Boolean =
            code == KeyCodes.SHIFT || code == KeyCodes.CAPS_LOCK

        fun isDeletionCode(code: Int): Boolean = code == KeyCodes.BACKSPACE
    }
}

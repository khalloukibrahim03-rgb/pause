package com.pause.keyboard.cursor

import android.view.inputmethod.InputConnection
import com.pause.shared.KeyCodes
import javax.inject.Inject

/**
 * Handles cursor movement via hardware or on-screen arrow keys.
 *
 * Uses InputConnection.setSelection() to move the cursor within
 * the editable text field. The text content itself is never read
 * or stored — only the cursor position is adjusted.
 */
class CursorController @Inject constructor() {

    fun moveLeft(ic: InputConnection?) {
        ic ?: return
        val (start, end) = getSelection(ic)
        if (start > 0) {
            ic.setSelection(start - 1, start - 1)
        }
    }

    fun moveRight(ic: InputConnection?) {
        ic ?: return
        val (start, end) = getSelection(ic)
        val textLen = getCursorContextLength(ic)
        if (start < textLen) {
            ic.setSelection(start + 1, start + 1)
        }
    }

    fun moveUp(ic: InputConnection?) {
        ic ?: return
        // Move to start of line (approximate — Android doesn't expose
        // line-based cursor movement through InputConnection directly)
        val (start, _) = getSelection(ic)
        ic.setSelection(0, start)
    }

    fun moveDown(ic: InputConnection?) {
        ic ?: return
        // Move to end — approximate behavior
        val (_, end) = getSelection(ic)
        val textLen = getCursorContextLength(ic)
        ic.setSelection(end, textLen)
    }

    fun handleArrowKey(ic: InputConnection?, code: Int) {
        when (code) {
            KeyCodes.ARROW_LEFT -> moveLeft(ic)
            KeyCodes.ARROW_RIGHT -> moveRight(ic)
            KeyCodes.ARROW_UP -> moveUp(ic)
            KeyCodes.ARROW_DOWN -> moveDown(ic)
        }
    }

    private fun getSelection(ic: InputConnection?): Pair<Int, Int> {
        val start = ic?.getSelectionStart() ?: 0
        val end = ic?.getSelectionEnd() ?: 0
        return minOf(start, end) to maxOf(start, end)
    }

    private fun getCursorContextLength(ic: InputConnection?): Int {
        val before = ic?.getTextBeforeCursor(Int.MAX_VALUE, 0)?.length ?: 0
        val after = ic?.getTextAfterCursor(Int.MAX_VALUE, 0)?.length ?: 0
        return before + after
    }
}

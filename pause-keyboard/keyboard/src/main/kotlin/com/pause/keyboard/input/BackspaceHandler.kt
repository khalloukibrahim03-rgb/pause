package com.pause.keyboard.input

import android.os.Handler
import android.os.Looper
import android.view.inputmethod.InputConnection
import com.pause.shared.KeyboardSettings
import javax.inject.Inject

/**
 * Handles backspace key events, including long-press auto-repeat.
 *
 * Single press: deletes one character before the cursor.
 * Long-press: initial delay ([settings.backspaceRepeatDelayMs]),
 * then auto-repeat at [settings.backspaceRepeatIntervalMs].
 */
class BackspaceHandler @Inject constructor() {

    private val handler = Handler(Looper.getMainLooper())
    private var isRepeating = false

    private var repeatRunnable: Runnable? = null

    fun handleBackspacePress(
        ic: InputConnection?,
        isLongPress: Boolean,
        settings: KeyboardSettings
    ) {
        ic ?: return
        ic.deleteSurroundingText(1, 0)

        if (isLongPress && !isRepeating) {
            startRepeat(ic, settings)
        }
    }

    fun handleBackspaceRelease() {
        stopRepeat()
    }

    private fun startRepeat(ic: InputConnection?, settings: KeyboardSettings) {
        isRepeating = true
        repeatRunnable = object : Runnable {
            override fun run() {
                ic?.deleteSurroundingText(1, 0)
                handler.postDelayed(this, settings.backspaceRepeatIntervalMs)
            }
        }
        handler.postDelayed(repeatRunnable!!, settings.backspaceRepeatDelayMs)
    }

    private fun stopRepeat() {
        if (isRepeating) {
            isRepeating = false
            repeatRunnable?.let { handler.removeCallbacks(it) }
            repeatRunnable = null
        }
    }
}

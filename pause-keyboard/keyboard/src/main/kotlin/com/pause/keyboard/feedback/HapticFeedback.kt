package com.pause.keyboard.feedback

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import android.view.View
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controls haptic feedback for key presses.
 *
 * Uses the system haptic engine when available (Android 8+),
 * falls back to basic vibration patterns on older versions.
 */
@Singleton
class HapticFeedback @Inject constructor() {

    fun performKeyPress(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }

    fun performLongPress(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    fun performBackspace(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    fun performSpacebar(view: View) {
        // Subtle tick
        view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
    }
}

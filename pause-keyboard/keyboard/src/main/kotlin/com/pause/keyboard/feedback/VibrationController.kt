package com.pause.keyboard.feedback

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Controls vibration intensity for key presses based on user settings.
 * Reads from [com.pause.shared.KeyboardSettings.vibrationIntensity].
 */
@Singleton
class VibrationController @Inject constructor(
    private val vibrator: Vibrator
) {

    fun vibrate(intensity: Int) {
        if (intensity <= 0) return
        val effectiveIntensity = intensity.coerceIn(0, 255)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            val amplitude = (effectiveIntensity / 255f * 255).toInt().coerceIn(1, 255)
            val effect = VibrationEffect.createOneShot(10, amplitude)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(10)
        }
    }

    fun isSupported(): Boolean {
        return vibrator.hasVibrator()
    }
}

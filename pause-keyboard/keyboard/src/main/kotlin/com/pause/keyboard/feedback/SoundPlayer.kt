package com.pause.keyboard.feedback

import android.content.Context
import android.media.AudioManager
import android.media.SoundPool
import android.view.SoundEffectConstants
import android.view.View
import androidx.annotation.RawRes
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Plays key-press sounds. Uses the system's built-in key press sound
 * for simplicity, or a custom SoundPool sound if provided.
 */
@Singleton
class SoundPlayer @Inject constructor(
    private val context: Context
) {

    private var soundPool: SoundPool? = null
    private var keyPressSoundId: Int = 0
    private var isLoaded = false

    fun playKeyPress(view: View) {
        view.playSoundEffect(SoundEffectConstants.CLICK)
    }

    fun playBackspace(view: View) {
        view.playSoundEffect(SoundEffectConstants.VIRTUAL_KEY)
    }

    fun playEnter(view: View) {
        view.playSoundEffect(SoundEffectConstants.VIRTUAL_KEY)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        isLoaded = false
    }

    private fun ensureSoundPool() {
        if (isLoaded) return
        soundPool = SoundPool.Builder().build()
        // Key press sound would be loaded from resources here.
        // For now, we use the system click sound which is sufficient.
        isLoaded = true
    }
}

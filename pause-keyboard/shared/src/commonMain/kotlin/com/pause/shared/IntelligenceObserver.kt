package com.pause.shared

import androidx.annotation.IntRange

/**
 * Interface that the keyboard calls for every relevant typing event.
 *
 * The keyboard must NOT pass actual text content through this interface.
 * Only key codes, timing, and boolean flags are transmitted.
 * The collector strips any labels before aggregation.
 */
interface IntelligenceObserver {

    /** Called on every key press or release. */
    fun onKeyEvent(
        code: Int,
        label: String,
        isDeletion: Boolean,
        isModifier: Boolean,
        isLongPress: Boolean,
        timestamp: Long
    )

    /** Called when the user stops typing for a measurable pause. */
    fun onPauseStart(timestamp: Long)

    /** Called when the user resumes typing after a pause. */
    fun onResumeFromPause(timestamp: Long)

    /** Called when the shift / caps state changes. */
    fun onShiftStateChanged(state: ShiftState)

    /** Called when the input language changes. */
    fun onLanguageChanged(language: Language)

    /** Called when the keyboard mode (letters/symbols/numbers) changes. */
    fun onModeChanged(mode: KeyboardMode)

    /** Called periodically with the latest aggregate metrics. */
    fun submitMetrics(metrics: TypingMetrics)

    /**
     * Returns the number of characters in the currently focused text field
     * (from the InputConnection), WITHOUT reading the text content itself.
     * The collector uses this to track message-length trends over time.
     */
    fun getMessageLength(): Int
}

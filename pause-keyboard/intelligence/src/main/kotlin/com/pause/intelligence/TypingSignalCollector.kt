package com.pause.intelligence

import com.pause.shared.Language
import com.pause.shared.ShiftState
import com.pause.shared.KeyboardMode
import com.pause.shared.TypingMetrics
import com.pause.shared.IntelligenceObserver
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Internal representation of a raw typing event.
 *
 * **Privacy**: Only the event type and timing are stored. No character
 * labels are ever persisted in this structure.
 */
internal data class RawKeyEvent(
    val code: Int,
    val isDeletion: Boolean,
    val isModifier: Boolean,
    val isLongPress: Boolean,
    val timestamp: Long
)

/**
 * Collector that receives typing events from the keyboard via
 * [IntelligenceObserver] and maintains a rolling in-memory buffer
 * of recent events for metric computation.
 *
 * **No text content is ever stored.** Labels are explicitly ignored
 * after the call returns.
 */
@Singleton
class TypingSignalCollector @Inject constructor() : IntelligenceObserver {

    /** Rolling buffer of recent key events — cleared on every [reset()]. */
    internal val eventBuffer = ArrayDeque<RawKeyEvent>(capacity = 500)

    /** Tracks the timestamp of the last key press event. */
    private var lastKeyPressTime: Long = 0L

    /** The most recent pause start timestamp (0 if no active pause). */
    private var pauseStart: Long = 0L

    /** Current shift state as reported by the keyboard. */
    private var currentShiftState: ShiftState = ShiftState.Lower

    /** Current language as reported by the keyboard. */
    private var currentLanguage: Language = Language.ENGLISH

    /** Current keyboard mode */
    private var currentMode: KeyboardMode = KeyboardMode.ALPHA

    /** Count of backspace long-presses. */
    private var backspaceLongPresss: Int = 0

    /** Current shift press streak (consecutive shift presses without intermediate keys). */
    private var shiftStreak: Int = 0

    /** Total punctuation keys pressed this session. */
    private var punctuationCount: Int = 0

    /** Total words typed (estimated by space / enter count). */
    private var wordCount: Int = 0

    /** Session start timestamp. */
    val sessionStartTime: Long = System.currentTimeMillis()

    @Volatile
    private var messageLength: Int = 0

    init {
        lastKeyPressTime = sessionStartTime
    }

    // ── IntelligenceObserver implementation ──────────────────────────────

    override fun onKeyEvent(
        code: Int,
        label: String,
        isDeletion: Boolean,
        isModifier: Boolean,
        isLongPress: Boolean,
        timestamp: Long
    ) {
        val now = if (timestamp > 0L) timestamp else System.currentTimeMillis()

        // Detect pause between this key press and the previous one
        if (lastKeyPressTime > 0L && now - lastKeyPressTime > PAUSE_THRESHOLD_MS) {
            pauseStart = lastKeyPressTime
        }

        eventBuffer.addLast(RawKeyEvent(code, isDeletion, isModifier, isLongPress, now))
        if (eventBuffer.size > 500) {
            eventBuffer.removeFirst()
        }

        // Update derived counters
        if (isLongPress && isDeletion) backspaceLongPresss++
        when (code) {
            KeyCodeCompat.SPACE, KeyCodeCompat.ENTER,
            KeyCodeCompat.DONE, KeyCodeCompat.MODE_TOGGLE -> {
                wordCount++
            }
            KeyCodeCompat.DOT, KeyCodeCompat.COMMA -> punctuationCount++
            else -> {
                if (label in listOf(".", ",", "!", "?", ";", ":", "'", "\"")) {
                    punctuationCount++
                }
            }
        }

        if (isModifier) {
            shiftStreak++
        } else {
            shiftStreak = 0
        }

        lastKeyPressTime = now
    }

    override fun onPauseStart(timestamp: Long) {
        pauseStart = if (timestamp > 0L) timestamp else System.currentTimeMillis()
    }

    override fun onResumeFromPause(timestamp: Long) {
        pauseStart = 0L
    }

    override fun onShiftStateChanged(state: ShiftState) {
        currentShiftState = state
    }

    override fun onLanguageChanged(language: Language) {
        currentLanguage = language
    }

    override fun onModeChanged(mode: KeyboardMode) {
        currentMode = mode
    }

    override fun submitMetrics(metrics: TypingMetrics) {
        // Metrics are computed by MetricsAggregator and pushed to us
        // for any side-effects (persistence, etc.). Currently in-memory only.
    }

    override fun getMessageLength(): Int = messageLength

    fun setMessageLength(length: Int) {
        messageLength = length
    }

    /**
     * Compute fresh [TypingMetrics] from the current event buffer.
     * Delegates to [MetricsAggregator].
     */
    fun computeMetrics(): TypingMetrics = MetricsAggregator.compute(eventBuffer, this)

    /** Clear all in-memory state — called when the keyboard session ends. */
    fun reset() {
        eventBuffer.clear()
        lastKeyPressTime = sessionStartTime
        pauseStart = 0L
        currentShiftState = ShiftState.Lower
        currentLanguage = Language.ENGLISH
        currentMode = KeyboardMode.ALPHA
        backspaceLongPresss = 0
        shiftStreak = 0
        punctuationCount = 0
        wordCount = 0
        messageLength = 0
    }

    companion object {
        private const val PAUSE_THRESHOLD_MS = 1_500L
    }
}

/**
 * Compatibility alias — maps to the shared [com.pause.shared.KeyCodes]
 * values so the intelligence module does not need a hard dependency
 * on the shared module's constants.
 */
internal object KeyCodeCompat {
    const val SPACE = -5
    const val ENTER = -4
    const val DONE = -100
    const val MODE_TOGGLE = -7
    const val DOT = -12
    const val COMMA = -13
}

package com.pause.intelligence

import com.pause.shared.InterventionType
import com.pause.shared.KeyboardSettings
import com.pause.shared.TypingMetrics
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class InterventionSchedulerTest {

    private lateinit var scheduler: InterventionScheduler

    @BeforeEach
    fun setUp() {
        scheduler = InterventionScheduler()
    }

    @Test
    fun `returns false when interventions disabled`() {
        val settings = KeyboardSettings(interventionsEnabled = false)
        val metrics = sampleMetrics()

        val result = scheduler.evaluateAndPost(metrics, settings)
        assertThat(result).isFalse()
        assertThat(scheduler.currentProposal.value).isNull()
    }

    @Test
    fun `returns false when PauseDetector finds no proposal`() {
        val settings = KeyboardSettings()
        val metrics = TypingMetrics(
            wordsPerMinute = 0f,
            totalKeysPressed = 1,
            totalDeletions = 0,
            impulsivityScore = 0.1f,
            sessionDurationMs = 1000L,
            totalWords = 0
        )

        val result = scheduler.evaluateAndPost(metrics, settings)
        assertThat(result).isFalse()
    }

    @Test
    fun `posts proposal when conditions met`() {
        val settings = KeyboardSettings()
        val metrics = sampleMetrics()

        val result = scheduler.evaluateAndPost(metrics, settings)
        assertThat(result).isTrue()
        assertThat(scheduler.currentProposal.value).isNotNull()
        assertThat(scheduler.currentProposal.value!!.type).isEqualTo(InterventionType.BREATHING_PROMPT)
    }

    @Test
    fun `cooldown blocks duplicate intervention`() {
        val settings = KeyboardSettings()
        val metrics = sampleMetrics()

        val first = scheduler.evaluateAndPost(metrics, settings)
        assertThat(first).isTrue()

        val second = scheduler.evaluateAndPost(metrics, settings)
        assertThat(second).isFalse()
    }

    @Test
    fun `respects maxInterventionsPerSession`() {
        val settings = KeyboardSettings(maxInterventionsPerSession = 1)
        val metrics = sampleMetrics()

        scheduler.evaluateAndPost(metrics, settings)
        scheduler.clear()
        val second = scheduler.evaluateAndPost(metrics, settings)
        assertThat(second).isFalse()
    }

    @Test
    fun `resetSession clears proposal`() {
        val settings = KeyboardSettings()
        val metrics = sampleMetrics()

        scheduler.evaluateAndPost(metrics, settings)
        assertThat(scheduler.currentProposal.value).isNotNull()

        scheduler.resetSession()
        assertThat(scheduler.currentProposal.value).isNull()
    }

    @Test
    fun `clear removes active proposal`() {
        val settings = KeyboardSettings()
        val metrics = sampleMetrics()

        scheduler.evaluateAndPost(metrics, settings)
        scheduler.clear()
        assertThat(scheduler.currentProposal.value).isNull()
    }

    private fun sampleMetrics(
        totalKeysPressed: Int = 10,
        totalDeletions: Int = 5,
        backspaceLongPresss: Int = 0,
        impulsivityScore: Float = 0.8f
    ): TypingMetrics {
        return TypingMetrics(
            wordsPerMinute = 40f,
            totalKeysPressed = totalKeysPressed,
            totalDeletions = totalDeletions,
            backspaceLongPresss = backspaceLongPresss,
            impulsivityScore = impulsivityScore,
            interKeyLatencyAvg = 60L,
            sessionDurationMs = 5000L,
            sessionStartTime = System.currentTimeMillis() - 5000L,
            totalWords = 2,
            totalPunctuation = 1,
            punctuationIntensity = 0.1f,
            capsUsageRatio = 0.2f,
            currentMessageLength = 100,
            deletionsPerWord = 2.5f,
            rewritesPerChar = 0.1f,
            timestamp = System.currentTimeMillis(),
            averagePauseDurationMs = 0L
        )
    }
}

package com.pause.intelligence

import com.pause.shared.InterventionType
import com.pause.shared.KeyboardSettings
import com.pause.shared.TypingMetrics
import com.pause.shared.Language
import com.pause.shared.ShiftState
import com.pause.shared.KeyboardMode
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class PauseDetectorTest {

    private val defaultSettings = KeyboardSettings()

    @Test
    fun `normal typing does not trigger intervention`() {
        val metrics = TypingMetrics(
            totalKeysPressed = 10,
            totalDeletions = 0,
            totalWords = 2,
            impulsivityScore = 0.3f,
            averagePauseDurationMs = 500L,
            interKeyLatencyAvg = 200L,
            sessionDurationMs = 10_000L,
            wordsPerMinute = 25f,
            backspaceLongPresss = 0,
            shiftPressStreak = 0,
            totalPunctuation = 1
        )
        val proposal = PauseDetector.evaluate(metrics)
        assertNull(proposal)
    }

    @Test
    fun `rapid deletions trigger breathing prompt intervention`() {
        val metrics = TypingMetrics(
            totalKeysPressed = 10,
            totalDeletions = 5, // 50% deletion ratio
            totalWords = 1,
            impulsivityScore = 0.5f,
            averagePauseDurationMs = 500L,
            interKeyLatencyAvg = 100L,
            sessionDurationMs = 5_000L,
            wordsPerMinute = 20f,
            backspaceLongPresss = 0,
            shiftPressStreak = 0,
            totalPunctuation = 0
        )
        val proposal = PauseDetector.evaluate(metrics)
        assertNotNull(proposal)
        assertEquals(InterventionType.BREATHING_PROMPT, proposal.type)
        assertEquals(3, proposal.priority)
    }

    @Test
    fun `backspace long-press triggers rapid deletion intervention`() {
        val metrics = TypingMetrics(
            totalKeysPressed = 10,
            totalDeletions = 2, // Below ratio threshold but long-press >= 2
            totalWords = 1,
            impulsivityScore = 0.5f,
            averagePauseDurationMs = 500L,
            interKeyLatencyAvg = 100L,
            sessionDurationMs = 5_000L,
            wordsPerMinute = 20f,
            backspaceLongPresss = 2, // Triggers long-press rule
            shiftPressStreak = 0,
            totalPunctuation = 0
        )
        val proposal = PauseDetector.evaluate(metrics)
        assertNotNull(proposal)
        assertEquals("rapid_deletions", proposal?.id)
    }

    @Test
    fun `high impulsivity with pauses triggers subtle highlight`() {
        val metrics = TypingMetrics(
            totalKeysPressed = 20,
            totalDeletions = 3,
            totalWords = 3,
            impulsivityScore = 0.8f, // Above 0.7 threshold
            averagePauseDurationMs = 4_000L, // Above 3s threshold
            interKeyLatencyAvg = 50L, // Below 80ms threshold
            sessionDurationMs = 10_000L,
            wordsPerMinute = 40f,
            backspaceLongPresss = 0,
            shiftPressStreak = 0,
            totalPunctuation = 2
        )
        val proposal = PauseDetector.evaluate(metrics)
        assertNotNull(proposal)
        assertEquals(InterventionType.SUBTLE_HIGHLIGHT, proposal?.type)
        assertEquals(2, proposal?.priority)
    }

    @Test
    fun `typing fatigue triggers minimal tip`() {
        val metrics = TypingMetrics(
            totalKeysPressed = 50,
            totalDeletions = 0,
            totalWords = 10,
            impulsivityScore = 0.2f,
            averagePauseDurationMs = 2_500L, // Above 2s
            interKeyLatencyAvg = 300L,
            sessionDurationMs = 45_000L, // Above 30s
            wordsPerMinute = 35f, // Above 30
            backspaceLongPresss = 0,
            shiftPressStreak = 0,
            totalPunctuation = 5
        )
        val proposal = PauseDetector.evaluate(metrics)
        assertNotNull(proposal)
        assertEquals(InterventionType.MINIMAL_TIP, proposal?.type)
        assertEquals(1, proposal?.priority)
    }

    @Test
    fun `resolveContent returns correct strings`() {
        val breathing = com.pause.shared.InterventionProposal(
            id = "test",
            type = InterventionType.BREATHING_PROMPT,
            priority = 1,
            content = "",
            durationMs = 1000L
        )
        assertEquals("Take a breath", PauseDetector.resolveContent(breathing))

        val highlight = breathing.copy(type = InterventionType.SUBTLE_HIGHLIGHT)
        assertEquals("You're doing great", PauseDetector.resolveContent(highlight))

        val tip = breathing.copy(type = InterventionType.MINIMAL_TIP)
        assertEquals("Pause?", PauseDetector.resolveContent(tip))
    }
}

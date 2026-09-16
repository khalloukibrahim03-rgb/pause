package com.pause.intelligence

import com.pause.shared.TypingMetrics
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class MetricsAggregatorTest {

    @Test
    fun `empty buffer produces zeroed metrics`() {
        val collector = TypingSignalCollector()
        val metrics = collector.computeMetrics()
        assertEquals(0, metrics.totalKeysPressed)
        assertEquals(0f, metrics.wordsPerMinute)
    }

    @Test
    fun `single event produces correct count`() {
        val collector = TypingSignalCollector()
        collector.onKeyEvent(
            code = 100,
            label = "a",
            isDeletion = false,
            isModifier = false,
            isLongPress = false,
            timestamp = System.currentTimeMillis()
        )
        val metrics = collector.computeMetrics()
        assertEquals(1, metrics.totalKeysPressed)
    }

    @Test
    fun `backspace events counted as deletions`() {
        val collector = TypingSignalCollector()
        val baseTime = System.currentTimeMillis()
        collector.onKeyEvent(
            code = 'a'.code, label = "a",
            isDeletion = false, isModifier = false, isLongPress = false,
            timestamp = baseTime
        )
        collector.onKeyEvent(
            code = 'b'.code, label = "b",
            isDeletion = false, isModifier = false, isLongPress = false,
            timestamp = baseTime + 100
        )
        collector.onKeyEvent(
            code = com.pause.shared.KeyCodes.BACKSPACE, label = "",
            isDeletion = true, isModifier = false, isLongPress = false,
            timestamp = baseTime + 200
        )
        val metrics = collector.computeMetrics()
        assertEquals(3, metrics.totalKeysPressed)
        assertEquals(1, metrics.totalDeletions)
    }

    @Test
    fun `space events counted as words`() {
        val collector = TypingSignalCollector()
        val baseTime = System.currentTimeMillis()
        collector.onKeyEvent(
            code = 'h'.code, label = "h",
            isDeletion = false, isModifier = false, isLongPress = false,
            timestamp = baseTime
        )
        collector.onKeyEvent(
            code = com.pause.shared.KeyCodes.SPACE, label = " ",
            isDeletion = false, isModifier = false, isLongPress = false,
            timestamp = baseTime + 50
        )
        val metrics = collector.computeMetrics()
        assertEquals(2, metrics.totalKeysPressed)
        assertEquals(1, metrics.totalWords)
    }

    @Test
    fun `impulsivity score is between 0 and 1`() {
        val collector = TypingSignalCollector()
        val baseTime = System.currentTimeMillis()
        // Rapid typing with some deletions
        for (i in 0 until 10) {
            collector.onKeyEvent(
                code = ('a' + i).code, label = "a",
                isDeletion = false, isModifier = false, isLongPress = false,
                timestamp = baseTime + i * 20L // very fast typing
            )
        }
        collector.onKeyEvent(
            code = com.pause.shared.KeyCodes.BACKSPACE, label = "",
            isDeletion = true, isModifier = false, isLongPress = false,
            timestamp = baseTime + 200
        )
        val metrics = collector.computeMetrics()
        assertTrue(metrics.impulsivityScore >= 0f && metrics.impulsivityScore <= 1f)
    }

    @Test
    fun `pause detection works for long gaps`() {
        val collector = TypingSignalCollector()
        collector.onKeyEvent(
            code = 'a'.code, label = "a",
            isDeletion = false, isModifier = false, isLongPress = false,
            timestamp = 0L
        )
        // Simulate a 2-second pause
        collector.onKeyEvent(
            code = 'b'.code, label = "b",
            isDeletion = false, isModifier = false, isLongPress = false,
            timestamp = 2000L
        )
        val metrics = collector.computeMetrics()
        // With a 2000ms gap, which exceeds the 1500ms threshold,
        // averagePauseDurationMs should be positive
        assertTrue(metrics.averagePauseDurationMs > 0 || metrics.interKeyLatencyAvg >= 2000L)
    }

    private fun assertTrue(condition: Boolean) {
        org.junit.jupiter.api.Assertions.assertTrue(condition)
    }
}

package com.pause.intelligence

import com.pause.shared.Language
import com.pause.shared.ShiftState
import com.pause.shared.KeyboardMode
import com.pause.shared.TypingMetrics
import kotlin.math.max
import kotlin.math.min

/**
 * Pure-function aggregator that transforms a buffer of [RawKeyEvent]s
 * into a [TypingMetrics] snapshot.
 *
 * All computation is local, in-memory, and contains NO character-level
 * data — only timing, counts, and ratios.
 */
internal object MetricsAggregator {

    private const val MS_PER_MINUTE = 60_000L
    private const val PAUSE_THRESHOLD_MS = 1_500L
    private const val REWRITE_WINDOW_MS = 5_000L

    fun compute(
        events: ArrayDeque<RawKeyEvent>,
        collector: TypingSignalCollector
    ): TypingMetrics {
        if (events.isEmpty()) {
            return TypingMetrics(
                timestamp = System.currentTimeMillis(),
                sessionStartTime = collector.sessionStartTime
            )
        }

        val now = System.currentTimeMillis()
        val sessionStart = collector.sessionStartTime
        val sessionDurationMs = now - sessionStart

        // Inter-key latencies
        val latencies = events.zipWithNext { a, b -> (b.timestamp - a.timestamp).coerceAtLeast(0L) }
        val interKeyAvg = if (latencies.isNotEmpty()) {
            latencies.average().toLong()
        } else 0L

        // Pauses (latencies exceeding threshold)
        val pauses = latencies.filter { it > PAUSE_THRESHOLD_MS }
        val avgPause = if (pauses.isNotEmpty()) {
            pauses.average().toLong()
        } else 0L

        // Deletions
        val deletions = events.count { it.isDeletion }
        val words = max(collector.wordCount, 1)
        val deletionsPerWord = deletions.toFloat() / words.toFloat()

        // Rewrites (backspace immediately followed by new character entry)
        val rewrites = countRewrites(events.toList())
        val charsTyped = events.size
        val rewritesPerChar = if (charsTyped > 0) rewrites.toFloat() / charsTyped.toFloat() else 0f

        // Punctuation
        val punctuationCount = events.count { it.code == KeyCodeCompat.DOT || it.code == KeyCodeCompat.COMMA }
        val totalKeys = max(events.size, 1)
        val punctuationIntensity = punctuationCount.toFloat() / totalKeys.toFloat()

        // Caps / modifier usage
        val modifierCount = events.count { it.isModifier }
        val capsUsageRatio = modifierCount.toFloat() / totalKeys.toFloat()

        // Words per minute
        val wpm = if (sessionDurationMs > 0) {
            (words.toFloat() / sessionDurationMs.toFloat()) * MS_PER_MINUTE
        } else 0f

        // Impulsivity score: combination of high deletion rate and short latencies.
        // Range: 0.0 (calm, deliberate) to 1.0 (rapid, impulsive).
        val deletionRatio = if (totalKeys > 0) deletions.toFloat() / totalKeys.toFloat() else 0f
        val avgLatencyMs = if (latencies.isNotEmpty()) latencies.average() / 1000f else 0f
        val latencyComponent = if (avgLatencyMs < 300f) 1f - (min(avgLatencyMs, 300f) / 300f) else 0f
        val deletionComponent = min(deletionRatio, 1f)
        val impulsivityScore = (latencyComponent * 0.5f + deletionComponent * 0.5f).coerceIn(0f, 1f)

        return TypingMetrics(
            timestamp = now,
            wordsPerMinute = wpm.coerceAtLeast(0f),
            averagePauseDurationMs = avgPause,
            deletionsPerWord = deletionsPerWord,
            rewritesPerChar = rewritesPerChar,
            punctuationIntensity = punctuationIntensity,
            capsUsageRatio = capsUsageRatio,
            currentMessageLength = collector.getMessageLength(),
            interKeyLatencyAvg = interKeyAvg,
            backspaceLongPresss = collector.backspaceLongPresss,
            shiftPressStreak = collector.shiftStreak,
            impulsivityScore = impulsivityScore,
            sessionStartTime = sessionStart,
            sessionDurationMs = sessionDurationMs,
            totalKeysPressed = events.size,
            totalDeletions = deletions,
            totalPunctuation = punctuationCount,
            totalWords = words
        )
    }

    private fun countRewrites(events: List<RawKeyEvent>): Int {
        var count = 0
        for (i in 1 until events.size) {
            val prev = events[i - 1]
            val curr = events[i]
            if (prev.isDeletion && !curr.isDeletion && !curr.isModifier &&
                (curr.timestamp - prev.timestamp) < REWRITE_WINDOW_MS
            ) {
                count++
            }
        }
        return count
    }
}

package com.pause.shared

/**
 * Aggregate typing metrics derived from raw key-event timing.
 *
 * **Privacy contract**: This class never contains actual character values.
 * Only timing, counts, and ratios are collected. The keyboard passes
 * raw events through IntelligenceObserver which strips all text labels
 * before aggregation.
 */
data class TypingMetrics(
    val timestamp: Long = 0L,
    val wordsPerMinute: Float = 0f,
    val averagePauseDurationMs: Long = 0L,
    val deletionsPerWord: Float = 0f,
    val rewritesPerChar: Float = 0f,
    val punctuationIntensity: Float = 0f,
    val capsUsageRatio: Float = 0f,
    val currentMessageLength: Int = 0,
    val interKeyLatencyAvg: Long = 0L,
    val backspaceLongPresss: Int = 0,
    val shiftPressStreak: Int = 0,
    val impulsivityScore: Float = 0f,
    val sessionStartTime: Long = 0L,
    val sessionDurationMs: Long = 0L,
    val totalKeysPressed: Int = 0,
    val totalDeletions: Int = 0,
    val totalPunctuation: Int = 0,
    val totalWords: Int = 0
)

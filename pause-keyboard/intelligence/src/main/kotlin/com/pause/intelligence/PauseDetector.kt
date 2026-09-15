package com.pause.intelligence

import com.pause.shared.InterventionProposal
import com.pause.shared.InterventionType
import com.pause.shared.TypingMetrics

/**
 * Analyzes [TypingMetrics] and produces an [InterventionProposal]
 * when local criteria indicate the user might benefit from a Pause.
 *
 * **Rules** (all local, in-memory):
 * 1. Rapid deletions: deletion ratio > 25% with >=5 deletions OR >=2 backspace long-presses
 * 2. High impulsivity + frequent pauses: impulsivity > 0.7, avg pause > 3s, fast typing (<80ms latency)
 * 3. Typing fatigue: session > 30s, avg pause > 2s, WPM > 30
 *
 * Each rule returns a proposal with priority. Only the highest-priority
 * active rule fires at any given evaluation.
 */
object PauseDetector {

    /**
     * Evaluate the latest [metrics] and return a proposal if criteria are met.
     */
    fun evaluate(metrics: TypingMetrics): InterventionProposal? {
        // Rule 1: Rapid deletions — user is deleting a lot
        val deletionRatio = if (metrics.totalKeysPressed > 0) {
            metrics.totalDeletions.toFloat() / metrics.totalKeysPressed
        } else 0f

        val rapidDeletion = deletionRatio > 0.25f && metrics.totalDeletions >= 5
        val rapidDeletionStreak = metrics.backspaceLongPresss >= 2

        if (rapidDeletion || rapidDeletionStreak) {
            return InterventionProposal(
                id = "rapid_deletions",
                type = InterventionType.BREATHING_PROMPT,
                priority = 3,
                content = "",
                durationMs = 3_000L
            )
        }

        // Rule 2: High impulsivity + short latencies + long pauses
        if (metrics.impulsivityScore > 0.7f &&
            metrics.averagePauseDurationMs > 3_000L &&
            metrics.interKeyLatencyAvg > 0 &&
            metrics.interKeyLatencyAvg < 80L
        ) {
            return InterventionProposal(
                id = "impulsive_typing",
                type = InterventionType.SUBTLE_HIGHLIGHT,
                priority = 2,
                content = "",
                durationMs = 4_000L
            )
        }

        // Rule 3: Long pause after heavy typing (potential fatigue)
        if (metrics.sessionDurationMs > 30_000L &&
            metrics.averagePauseDurationMs > 2_000L &&
            metrics.wordsPerMinute > 30f
        ) {
            return InterventionProposal(
                id = "typing_fatigue",
                type = InterventionType.MINIMAL_TIP,
                priority = 1,
                content = "",
                durationMs = 5_000L
            )
        }

        return null
    }

    /**
     * Produce neutral, non-judgmental content for an intervention.
     * The keyboard UI calls this to render the intervention text/label.
     * Content NEVER references the user's actual typed content.
     */
    fun resolveContent(proposal: InterventionProposal): String {
        return when (proposal.type) {
            InterventionType.BREATHING_PROMPT -> "Take a breath"
            InterventionType.SUBTLE_HIGHLIGHT -> "You're doing great"
            InterventionType.MINIMAL_TIP -> "Pause?"
        }
    }
}

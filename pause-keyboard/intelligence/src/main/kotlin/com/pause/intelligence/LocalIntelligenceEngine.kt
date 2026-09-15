package com.pause.intelligence

import com.pause.shared.InterventionProposal
import com.pause.shared.IntelligenceObserver
import com.pause.shared.KeyboardSettings
import com.pause.shared.TypingMetrics
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Top-level orchestrator that wires [TypingSignalCollector] →
 * [MetricsAggregator] → [PauseDetector] → [InterventionScheduler].
 *
 * The keyboard obtains the [IntelligenceObserver] from here to report
 * keystrokes, and observes [interventions] to react to Pause proposals.
 */
@Singleton
class LocalIntelligenceEngine @Inject constructor(
    private val collector: TypingSignalCollector,
    private val scheduler: InterventionScheduler,
    private val settingsProvider: () -> KeyboardSettings
) {

    /**
     * The keyboard calls this to obtain the [IntelligenceObserver]
     * that receives every key event it processes.
     */
    val signalObserver: IntelligenceObserver = collector

    /**
     * Hot stream that emits the currently-active [InterventionProposal]
     * (or null when no intervention should be shown).
     *
     * The keyboard collects this inside a lifecycle-aware coroutine scope
     * and renders [InterventionOverlay] accordingly.
     */
    val interventions: Flow<InterventionProposal?> = scheduler.currentProposal

    /**
     * Compute fresh metrics from the collector's event buffer,
     * feed them to the scheduler for intervention evaluation,
     * and emit the result to the interventions stream.
     *
     * Called after each key press (debounced) by the keyboard.
     */
    fun evaluateCurrentMetrics() {
        val metrics = collector.computeMetrics()
        val settings = settingsProvider()
        scheduler.evaluateAndPost(metrics, settings)
    }

    /** Called when the IME session ends — clears all in-memory state. */
    fun onSessionEnd() {
        collector.reset()
        scheduler.resetSession()
    }

    /**
     * Expose the latest metrics for the dashboard.
     * The dashboard calls this on a timer (e.g. every 5 seconds).
     */
    fun getCurrentMetrics(): TypingMetrics = collector.computeMetrics()
}

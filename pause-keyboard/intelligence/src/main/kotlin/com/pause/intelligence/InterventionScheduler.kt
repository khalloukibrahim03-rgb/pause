package com.pause.intelligence

import com.pause.shared.InterventionProposal
import com.pause.shared.InterventionType
import com.pause.shared.KeyboardSettings
import com.pause.shared.TypingMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages intervention delivery: cooldown, deduplication, max per session.
 *
 * Observes metrics from [LocalIntelligenceEngine], throttles proposals,
 * and exposes the currently-active proposal as a StateFlow.
 */
@Singleton
class InterventionScheduler @Inject constructor() {

    private val _currentProposal = MutableStateFlow<InterventionProposal?>(null)
    val currentProposal: StateFlow<InterventionProposal?> = _currentProposal.asStateFlow()

    private var lastInterventionTime: Long = 0L
    private var interventionsThisSession: Int = 0
    private val shownIds = mutableSetOf<String>()

    /**
     * Evaluate the latest [metrics] with [settings] and return true if
     * an intervention was accepted and shown.
     */
    fun evaluateAndPost(metrics: TypingMetrics, settings: KeyboardSettings): Boolean {
        if (!settings.interventionsEnabled) return false

        val proposal = PauseDetector.evaluate(metrics) ?: return false

        val now = System.currentTimeMillis()
        val cooldownPassed = now - lastInterventionTime >= settings.interventionCooldownMs
        val underMax = interventionsThisSession < settings.maxInterventionsPerSession
        val notDuplicate = !shownIds.contains(proposal.id)

        if (cooldownPassed && underMax && notDuplicate) {
            val resolved = proposal.copy(
                content = PauseDetector.resolveContent(proposal)
            )
            _currentProposal.value = resolved
            lastInterventionTime = now
            interventionsThisSession++
            shownIds.add(proposal.id)
            return true
        }

        return false
    }

    /** Clear the active proposal without counting it. */
    fun clear() {
        _currentProposal.value = null
    }

    /** Reset session counters — called when a new typing session begins. */
    fun resetSession() {
        interventionsThisSession = 0
        shownIds.clear()
        lastInterventionTime = 0L
        _currentProposal.value = null
    }
}

package com.pause.app.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pause.intelligence.LocalIntelligenceEngine
import com.pause.shared.TypingMetrics
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Dashboard screen.
 *
 * Periodically polls [LocalIntelligenceEngine] for the latest
 * aggregate typing metrics. **No message content is ever exposed.**
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val intelligenceEngine: LocalIntelligenceEngine
) : ViewModel() {

    private val _metrics = MutableStateFlow(TypingMetrics())
    val metrics: StateFlow<TypingMetrics> = _metrics.asStateFlow()

    init {
        startPolling()
    }

    private fun startPolling() {
        viewModelScope.launch {
            while (true) {
                _metrics.value = intelligenceEngine.getCurrentMetrics()
                delay(5_000L) // Refresh every 5 seconds
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Metrics continue to be cached in the intelligence engine
    }
}

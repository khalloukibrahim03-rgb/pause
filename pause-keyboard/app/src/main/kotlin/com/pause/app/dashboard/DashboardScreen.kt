package com.pause.app.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillParentMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pause.app.R
import com.pause.shared.TypingMetrics

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onSettingsClick: () -> Unit
) {
    val metrics = viewModel.metrics.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = { Text(stringResource(R.string.dashboard_title)) },
            actions = {
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = stringResource(R.string.settings_title)
                    )
                }
            }
        )

        DashboardContent(metrics = metrics)
    }
}

@Composable
fun DashboardContent(metrics: TypingMetrics) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        MetricCard(
            title = stringResource(R.string.dashboard_impulsivity_label),
            value = String.format("%.2f", metrics.impulsivityScore),
            barValue = metrics.impulsivityScore,
            barColor = if (metrics.impulsivityScore > 0.7f) Color(0xFFFF9800) else Color(0xFF4CAF50)
        )

        MetricCard(
            title = stringResource(R.string.dashboard_wpm_label),
            value = String.format("%.1f", metrics.wordsPerMinute),
            barValue = (metrics.wordsPerMinute / 60f).coerceIn(0f, 1f),
            barColor = Color(0xFF4A90E2)
        )

        MetricCard(
            title = stringResource(R.string.dashboard_deletions_label),
            value = "${metrics.totalDeletions}",
            barValue = 0f,
            barColor = Color(0xFF757575)
        )

        MetricCard(
            title = "Deletions / word",
            value = String.format("%.2f", metrics.deletionsPerWord),
            barValue = (metrics.deletionsPerWord / 2f).coerceIn(0f, 1f),
            barColor = if (metrics.deletionsPerWord > 0.5f) Color(0xFFFF9800) else Color(0xFF4CAF50)
        )

        MetricCard(
            title = "Inter-key latency (ms)",
            value = "${metrics.interKeyLatencyAvg}",
            barValue = 0f,
            barColor = Color(0xFF757575)
        )

        MetricCard(
            title = "Session duration",
            value = formatDuration(metrics.sessionDurationMs),
            barValue = 0f,
            barColor = Color(0xFF757575)
        )

        Box(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    barValue: Float,
    barColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            if (barValue > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0xFFE0E0E0))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(3.dp))
                            .background(barColor)
                            .fillParentMaxWidth(barValue)
                    )
                }
            }
        }
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%d:%02d", minutes, seconds)
}

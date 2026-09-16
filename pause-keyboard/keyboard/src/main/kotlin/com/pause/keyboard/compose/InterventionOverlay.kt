package com.pause.keyboard.compose

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pause.shared.InterventionProposal
import com.pause.shared.KeyboardSettings
import kotlinx.coroutines.delay

/**
 * Subtle Pause intervention UI rendered over the keyboard.
 *
 * Design principles:
 * - NEVER blocks typing — rendered as a non-interactive overlay
 * - NEVER displays user's typed content
 * - Subtly pulses/fades in and out
 * - Auto-dismisses after duration
 * - Tappable (optional) for minimal feedback only
 */
@Composable
fun InterventionOverlay(
    proposal: InterventionProposal,
    settings: KeyboardSettings
) {
    val alpha = remember(proposal.id) { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(proposal.id) {
        alpha.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(durationMillis = 400)
        )
        delay(proposal.durationMs - 800)
        alpha.animateTo(0f, tween(durationMillis = 400))
    }

    if (alpha.value > 0.01f) {
        val textColor = if (settings.isDarkTheme) Color(0xFF81C784) else Color(0xFF1A73E9)
        val bgColor = if (settings.isDarkTheme) Color(0x3381C784) else Color(0x204A90E2)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .alpha(alpha.value)
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(bgColor, bgColor.copy(alpha = 0.1f), bgColor)
                    )
                )
        ) {
            Text(
                text = proposal.content.ifEmpty { "Take a breath" },
                style = MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

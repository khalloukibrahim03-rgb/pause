package com.pause.keyboard.compose

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pause.shared.InterventionProposal
import com.pause.shared.KeyboardDefinition
import com.pause.shared.KeyboardSettings
import com.pause.shared.KeySpec
import com.pause.shared.ShiftState
import kotlinx.coroutines.delay

@Composable
fun KeyboardRow(
    keys: List<KeySpec>,
    isRtl: Boolean,
    isShiftActive: Boolean,
    settings: KeyboardSettings,
    keyStates: Map<Int, Boolean>,
    onPressed: (code: Int, label: String) -> Unit,
    onLongPress: (code: Int, label: String) -> Unit
) {
    val displayKeys = if (isRtl) keys.asReversed() else keys

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        displayKeys.forEach { key ->
            KeyComposable(
                key = key,
                modifier = Modifier.weight(key.widthRatio),
                isPressed = keyStates[key.code] ?: false,
                isShiftActive = isShiftActive,
                settings = settings,
                onPressed = onPressed,
                onLongPress = onLongPress
            )
        }
    }
}

@Composable
fun KeyboardLayout(
    definition: KeyboardDefinition,
    settings: KeyboardSettings,
    shiftState: ShiftState,
    keyStates: Map<Int, Boolean>,
    onPressed: (code: Int, label: String) -> Unit,
    onLongPress: (code: Int, label: String) -> Unit
) {
    val isRtl = definition.language.isRtl
    val isShiftActive = ShiftState.isUpperCase(shiftState)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .background(if (settings.isDarkTheme) Color(0xFF212121) else Color(0xFFF5F5F5))
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        definition.rows.forEach { row ->
            KeyboardRow(
                keys = row,
                isRtl = isRtl,
                isShiftActive = isShiftActive,
                settings = settings,
                keyStates = keyStates,
                onPressed = onPressed,
                onLongPress = onLongPress
            )
        }
    }
}

@Composable
fun InterventionOverlay(
    proposal: InterventionProposal,
    settings: KeyboardSettings
) {
    val alpha = remember(proposal.id) { Animatable(0f) }

    LaunchedEffect(proposal.id) {
        alpha.animateTo(0.7f, tween(durationMillis = 400))
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
                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                fontSize = 14.sp,
                color = textColor,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

@Composable
fun PauseKeyboard(
    keyboardState: com.pause.keyboard.KeyboardState,
    settings: KeyboardSettings,
    interventionProposal: InterventionProposal?
) {
    val definition = keyboardState.keyboardDefinition.value
    val shift = keyboardState.shiftState.value
    val isRtl = keyboardState.language.value.isRtl

    val keyStates = remember { mutableStateMapOf<Int, Boolean>() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp),
        color = if (settings.isDarkTheme) Color(0xFF212121) else Color(0xFFF5F5F5)
    ) {
        Box {
            definition?.let { def ->
                KeyboardLayout(
                    definition = def,
                    settings = settings,
                    shiftState = shift,
                    keyStates = keyStates,
                    onPressed = { code, label ->
                        keyStates[code] = true
                        keyboardState.pressKey(code, label, isLongPress = false)
                        keyStates[code] = false
                    },
                    onLongPress = { code, label ->
                        keyStates[code] = true
                        keyboardState.pressKey(code, label, isLongPress = true)
                        keyStates[code] = false
                    }
                )
            }

            interventionProposal?.let { proposal ->
                InterventionOverlay(proposal = proposal, settings = settings)
            }
        }
    }
}

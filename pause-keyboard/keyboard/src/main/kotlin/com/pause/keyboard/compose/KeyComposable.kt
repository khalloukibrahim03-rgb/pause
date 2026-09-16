package com.pause.keyboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pause.shared.KeySpec
import com.pause.shared.KeyType
import com.pause.shared.KeyboardSettings

@Composable
fun KeyComposable(
    key: KeySpec,
    modifier: Modifier,
    isPressed: Boolean,
    isShiftActive: Boolean,
    settings: KeyboardSettings,
    onPressed: (code: Int, label: String) -> Unit,
    onLongPress: (code: Int, label: String) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val displayLabel = computeDisplayLabel(key, isShiftActive)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(keyBackgroundColor(key, isPressed, settings), shape = RoundedCornerShape(8.dp))
            .border(1.dp, keyBorderColor(settings), shape = RoundedCornerShape(8.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { offset ->
                        onPressed(key.code, displayLabel)
                        waitForUpOrCancellation()
                    },
                    onLongPress = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onLongPress(key.code, displayLabel)
                    }
                )
            }
    ) {
        Text(
            text = displayLabel,
            style = MaterialTheme.typography.titleMedium,
            fontSize = 20.sp,
            fontWeight = if (key.type == KeyType.SHIFT) FontWeight.Bold else FontWeight.Normal,
            color = keyTextColor(key, settings),
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

private fun computeDisplayLabel(key: KeySpec, isShiftActive: Boolean): String {
    if (key.label.isEmpty()) return key.label
    return when (key.type) {
        KeyType.SHIFT -> if (isShiftActive) "⇪" else "⇧"
        KeyType.BACKSPACE -> "⌫"
        KeyType.ENTER -> "⏎"
        KeyType.SPACE -> " "
        KeyType.LANGUAGE -> "🌐"
        KeyType.MODE_TOGGLE -> "≣"
        KeyType.EMOJI -> "😀"
        KeyType.ARROW_LEFT -> "←"
        KeyType.ARROW_RIGHT -> "→"
        KeyType.ARROW_UP -> "↑"
        KeyType.ARROW_DOWN -> "↓"
        else -> {
            if (key.label.length == 1 && key.label[0].isLetter()) {
                if (isShiftActive) key.label.uppercase() else key.label.lowercase()
            } else key.label
        }
    }
}

private fun keyBackgroundColor(key: KeySpec, isPressed: Boolean, settings: KeyboardSettings): Color {
    val alpha = if (isPressed) 0.7f else 0.9f
    val isDark = settings.isDarkTheme
    return when (key.type) {
        KeyType.SHIFT, KeyType.SYSTEM, KeyType.LANGUAGE, KeyType.MODE_TOGGLE,
        KeyType.EMOJI, KeyType.SPACE -> {
            if (isDark) Color(0xFF404040).copy(alpha)
            else Color(0xFFE0E0E0).copy(alpha)
        }
        KeyType.BACKSPACE, KeyType.ENTER -> {
            if (isDark) Color(0xFF535353).copy(alpha)
            else Color(0xFFD0D0D0).copy(alpha)
        }
        else -> {
            if (isDark) Color(0xFF303030).copy(alpha)
            else Color(0xFFFFFFFF).copy(alpha)
        }
    }
}

private fun keyBorderColor(settings: KeyboardSettings): Color =
    if (settings.isDarkTheme) Color(0xFF505050) else Color(0xFFC0C0C0)

private fun keyTextColor(key: KeySpec, settings: KeyboardSettings): Color =
    if (settings.isDarkTheme) Color(0xFFFAFAFA) else Color(0xFF212121)

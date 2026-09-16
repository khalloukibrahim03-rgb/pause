@file:Suppress("unused")

package com.pause.keyboard.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pause.shared.KeySpec
import com.pause.shared.KeyboardSettings

@Composable
fun LongPressPopup(
    key: KeySpec,
    settings: KeyboardSettings,
    anchorX: Float,
    anchorY: Float,
    onAlternateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val labels = key.longPressLabels.ifEmpty { return }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .offset { IntOffset(anchorX.toInt(), anchorY.toInt() - 120) }
            .clip(RoundedCornerShape(12.dp))
            .background(if (settings.isDarkTheme) Color(0xFF424242) else Color(0xFFE0E0E0))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = { onDismiss() }
                )
            }
            .padding(8.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.align(Alignment.Center)
        ) {
            labels.forEach { label ->
                Text(
                    text = label,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (settings.isDarkTheme) Color(0xFFFAFAFA) else Color(0xFF212121),
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .clickable { onAlternateSelected(label) }
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
fun Modifier.longPressGesture(
    onLongPress: (offsetX: Float, offsetY: Float) -> Unit
): Modifier {
    return this.pointerInput(Unit) {
        detectTapGestures(
            onLongPress = { offset ->
                onLongPress(offset.x, offset.y)
            }
        )
    }
}

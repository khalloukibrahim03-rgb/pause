package com.pause.keyboard.compose

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.DpOffset

@Composable
fun Modifier.detectLongPress(
    onLongPress: (DpOffset) -> Unit
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onLongPress = { offset ->
            onLongPress(DpOffset(offset.x, offset.y))
        }
    )
}

@Composable
fun Modifier.detectTap(
    onTap: (Offset) -> Unit
): Modifier = this.pointerInput(Unit) {
    detectTapGestures(
        onPress = { offset ->
            onTap(Offset(offset.x, offset.y))
            waitForUpOrCancellation()
        }
    )
}

data class Offset(val x: Float, val y: Float)

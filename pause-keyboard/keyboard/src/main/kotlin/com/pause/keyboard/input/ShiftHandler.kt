package com.pause.keyboard.input

import com.pause.shared.ShiftState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class ShiftHandler @Inject constructor() {

    private val _shiftState = MutableStateFlow(ShiftState.Lower)
    val shiftState: StateFlow<ShiftState> = _shiftState

    private var lastShiftPressTime: Long = 0L

    fun handleShiftPress(timestamp: Long = System.currentTimeMillis()): ShiftState {
        val now = if (timestamp > 0L) timestamp else System.currentTimeMillis()
        val current = _shiftState.value

        val newState = when (current) {
            is ShiftState.Lower -> {
                val timeSinceLast = now - lastShiftPressTime
                if (timeSinceLast <= doubleTapWindowMs) {
                    ShiftState.CapsLock
                } else {
                    ShiftState.Upper
                }
            }
            is ShiftState.Upper -> {
                val timeSinceLast = now - lastShiftPressTime
                if (timeSinceLast <= doubleTapWindowMs) {
                    ShiftState.CapsLock
                } else {
                    ShiftState.Lower
                }
            }
            is ShiftState.CapsLock -> ShiftState.Lower
        }

        lastShiftPressTime = now
        _shiftState.value = newState
        return newState
    }

    fun resetAfterKeyPress() {
        if (_shiftState.value is ShiftState.Upper) {
            _shiftState.value = ShiftState.Lower
        }
    }

    fun reset() {
        _shiftState.value = ShiftState.Lower
        lastShiftPressTime = 0L
    }

    companion object {
        private const val doubleTapWindowMs: Long = 300L
    }
}

package com.pause.shared

/**
 * Tracks the shift / case state across the keyboard.
 */
sealed interface ShiftState {
    object Lower : ShiftState
    object Upper : ShiftState
    object CapsLock : ShiftState

    companion object {
        fun next(current: ShiftState): ShiftState = when (current) {
            Lower -> Upper
            Upper -> Lower
            CapsLock -> Lower
        }

        fun toggleCaps(current: ShiftState): ShiftState = when (current) {
            Lower, Upper -> CapsLock
            CapsLock -> Lower
        }

        fun isUpperCase(state: ShiftState): Boolean = state is Upper || state is CapsLock
    }
}

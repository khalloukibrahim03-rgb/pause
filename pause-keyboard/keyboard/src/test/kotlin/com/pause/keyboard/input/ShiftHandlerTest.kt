package com.pause.keyboard.input

import com.pause.shared.ShiftState
import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ShiftHandlerTest {

    private lateinit var handler: ShiftHandler

    @BeforeEach
    fun setUp() {
        handler = ShiftHandler()
    }

    @Test
    fun `single tap from Lower transitions to Upper`() {
        val result = handler.handleShiftPress(timestamp = 1000L)
        assertThat(result).isInstanceOf(ShiftState.Upper::class.java)
    }

    @Test
    fun `tap from Upper transitions back to Lower`() {
        handler.handleShiftPress(timestamp = 1000L) // Lower → Upper
        val result = handler.handleShiftPress(timestamp = 2000L) // Upper → Lower
        assertThat(result).isInstanceOf(ShiftState.Lower::class.java)
    }

    @Test
    fun `double tap within window transitions to CapsLock`() {
        handler.handleShiftPress(timestamp = 1000L) // Lower → Upper
        val result = handler.handleShiftPress(timestamp = 1200L) // Upper → CapsLock (within 300ms)
        assertThat(result).isInstanceOf(ShiftState.CapsLock::class.java)
    }

    @Test
    fun `double tap outside window does not trigger CapsLock`() {
        handler.handleShiftPress(timestamp = 1000L) // Lower → Upper
        val result = handler.handleShiftPress(timestamp = 2000L) // Upper → Lower (after 300ms window)
        assertThat(result).isInstanceOf(ShiftState.Lower::class.java)
    }

    @Test
    fun `reset clears state to Lower`() {
        handler.handleShiftPress(timestamp = 1000L)
        handler.reset()
        assertThat(handler.shiftState.value).isInstanceOf(ShiftState.Lower::class.java)
    }

    @Test
    fun `resetAfterKeyPress reverts Upper to Lower`() {
        handler.handleShiftPress(timestamp = 1000L) // → Upper
        handler.resetAfterKeyPress()
        assertThat(handler.shiftState.value).isInstanceOf(ShiftState.Lower::class.java)
    }

    @Test
    fun `resetAfterKeyPress does not affect CapsLock`() {
        handler.handleShiftPress(timestamp = 1000L) // → Upper
        handler.handleShiftPress(timestamp = 1200L) // → CapsLock
        handler.resetAfterKeyPress()
        assertThat(handler.shiftState.value).isInstanceOf(ShiftState.CapsLock::class.java)
    }

    @Test
    fun `shiftState starts as Lower`() {
        assertThat(handler.shiftState.value).isInstanceOf(ShiftState.Lower::class.java)
    }

    @Test
    fun `CapsLock toggles back to Lower on tap`() {
        // Enter CapsLock
        handler.handleShiftPress(timestamp = 1000L) // Lower → Upper
        handler.handleShiftPress(timestamp = 1200L) // Upper → CapsLock
        // Tap again to exit
        handler.handleShiftPress(timestamp = 5000L) // CapsLock → Lower
        assertThat(handler.shiftState.value).isInstanceOf(ShiftState.Lower::class.java)
    }
}

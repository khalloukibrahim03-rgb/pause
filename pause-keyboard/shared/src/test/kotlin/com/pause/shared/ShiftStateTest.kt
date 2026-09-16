package com.pause.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ShiftStateTest {

    @Test
    fun `next of Lower is Upper`() {
        assertEquals(ShiftState.Upper, ShiftState.next(ShiftState.Lower))
    }

    @Test
    fun `next of Upper is Lower`() {
        assertEquals(ShiftState.Lower, ShiftState.next(ShiftState.Upper))
    }

    @Test
    fun `next of CapsLock is Lower`() {
        assertEquals(ShiftState.Lower, ShiftState.next(ShiftState.CapsLock))
    }

    @Test
    fun `toggleCaps from Lower is CapsLock`() {
        assertEquals(ShiftState.CapsLock, ShiftState.toggleCaps(ShiftState.Lower))
    }

    @Test
    fun `toggleCaps from Upper is CapsLock`() {
        assertEquals(ShiftState.CapsLock, ShiftState.toggleCaps(ShiftState.Upper))
    }

    @Test
    fun `toggleCaps from CapsLock is Lower`() {
        assertEquals(ShiftState.Lower, ShiftState.toggleCaps(ShiftState.CapsLock))
    }

    @Test
    fun `isUpperCase is true for Upper`() {
        assertTrue(ShiftState.isUpperCase(ShiftState.Upper))
    }

    @Test
    fun `isUpperCase is true for CapsLock`() {
        assertTrue(ShiftState.isUpperCase(ShiftState.CapsLock))
    }

    @Test
    fun `isUpperCase is false for Lower`() {
        assertFalse(ShiftState.isUpperCase(ShiftState.Lower))
    }
}

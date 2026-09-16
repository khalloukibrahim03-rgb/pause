package com.pause.shared

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test

class KeySpecTest {

    @Test
    fun `KeySpec has correct defaults for width ratio`() {
        val key = KeySpec(code = 65, label = "A", type = KeyType.LETTER)
        assertThat(key.widthRatio).isEqualTo(1.0f)
    }

    @Test
    fun `KeySpec stores label and code correctly`() {
        val key = KeySpec(code = 66, label = "B", type = KeyType.LETTER)
        assertThat(key.label).isEqualTo("B")
        assertThat(key.code).isEqualTo(66)
    }

    @Test
    fun `KeySpec handles long press alternates`() {
        val alternates = listOf("é", "è", "ê")
        val key = KeySpec(
            code = 101,
            label = "e",
            type = KeyType.LETTER,
            longPressLabels = alternates
        )
        assertThat(key.longPressLabels).containsExactly("é", "è", "ê")
    }

    @Test
    fun `KeySpec requiresShift defaults to false`() {
        val key = KeySpec(code = 65, label = "A", type = KeyType.LETTER)
        assertThat(key.requiresShift).isFalse()
    }

    @Test
    fun `KeySpec capsLockAffect defaults to false`() {
        val key = KeySpec(code = 65, label = "A", type = KeyType.LETTER)
        assertThat(key.capsLockAffect).isFalse()
    }
}

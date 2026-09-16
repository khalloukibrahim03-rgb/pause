package com.pause.shared

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class LanguageTest {

    @Test
    fun `fromCode returns ENGLISH for en`() {
        assertEquals(Language.ENGLISH, Language.fromCode("en"))
    }

    @Test
    fun `fromCode returns ARABIC for ar`() {
        assertEquals(Language.ARABIC, Language.fromCode("ar"))
    }

    @Test
    fun `fromCode returns ENGLISH for unknown code`() {
        assertEquals(Language.ENGLISH, Language.fromCode("xx"))
    }

    @Test
    fun `ENGLISH is not RTL`() {
        assertEquals(false, Language.ENGLISH.isRtl)
    }

    @Test
    fun `ARABIC is RTL`() {
        assertEquals(true, Language.ARABIC.isRtl)
    }

    @Test
    fun `next of ENGLISH is ARABIC`() {
        assertEquals(Language.ARABIC, Language.next(Language.ENGLISH))
    }

    @Test
    fun `next of ARABIC is ENGLISH`() {
        assertEquals(Language.ENGLISH, Language.next(Language.ARABIC))
    }
}

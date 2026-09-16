package com.pause.keyboard.layout

import com.pause.shared.KeyCodes
import com.pause.shared.KeyType
import com.pause.shared.KeyboardMode
import com.pause.shared.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EnglishKeyboardProviderTest {

    @Test
    fun alphaLayoutHasFourRows() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.ALPHA)
        assertEquals(4, def.rows.size)
    }

    @Test
    fun firstRowStartsWithQ() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.ALPHA)
        val firstKey = def.rows[0].first()
        assertEquals("Q", firstKey.label)
        assertEquals(KeyType.LETTER, firstKey.type)
    }

    @Test
    fun thirdRowHasShiftAndBackspace() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.ALPHA)
        val thirdRow = def.rows[2]
        val hasShift = thirdRow.any { it.code == KeyCodes.SHIFT }
        val hasBackspace = thirdRow.any { it.code == KeyCodes.BACKSPACE }
        assertTrue(hasShift, "Row 3 should have shift key")
        assertTrue(hasBackspace, "Row 3 should have backspace key")
    }

    @Test
    fun bottomRowHasSpaceLanguageArrowsEmoji() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.ALPHA)
        val bottomRow = def.rows[3]
        assertTrue(bottomRow.any { it.code == KeyCodes.SPACE })
        assertTrue(bottomRow.any { it.code == KeyCodes.LANGUAGE_SWITCH })
        assertTrue(bottomRow.any { it.code == KeyCodes.ARROW_LEFT })
        assertTrue(bottomRow.any { it.code == KeyCodes.ARROW_RIGHT })
        assertTrue(bottomRow.any { it.code == KeyCodes.EMOJI })
    }

    @Test
    fun symbolsLayoutHasBackspace() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.SYMBOLS)
        val hasBackspace = def.rows.any { row ->
            row.any { it.code == KeyCodes.BACKSPACE }
        }
        assertTrue(hasBackspace)
    }

    @Test
    fun numbersLayoutHasDigits0Through9() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.NUMBERS)
        val allKeys = def.rows.flatten()
        val digits = setOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
        val foundDigits = allKeys.map { it.label }.filter { it in digits }.toSet()
        assertEquals(10, foundDigits.size)
    }

    @Test
    fun languageIsEnglish() {
        val def = EnglishKeyboardProvider.provide()
        assertEquals(Language.ENGLISH, def.language)
    }

    @Test
    fun requiresShiftFlagIsSetForLetters() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.ALPHA)
        val firstKey = def.rows[0].first()
        assertTrue(firstKey.requiresShift)
    }

    @Test
    fun longPressAlternatesAvailableForVowels() {
        val def = EnglishKeyboardProvider.provide(KeyboardMode.ALPHA)
        val allKeys = def.rows.flatten()
        val eKey = allKeys.find { it.label == "E" }
        assertNotNull(eKey)
        assertTrue(eKey!!.longPressLabels.isNotEmpty(), "E should have long-press alternates")
        assertEquals(listOf("é", "è", "ê", "ë"), eKey.longPressLabels)
    }
}

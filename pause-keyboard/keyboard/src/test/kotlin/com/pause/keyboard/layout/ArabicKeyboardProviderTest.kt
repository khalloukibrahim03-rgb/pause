package com.pause.keyboard.layout

import com.pause.shared.KeyCodes
import com.pause.shared.KeyboardMode
import com.pause.shared.Language
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArabicKeyboardProviderTest {

    @Test
    fun alphaLayoutHasFiveRows() {
        val def = ArabicKeyboardProvider.provide(KeyboardMode.ALPHA)
        assertEquals(5, def.rows.size)
    }

    @Test
    fun languageIsArabic() {
        val def = ArabicKeyboardProvider.provide()
        assertEquals(Language.ARABIC, def.language)
    }

    @Test
    fun firstRowContainsArabicLetters() {
        val def = ArabicKeyboardProvider.provide(KeyboardMode.ALPHA)
        val firstRowLabels = def.rows[0].map { it.label }
        assertTrue(firstRowLabels.contains("ض"), "Row 1 should contain ض")
        assertTrue(firstRowLabels.contains("ص"), "Row 1 should contain ص")
        assertTrue(firstRowLabels.contains("ق"), "Row 1 should contain ق")
    }

    @Test
    fun shiftAndBackspaceInThirdRow() {
        val def = ArabicKeyboardProvider.provide(KeyboardMode.ALPHA)
        val thirdRow = def.rows[2]
        val hasShift = thirdRow.any { it.code == KeyCodes.SHIFT }
        val hasBackspace = thirdRow.any { it.code == KeyCodes.BACKSPACE }
        assertTrue(hasShift, "Third row should have shift")
        assertTrue(hasBackspace, "Third row should have backspace")
    }

    @Test
    fun bottomRowHasSpaceAndLanguage() {
        val def = ArabicKeyboardProvider.provide(KeyboardMode.ALPHA)
        val bottomRow = def.rows[3]
        assertTrue(bottomRow.any { it.code == KeyCodes.SPACE })
        assertTrue(bottomRow.any { it.code == KeyCodes.LANGUAGE_SWITCH })
    }

    @Test
    fun arabicLettersDontRequireShift() {
        val def = ArabicKeyboardProvider.provide(KeyboardMode.ALPHA)
        val allKeys = def.rows.flatten()
        val letterKeys = allKeys.filter { it.label.isNotEmpty() && it.label[0].isLetter() }
        letterKeys.forEach {
            assertTrue(!it.requiresShift, "Arabic letters should not require shift")
        }
    }

    @Test
    fun symbolsLayoutHasBackspace() {
        val def = ArabicKeyboardProvider.provide(KeyboardMode.SYMBOLS)
        val hasBackspace = def.rows.any { row ->
            row.any { it.code == KeyCodes.BACKSPACE }
        }
        assertTrue(hasBackspace)
    }
}

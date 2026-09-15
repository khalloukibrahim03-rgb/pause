package com.pause.keyboard.layout

import com.pause.shared.KeyCodes
import com.pause.shared.KeySpec
import com.pause.shared.KeyType
import com.pause.shared.KeyboardDefinition
import com.pause.shared.KeyboardMode
import com.pause.shared.Language

/**
 * Provides the Arabic keyboard layout definition.
 *
 * Arabic is a right-to-left (RTL) script. The key order in each row
 * is defined left-to-right in the source data, but [KeyboardLayout]
 * automatically reverses the visual order when `language.isRtl` is true.
 *
 * Layout (visual, RTL):
 * Row 1: بر لماخص ؟
 * Row 2: ش ض ص ق ف غ ع ه خ ح
 * Row 3: غلا ميم ن ه ت ب ...
 * (Standard Arabic PC/Phone keyboard layout)
 */
object ArabicKeyboardProvider {

    private val arabicVowelAlternates = mapOf(
        "ا" to listOf("أ", "إ", "آ"),
        "و" to listOf("ؤ", "ء"),
        "ي" to listOf("ئ", "ى"),
        "ه" to listOf("ة"),
        "س" to listOf("ش"),
        "ن" to listOf("ں"),
    )

    fun provide(mode: KeyboardMode = KeyboardMode.ALPHA): KeyboardDefinition {
        return when (mode) {
            KeyboardMode.ALPHA -> provideAlpha()
            KeyboardMode.SYMBOLS -> provideSymbols()
            KeyboardMode.NUMBERS -> provideNumbers()
        }
    }

    private fun provideAlpha(): KeyboardDefinition {
        // Arabic letters defined in LTR source order — visual reversal
        // handled by KeyboardLayout when language.isRtl == true
        val row1 = listOf(
            arabicLetter("ض"), arabicLetter("ص"), arabicLetter("ق"),
            arabicLetter("ف"), arabicLetter("غ"), arabicLetter("ع"),
            arabicLetter("ه"), arabicLetter("خ"), arabicLetter("ح"),
            arabicLetter("ج")
        )

        val row2 = listOf(
            arabicLetter("ش"), arabicLetter("س"), arabicLetter("ي"),
            arabicLetter("ب"), arabicLetter("ل"), arabicLetter("م"),
            arabicLetter("ن"), arabicLetter("ت"), arabicLetter("ا")
        )

        val row3 = listOf(
            KeySpec(KeyCodes.SHIFT, "", KeyType.SHIFT, widthRatio = 1.2f),
            arabicLetter("ئ"), arabicLetter("ء"), arabicLetter("ؤ"),
            arabicLetter("ر"), arabicLetter("ل"), arabicLetter("ى"),
            arabicLetter("ة"), arabicLetter("و"), arabicLetter("ز"),
            KeySpec(KeyCodes.BACKSPACE, "⌫", KeyType.BACKSPACE, widthRatio = 1.2f)
        )

        val row4 = listOf(
            KeySpec(KeyCodes.LANGUAGE_SWITCH, "🌐", KeyType.LANGUAGE, widthRatio = 1.5f),
            KeySpec(KeyCodes.SPACE, " ", KeyType.SPACE, widthRatio = 5f),
            KeySpec(KeyCodes.ARROW_LEFT, "←", KeyType.ARROW_LEFT, widthRatio = 1.0f),
            KeySpec(KeyCodes.ARROW_RIGHT, "→", KeyType.ARROW_RIGHT, widthRatio = 1.0f),
            KeySpec(KeyCodes.EMOJI, "😀", KeyType.EMOJI, widthRatio = 1.0f)
        )

        return KeyboardDefinition(
            name = "Arabic",
            language = Language.ARABIC,
            mode = KeyboardMode.ALPHA,
            rows = listOf(row1, row2, row3, row4),
            edgePaddingRatio = 0.02f
        )
    }

    private fun provideSymbols(): KeyboardDefinition {
        val row1 = listOf(
            symbolKey("1"), symbolKey("2"), symbolKey("3"), symbolKey("4"),
            symbolKey("5"), symbolKey("6"), symbolKey("7"),
            symbolKey("8"), symbolKey("9"), symbolKey("0"),
            KeySpec(KeyCodes.BACKSPACE, "⌫", KeyType.BACKSPACE, widthRatio = 1.2f)
        )

        val row2 = listOf(
            symbolKey("."), symbolKey(","), symbolKey("!"), symbolKey("?"),
            symbolKey("("), symbolKey(")"), symbolKey("["),
            symbolKey("]"), symbolKey("{"), symbolKey("}")
        )

        val row3 = listOf(
            symbolKey("@"), symbolKey("#"), symbolKey("$"),
            symbolKey("%"), symbolKey("^"), symbolKey("&"),
            symbolKey("*"), symbolKey("+")
        )

        val row4 = listOf(
            KeySpec(KeyCodes.MODE_TOGGLE, "⇀", KeyType.MODE_TOGGLE, widthRatio = 1.0f)
        )

        val row5 = listOf(
            KeySpec(KeyCodes.LANGUAGE_SWITCH, "🌐", KeyType.LANGUAGE, widthRatio = 1.5f),
            KeySpec(KeyCodes.SPACE, " ", KeyType.SPACE, widthRatio = 6f),
            KeySpec(KeyCodes.ARROW_LEFT, "←", KeyType.ARROW_LEFT),
            KeySpec(KeyCodes.ARROW_RIGHT, "→", KeyType.ARROW_RIGHT),
            KeySpec(KeyCodes.EMOJI, "😀", KeyType.EMOJI)
        )

        return KeyboardDefinition(
            name = "Arabic Symbols",
            language = Language.ARABIC,
            mode = KeyboardMode.SYMBOLS,
            rows = listOf(row1, row2, row3, row4, row5)
        )
    }

    private fun provideNumbers(): KeyboardDefinition {
        val row1 = listOf(
            digitKey("1"), digitKey("2"), digitKey("3"), digitKey("4"),
            digitKey("5"), digitKey("6"), digitKey("7"),
            digitKey("8"), digitKey("9"), digitKey("0"),
            KeySpec(KeyCodes.BACKSPACE, "⌫", KeyType.BACKSPACE, widthRatio = 1.2f)
        )

        val row2 = listOf(
            symbolKey("-"), symbolKey("."), symbolKey(","),
            symbolKey("("), symbolKey(")"), symbolKey("@"),
            symbolKey("#"), symbolKey("$"), symbolKey("+")
        )

        val row3 = listOf(
            KeySpec(KeyCodes.MODE_TOGGLE, "⇀", KeyType.MODE_TOGGLE, widthRatio = 1.0f)
        )

        val row4 = listOf(
            KeySpec(KeyCodes.LANGUAGE_SWITCH, "🌐", KeyType.LANGUAGE, widthRatio = 1.5f),
            KeySpec(KeyCodes.SPACE, " ", KeyType.SPACE, widthRatio = 6f),
            KeySpec(KeyCodes.ARROW_LEFT, "←", KeyType.ARROW_LEFT),
            KeySpec(KeyCodes.ARROW_RIGHT, "→", KeyType.ARROW_RIGHT),
            KeySpec(KeyCodes.EMOJI, "😀", KeyType.EMOJI)
        )

        return KeyboardDefinition(
            name = "Arabic Numbers",
            language = Language.ARABIC,
            mode = KeyboardMode.NUMBERS,
            rows = listOf(row1, row2, row3, row4)
        )
    }

    private fun arabicLetter(label: String): KeySpec {
        val alternates = arabicVowelAlternates[label] ?: emptyList()
        val codes = alternates.map { it.codePoints().toArray()[0] }
        return KeySpec(
            code = if (label.isNotEmpty()) label[0].code else 0,
            label = label,
            type = KeyType.LETTER,
            longPressCodes = codes,
            longPressLabels = alternates,
            requiresShift = false, // Arabic doesn't use shift for case
            capsLockAffect = false,
            rtlAdjust = 0f
        )
    }

    private fun symbolKey(label: String): KeySpec {
        val code = if (label.length == 1) label[0].code else label.hashCode()
        return KeySpec(code = code, label = label, type = KeyType.SYSTEM)
    }

    private fun digitKey(label: String): KeySpec {
        return KeySpec(
            code = label[0].code,
            label = label,
            type = KeyType.SYSTEM
        )
    }
}

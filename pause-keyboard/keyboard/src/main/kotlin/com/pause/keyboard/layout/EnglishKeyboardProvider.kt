package com.pause.keyboard.layout

import com.pause.shared.KeyCodes
import com.pause.shared.KeySpec
import com.pause.shared.KeyType
import com.pause.shared.KeyboardDefinition
import com.pause.shared.KeyboardMode
import com.pause.shared.Language

/**
 * Provides the English QWERTY keyboard layout definition.
 *
 * Layout:
 * Row 1: Q W E R T Y U I O P
 * Row 2: A S D F G H J K L
 * Row 3: ⇧ Z X C V B N M ⌫
 * Row 4: 🌐 Space ⏎
 */
object EnglishKeyboardProvider {

    private val vowelAlternates = mapOf(
        "e" to listOf("é", "è", "ê", "ë"),
        "a" to listOf("á", "à", "â", "ä"),
        "i" to listOf("í", "ì", "î", "ï"),
        "o" to listOf("ó", "ò", "ô", "ö"),
        "u" to listOf("ú", "ù", "û", "ü"),
        "n" to listOf("ñ"),
        "c" to listOf("ç"),
        "y" to listOf("ý", "ÿ"),
        "," to listOf("„", "«", "“", "„"),
        "." to listOf("…", "•", "”", "”"),
    )

    fun provide(mode: KeyboardMode = KeyboardMode.ALPHA): KeyboardDefinition {
        return when (mode) {
            KeyboardMode.ALPHA -> provideAlpha()
            KeyboardMode.SYMBOLS -> provideSymbols()
            KeyboardMode.NUMBERS -> provideNumbers()
        }
    }

    private fun provideAlpha(): KeyboardDefinition {
        val qwertyRow = listOf(
            letterKey("Q"), letterKey("W"), letterKey("E", alternates = vowelAlternates["e"]!!),
            letterKey("R"), letterKey("T"), letterKey("Y"), letterKey("U"),
            letterKey("I"), letterKey("O"), letterKey("P")
        )

        val asdfRow = listOf(
            letterKey("A"), letterKey("S"), letterKey("D", alternates = vowelAlternates["a"]!!),
            letterKey("F"), letterKey("G"), letterKey("H"), letterKey("J"),
            letterKey("K", alternates = vowelAlternates["i"]!!), letterKey("L")
        )

        val zxcRow = listOf(
            KeySpec(KeyCodes.SHIFT, "", KeyType.SHIFT, widthRatio = 1.2f),
            letterKey("Z"), letterKey("X"), letterKey("C"), letterKey("V"),
            letterKey("B"), letterKey("N"), letterKey("M"),
            KeySpec(KeyCodes.BACKSPACE, "⌫", KeyType.BACKSPACE, widthRatio = 1.2f)
        )

        val bottomRow = listOf(
            KeySpec(KeyCodes.LANGUAGE_SWITCH, "🌐", KeyType.LANGUAGE, widthRatio = 1.5f),
            KeySpec(KeyCodes.SPACE, " ", KeyType.SPACE, widthRatio = 5f),
            KeySpec(KeyCodes.ARROW_LEFT, "←", KeyType.ARROW_LEFT, widthRatio = 1.0f),
            KeySpec(KeyCodes.ARROW_RIGHT, "→", KeyType.ARROW_RIGHT, widthRatio = 1.0f),
            KeySpec(KeyCodes.EMOJI, "😀", KeyType.EMOJI, widthRatio = 1.0f)
        )

        return KeyboardDefinition(
            name = "English QWERTY",
            language = Language.ENGLISH,
            mode = KeyboardMode.ALPHA,
            rows = listOf(qwertyRow, asdfRow, zxcRow, bottomRow)
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
            symbolKey("-"), symbolKey("="), symbolKey("+"), symbolKey("."),
            symbolKey(","), symbolKey("!"), symbolKey("?"),
            symbolKey("("), symbolKey(")"), symbolKey("["),
            symbolKey("]")
        )

        val row3 = listOf(
            symbolKey("@"), symbolKey("#"), symbolKey("$"), symbolKey("%"),
            symbolKey("^"), symbolKey("&"), symbolKey("*"),
            symbolKey("{"), symbolKey("}"), symbolKey("\\"),
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
            name = "Symbols",
            language = Language.ENGLISH,
            mode = KeyboardMode.SYMBOLS,
            rows = listOf(row1, row2, row3, row4)
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
            name = "Numbers",
            language = Language.ENGLISH,
            mode = KeyboardMode.NUMBERS,
            rows = listOf(row1, row2, row3, row4)
        )
    }

    private fun letterKey(
        label: String,
        alternates: List<String> = emptyList()
    ): KeySpec {
        val codes = alternates.map { it.codePoints().toArray()[0] }
        return KeySpec(
            code = label[0].code,
            label = label,
            type = KeyType.LETTER,
            longPressCodes = codes,
            longPressLabels = alternates,
            requiresShift = true,
            capsLockAffect = true
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

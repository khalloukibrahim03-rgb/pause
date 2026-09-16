package com.pause.shared

/**
 * Supported input languages.
 */
enum class Language(
    val code: String,
    val isRtl: Boolean
) {
    ENGLISH("en", false),
    ARABIC("ar", true);

    companion object {
        fun fromCode(code: String): Language {
            return entries.firstOrNull { it.code == code } ?: ENGLISH
        }

        fun next(current: Language): Language {
            return when (current) {
                ENGLISH -> ARABIC
                ARABIC -> ENGLISH
            }
        }
    }
}

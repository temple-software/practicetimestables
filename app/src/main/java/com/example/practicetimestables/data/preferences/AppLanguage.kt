package com.example.practicetimestables.data.preferences

import java.util.Locale

enum class AppLanguage(val languageTag: String) {
    ENGLISH_UK("en-GB"),
    FRENCH("fr");

    companion object {
        fun fromLanguageTag(tag: String?): AppLanguage? =
            entries.firstOrNull { it.languageTag.equals(tag, ignoreCase = true) }

        fun fromSystem(locale: Locale = Locale.getDefault()): AppLanguage =
            if (locale.language == Locale.FRENCH.language) FRENCH else ENGLISH_UK
    }
}

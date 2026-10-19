package com.amq.bulbulwords.data

// English-mode letters and numbers. In English the app shows A-Z and 0-10 (spoken by the English
// voice); in Arabic it shows the Arabic letters and Arabic-Indic digits (WordsE.kt).

private val EN_NUMBER_NAMES = listOf("Zero", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten")
private val AR_NUMBER_NAMES = listOf("صفر", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة", "سبعة", "ثمانية", "تسعة", "عشرة")

val WORDS_NUMBERS_EN: List<Word> = EN_NUMBER_NAMES.mapIndexed { i, name ->
    Word("nm_en_$i", Category.NUMBERS, "txt:$i", AR_NUMBER_NAMES[i], name)
}

val WORDS_LETTERS_EN: List<Word> = ('A'..'Z').map { c ->
    Word("lt_en_$c", Category.LETTERS, "txt:$c", "حرف $c", "$c")
}

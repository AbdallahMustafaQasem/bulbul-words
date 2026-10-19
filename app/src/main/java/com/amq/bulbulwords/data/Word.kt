package com.amq.bulbulwords.data

import androidx.compose.ui.graphics.Color

enum class Lang(val label: String, val tag: String) {
    FUSHA("العربية", "ar"),
    EN("English", "en-US"),
}

enum class Category(val id: String, val ar: String, val en: String, val emoji: String, val color: Color) {
    FRUITS("fruits", "الفواكه", "Fruits", "🍎", Color(0xFFFF6B6B)),
    VEGETABLES("vegetables", "الخضروات", "Vegetables", "🥕", Color(0xFF2EC4B6)),
    FOOD("food", "الطعام", "Food", "🍕", Color(0xFFFF9F1C)),
    CLOTHES("clothes", "الملابس", "Clothes", "👕", Color(0xFF9B5DE5)),
    ANIMALS("animals", "الحيوانات", "Animals", "🦁", Color(0xFFF15BB5)),
    BIRDS("birds", "الطيور", "Birds", "🦜", Color(0xFF00BBF9)),
    BODY("body", "أجزاء الجسم", "Body", "👁️", Color(0xFFFEE440)),
    HOME("home", "مكونات المنزل", "Home", "🛋️", Color(0xFF8AC926)),
    TRANSPORT("transport", "وسائل المواصلات", "Transport", "🚗", Color(0xFF4361EE)),
    FAMILY("family", "العائلة", "Family", "👨‍👩‍👧", Color(0xFFEF476F)),
    PLACES("places", "الأماكن", "Places", "🏫", Color(0xFF118AB2)),
    ACTIONS("actions", "الأفعال", "Actions", "🏃", Color(0xFF06D6A0)),
    FEELINGS("feelings", "المشاعر", "Feelings", "😊", Color(0xFFF4A300)),
    NUMBERS("numbers", "الأرقام", "Numbers", "🔢", Color(0xFF7B2CBF)),
    LETTERS("letters", "الحروف", "Letters", "🔤", Color(0xFFFB8500)),
}

/**
 * One vocabulary item. [ar] = Arabic, [en] = English.
 * [sound] = Arabic onomatopoeia spoken for the sound games (animals/birds/vehicles only), else null.
 */
data class Word(
    val id: String,
    val cat: Category,
    val emoji: String,
    val ar: String,
    val en: String,
    val sound: String? = null,
) {
    fun text(lang: Lang) = when (lang) {
        Lang.FUSHA -> ar
        Lang.EN -> en
    }
}

object WordRepo {
    private val common by lazy {
        WORDS_FRUITS + WORDS_VEGETABLES + WORDS_FOOD + WORDS_CLOTHES + WORDS_ANIMALS +
            WORDS_BIRDS + WORDS_BODY + WORDS_HOME + WORDS_HOME_EXTRA + WORDS_TRANSPORT +
            WORDS_FAMILY + WORDS_PLACES + WORDS_ACTIONS + WORDS_FEELINGS
    }
    private val arabicSet by lazy { common + WORDS_NUMBERS + WORDS_LETTERS }
    private val englishSet by lazy { common + WORDS_NUMBERS_EN + WORDS_LETTERS_EN }

    /** All words for the currently selected language (letters/numbers differ per language). */
    val all: List<Word> get() = if (Settings.lang == Lang.EN) englishSet else arabicSet

    fun of(cat: Category?) =
        if (cat == null) all.filter { it.cat != Category.LETTERS && it.cat != Category.NUMBERS }
        else all.filter { it.cat == cat }
}

/** Home-made vector drawing used when a word has no suitable emoji. */
typealias DrawFn = androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit

/** Registry of all hand-drawn pictures, keyed by the name after "draw:" in [Word.emoji]. */
val ALL_DRAWINGS: Map<String, DrawFn> by lazy { DRAWINGS_D + DRAWINGS_E }

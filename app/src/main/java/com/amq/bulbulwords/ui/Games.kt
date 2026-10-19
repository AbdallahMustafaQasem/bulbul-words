package com.amq.bulbulwords.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.amq.bulbulwords.games.BalloonsGame
import com.amq.bulbulwords.games.ChooseGame
import com.amq.bulbulwords.games.ColorsGame
import com.amq.bulbulwords.games.CountGame
import com.amq.bulbulwords.games.DressFeedGame
import com.amq.bulbulwords.games.FeelingsGame
import com.amq.bulbulwords.games.FindGame
import com.amq.bulbulwords.games.FunctionGame
import com.amq.bulbulwords.games.GameHost
import com.amq.bulbulwords.games.GiveMeGame
import com.amq.bulbulwords.games.MemoryGame
import com.amq.bulbulwords.games.OddOneOutGame
import com.amq.bulbulwords.games.PuzzleGame
import com.amq.bulbulwords.games.SentenceGame
import com.amq.bulbulwords.games.SequenceGame
import com.amq.bulbulwords.games.ShadowGame
import com.amq.bulbulwords.games.ShapesGame
import com.amq.bulbulwords.games.SortGame
import com.amq.bulbulwords.games.SoundsGame
import com.amq.bulbulwords.games.SpeakGame
import com.amq.bulbulwords.games.WhereGame

class GameDef(
    val id: String,
    val title: String,
    val emoji: String,
    val color: Color,
    val content: @Composable (GameHost) -> Unit,
)

val GAMES = listOf(
    GameDef("choose", "اختر الصورة", "👆", Color(0xFFFF6B6B)) { ChooseGame(it) },
    GameDef("memory", "لعبة الذاكرة", "🧠", Color(0xFF9B5DE5)) { MemoryGame(it) },
    GameDef("odd", "من المختلف؟", "🧐", Color(0xFFFF9F1C)) { OddOneOutGame(it) },
    GameDef("shapes", "الأشكال", "🔺", Color(0xFF2EC4B6)) { ShapesGame(it) },
    GameDef("puzzle", "البازل", "🧩", Color(0xFF4361EE)) { PuzzleGame(it) },
    GameDef("colors", "الألوان", "🎨", Color(0xFFF15BB5)) { ColorsGame(it) },
    GameDef("sounds", "أصوات", "🔊", Color(0xFF00BBF9)) { SoundsGame(it) },
    GameDef("balloons", "البالونات", "🎈", Color(0xFFE63946)) { BalloonsGame(it) },
    GameDef("count", "العدّ", "🔢", Color(0xFF8AC926)) { CountGame(it) },
    GameDef("speak", "قل الكلمة", "🗣️", Color(0xFFFFB703)) { SpeakGame(it) },
    GameDef("sort", "رتّب", "🧺", Color(0xFF06D6A0)) { SortGame(it) },
    GameDef("shadow", "الظل", "👤", Color(0xFF7209B7)) { ShadowGame(it) },
    GameDef("dressfeed", "ألبس وأطعم", "🧒", Color(0xFFFF7B54)) { DressFeedGame(it) },
    GameDef("sequence", "ماذا بعد؟", "🔢", Color(0xFF3D8361)) { SequenceGame(it) },
    GameDef("where", "أين؟", "📦", Color(0xFF5F6CAF)) { WhereGame(it) },
    GameDef("sentence", "جملتي", "💬", Color(0xFF00A6A6)) { SentenceGame(it) },
    GameDef("function", "ما الوظيفة؟", "🥄", Color(0xFFD9534F)) { FunctionGame(it) },
    GameDef("feelings", "كيف يشعر؟", "😊", Color(0xFFF4A300)) { FeelingsGame(it) },
    GameDef("giveme", "أعطني…", "🤲", Color(0xFF8E6CEF)) { GiveMeGame(it) },
    GameDef("find", "ابحث عن…", "🔍", Color(0xFF2A9D8F)) { FindGame(it) },
)

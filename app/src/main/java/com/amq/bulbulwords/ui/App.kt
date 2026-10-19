package com.amq.bulbulwords.ui

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amq.bulbulwords.data.Category
import com.amq.bulbulwords.data.Lang
import com.amq.bulbulwords.data.ParentLock
import com.amq.bulbulwords.data.Progress
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Speaker
import com.amq.bulbulwords.data.Word
import com.amq.bulbulwords.data.Tips
import com.amq.bulbulwords.data.WordRepo
import com.amq.bulbulwords.games.EmojiBubble
import com.amq.bulbulwords.games.Pic
import com.amq.bulbulwords.games.GameHost
import com.amq.bulbulwords.games.Ink
import com.amq.bulbulwords.games.KidBg
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

private sealed interface Screen {
    data object Tabs : Screen
    data class Learn(val cat: Category) : Screen
    data class Play(val game: GameDef, val cat: Category?) : Screen
    data class Result(val game: GameDef, val cat: Category?, val score: Int, val total: Int) : Screen
}

@Composable
fun App(speaker: Speaker) {
    var screen by remember { mutableStateOf<Screen>(Screen.Tabs) }
    var tab by remember { mutableIntStateOf(0) }
    var seconds by remember { mutableIntStateOf(0) }
    var locked by remember { mutableStateOf(false) }

    androidx.activity.compose.BackHandler(enabled = screen !is Screen.Tabs) { screen = Screen.Tabs }

    // Play-time limit: counts time while the app is on screen; a parent can unlock another period.
    val lifecycle = androidx.lifecycle.compose.LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(locked, Settings.playMinutes) {
        if (locked) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(1000)
            if (lifecycle.currentState.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED)) {
                seconds++
                val limit = Settings.playMinutes
                if (limit > 0 && seconds >= limit * 60) { locked = true; speaker.stop(); break }
            }
        }
    }
    if (locked) {
        TimeUpScreen(onParentUnlock = { seconds = 0; locked = false })
        return
    }

    Box(Modifier.fillMaxSize()) {
        when (val s = screen) {
            Screen.Tabs -> TabsScreen(
                tab, { tab = it }, speaker,
                onLearn = { screen = Screen.Learn(it) },
                onPlay = { g, c -> screen = Screen.Play(g, c) },
            )
            is Screen.Learn -> LearnScreen(s.cat, speaker) { screen = Screen.Tabs }
            is Screen.Play -> {
                val host = remember(s) {
                    GameHost(
                        pool = WordRepo.of(s.cat), speaker = speaker, accent = s.game.color,
                        onBack = { screen = Screen.Tabs },
                        onFinish = { sc, tot ->
                            Progress.record(s.game.id, sc, tot)
                            Progress.addStars(if (tot == 0) 0 else (sc * 3 / tot))
                            Sfx.win(); speaker.praise()
                            screen = Screen.Result(s.game, s.cat, sc, tot)
                        },
                    )
                }
                Box(Modifier.fillMaxSize().background(KidBg), contentAlignment = Alignment.TopCenter) {
                    Box(Modifier.widthIn(max = 720.dp).fillMaxHeight()) { s.game.content(host) }
                }
            }
            is Screen.Result -> Box(Modifier.fillMaxSize().background(KidBg), contentAlignment = Alignment.TopCenter) {
                Box(Modifier.widthIn(max = 640.dp).fillMaxHeight()) {
                    ResultScreen(s, onAgain = { screen = Screen.Play(s.game, s.cat) }, onHome = { screen = Screen.Tabs })
                }
            }
        }
        // New-sticker celebration (never interrupts a running game).
        val fresh = Progress.justUnlocked
        if (fresh.isNotEmpty() && screen !is Screen.Play) {
            val ws = fresh.mapNotNull { id -> WordRepo.all.firstOrNull { it.id == id } }
            if (ws.isNotEmpty()) {
                LaunchedEffect(fresh.size) { Sfx.win(); if (ws.size == 1) speaker.say(ws[0]) else speaker.praise() }
                StickerDialog(ws) { Progress.justUnlocked = emptyList() }
            } else Progress.justUnlocked = emptyList()
        }
    }
}

// ───────────────────────── Tabs ─────────────────────────

@Composable
private fun TabsScreen(
    tab: Int, onTab: (Int) -> Unit, speaker: Speaker,
    onLearn: (Category) -> Unit, onPlay: (GameDef, Category?) -> Unit,
) {
    val tablet = isTablet()
    if (tablet) {
        Row(Modifier.fillMaxSize().background(KidBg)) {
            NavRail(tab, onTab)
            Box(Modifier.weight(1f).fillMaxHeight()) {
                when (tab) {
                    0 -> HomeTab(onLearn, speaker)
                    1 -> GamesTab(onPlay)
                    2 -> AlbumTab(speaker)
                    else -> ParentGate { ParentTablet(speaker) }
                }
            }
        }
        return
    }
    Column(Modifier.fillMaxSize().background(KidBg)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (tab) {
                0 -> HomeTab(onLearn, speaker)
                1 -> GamesTab(onPlay)
                2 -> AlbumTab(speaker)
                3 -> ParentGate { ProgressTab() }
                else -> ParentGate { SettingsTab(speaker) }
            }
        }
        Row(
            Modifier.fillMaxWidth().background(Color.White).navigationBarsPadding().padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            listOf("📚" to "تعلّم", "🎮" to "ألعاب", "🌟" to "ملصقاتي", "📊" to "تقدّمي", "⚙️" to "إعدادات").forEachIndexed { i, (e, l) ->
                val sel = tab == i
                val sc by animateFloatAsState(if (sel) 1.15f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "t")
                Column(
                    Modifier.clip(RoundedCornerShape(16.dp)).clickable { Sfx.tap(); onTab(i) }
                        .background(if (sel) Color(0xFFFFE08A) else Color.Transparent)
                        .padding(horizontal = 16.dp, vertical = 6.dp).scale(sc),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(e, fontSize = 26.sp)
                    Text(l, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
            }
        }
    }
}

@Composable
private fun HomeTab(onLearn: (Category) -> Unit, speaker: Speaker) {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    val tablet = isTablet()
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                val n = Settings.childName
                Text(
                    if (n.isBlank()) "بلبل الكلمات" else "أهلاً يا $n! 👋",
                    fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink,
                )
                Text("اختر ما تريد أن تتعلم اليوم", fontSize = 15.sp, color = Ink.copy(alpha = 0.7f))
            }
            if (tablet) {
                Box(Modifier.width(320.dp)) { DailyGoal() }
                Spacer(Modifier.width(16.dp))
            }
            Text("⭐ ${Progress.version.let { Progress.stars() }}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
        if (!tablet) {
            Spacer(Modifier.height(10.dp))
            DailyGoal()
        }
        if (!speaker.arabicOk) {
            Spacer(Modifier.height(8.dp))
            Text(
                "🔈 لا يوجد صوت عربي على جهازك. اضغط هنا لتثبيته من إعدادات النطق.",
                Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(Color(0xFFFFE3E3))
                    .clickable {
                        try { ctx.startActivity(android.content.Intent("com.android.settings.TTS_SETTINGS").addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)) }
                        catch (_: Exception) { }
                    }.padding(12.dp),
                color = Ink, fontSize = 14.sp, fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(12.dp))
        LangChips()
        Spacer(Modifier.height(12.dp))
        LazyVerticalGrid(
            GridCells.Fixed(gridCols(170, 2, 5)),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(Category.values().toList()) { c ->
                val words = WordRepo.of(c)
                val learned = Progress.learned()
                val done = words.count { it.id in learned }
                Column(
                    Modifier.fillMaxWidth().height(170.dp).clip(RoundedCornerShape(28.dp))
                        .background(Brush.linearGradient(listOf(c.color, c.color.copy(alpha = 0.65f))))
                        .clickable { Sfx.tap(); onLearn(c) }.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    // mini collage of three words from the category
                    Row(horizontalArrangement = Arrangement.spacedBy((-6).dp)) {
                        words.filter { !it.emoji.startsWith("draw:") }.take(3).forEach { Pic(it.emoji, 34.dp) }
                    }
                    Text(c.emoji, fontSize = 54.sp)
                    Text(
                        if (Settings.lang == Lang.EN) c.en else c.ar,
                        fontSize = 19.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center,
                    )
                    Text("‪$done / ${words.size}‬", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun LangChips() {
    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Lang.values().forEach { l ->
            val sel = Settings.lang == l
            Text(
                l.label,
                Modifier.clip(RoundedCornerShape(50)).clickable { Settings.lang = l; Settings.save(); Sfx.tap() }
                    .background(if (sel) Ink else Color.White).padding(horizontal = 14.dp, vertical = 8.dp),
                color = if (sel) Color.White else Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp,
            )
        }
    }
}

// ───────────────────────── Learn ─────────────────────────

@Composable
private fun LearnScreen(cat: Category, speaker: Speaker, onBack: () -> Unit) {
    if (screenWidth() >= 720) {
        LearnTablet(cat, speaker, onBack)
        return
    }
    var showTips by remember { mutableStateOf(false) }
    val words = remember(cat, Settings.lang) { WordRepo.of(cat) }
    val pager = rememberPagerState { words.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pager.currentPage, Settings.lang) {
        val w = words.getOrNull(pager.currentPage) ?: return@LaunchedEffect
        speaker.say(w)
        Progress.markLearned(w.id)
    }
    if (showTips) TipsDialog(cat) { showTips = false }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(cat.color.copy(alpha = 0.35f), Color.White)))
            .statusBarsPadding().navigationBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundBtn("➜", cat.color, onBack)
            Text(
                if (Settings.lang == Lang.EN) cat.en else cat.ar, Modifier.weight(1f), textAlign = TextAlign.Center,
                fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink,
            )
            RoundBtn("💡", cat.color, { showTips = true })
            Spacer(Modifier.width(8.dp))
            Text("‪${pager.currentPage + 1}/${words.size}‬", fontWeight = FontWeight.Bold, color = Ink)
        }
        Box(Modifier.padding(horizontal = 12.dp)) { LangChips() }
        HorizontalPager(pager, Modifier.weight(1f).fillMaxWidth()) { i ->
            val w = words[i]
            Column(
                Modifier.fillMaxSize().padding(20.dp).clickable { speaker.say(w) },
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                EmojiBubble(w.emoji, 260.dp, cat.color)
                Spacer(Modifier.height(24.dp))
                if (Settings.showText) {
                    Text(w.text(Settings.lang), fontSize = 44.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center)
                    if (Settings.lang != Lang.EN) Text(w.en, fontSize = 18.sp, color = Ink.copy(alpha = 0.55f))
                }
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoundBtn("🔊", cat.color, { speaker.say(w) }, 64)
                    RoundBtn("🐢", cat.color, { speaker.say(w, slow = true) }, 64)
                    if (w.sound != null) RoundBtn("🎵", cat.color, { speaker.saySound(w) }, 64)
                }
            }
        }
        // thumbnail strip
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()).padding(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            words.forEachIndexed { i, w ->
                Box(
                    Modifier.size(52.dp).clip(RoundedCornerShape(14.dp))
                        .background(if (i == pager.currentPage) cat.color else Color.White)
                        .clickable { scope.launch { pager.animateScrollToPage(i) } },
                    contentAlignment = Alignment.Center,
                ) { Pic(w.emoji, 34.dp) }
            }
        }
    }
}

@Composable
private fun RoundBtn(label: String, color: Color, onClick: () -> Unit, size: Int = 44) {
    Box(
        Modifier.size(size.dp).clip(CircleShape).background(color).clickable { Sfx.tap(); onClick() },
        contentAlignment = Alignment.Center,
    ) { Text(label, fontSize = (size * 0.45f).sp, color = Color.White) }
}

// ───────────────────────── Games ─────────────────────────

@Composable
private fun GamesTab(onPlay: (GameDef, Category?) -> Unit) {
    var cat by remember { mutableStateOf<Category?>(null) }
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("ألعاب تعليمية", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (listOf<Category?>(null) + Category.values().toList()).forEach { c ->
                val sel = cat == c
                Text(
                    if (c == null) "🌈 الكل" else "${c.emoji} ${c.ar}",
                    Modifier.clip(RoundedCornerShape(50)).clickable { cat = c; Sfx.tap() }
                        .background(if (sel) Ink else Color.White).padding(horizontal = 14.dp, vertical = 8.dp),
                    color = if (sel) Color.White else Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            GridCells.Fixed(gridCols(240, 2, 4)),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(GAMES) { g ->
                val best = Progress.version.let { Progress.best(g.id) }
                Column(
                    Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(26.dp))
                        .background(Brush.linearGradient(listOf(g.color, g.color.copy(alpha = 0.6f))))
                        .clickable { Sfx.tap(); onPlay(g, cat) }.padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
                ) {
                    Text(g.emoji, fontSize = 48.sp)
                    Text(g.title, fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
                    Text(if (best > 0) "أفضل نتيجة $best٪" else "جديد!", fontSize = 12.sp, color = Color.White.copy(alpha = 0.9f))
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun ResultScreen(r: Screen.Result, onAgain: () -> Unit, onHome: () -> Unit) {
    val stars = if (r.total == 0) 0 else (r.score * 3 / r.total).coerceIn(0, 3)
    Column(
        Modifier.fillMaxSize().background(KidBg).statusBarsPadding().navigationBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Text(if (stars >= 2) "أحسنت! 🎉" else "حاول مرة أخرى 💪", fontSize = 36.sp, fontWeight = FontWeight.Black, color = Ink)
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { Text(if (it < stars) "⭐" else "☆", fontSize = 64.sp) }
        }
        Spacer(Modifier.height(8.dp))
        Text("${r.score} / ${r.total}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(32.dp))
        BigButton("🔁 العب مرة أخرى", r.game.color, onAgain)
        Spacer(Modifier.height(12.dp))
        BigButton("🏠 الرئيسية", Color(0xFF6C757D), onHome)
    }
}

@Composable
private fun BigButton(text: String, color: Color, onClick: () -> Unit) {
    Text(
        text,
        Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(color).clickable { Sfx.tap(); onClick() }.padding(vertical = 16.dp),
        textAlign = TextAlign.Center, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black,
    )
}

// ───────────────────────── Progress ─────────────────────────

@Composable
private fun ProgressTab() {
    val ver = Progress.version
    val learned = Progress.learned()
    LazyColumn(Modifier.fillMaxSize().statusBarsPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("متابعة أداء الطفل", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("⭐ ${Progress.stars()} نجمة · ${learned.size} كلمة تعلّمها من ${WordRepo.all.size}", color = Ink, fontWeight = FontWeight.Bold)
        }
        items(Category.values().toList()) { c ->
            val ws = WordRepo.of(c)
            val d = ws.count { it.id in learned }
            Bar("${c.emoji} ${c.ar}", d.toFloat() / ws.size, "$d/${ws.size}", c.color)
        }
        item { Text("الألعاب", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink, modifier = Modifier.padding(top = 8.dp)) }
        items(GAMES) { g ->
            val plays = Progress.plays(g.id)
            val acc = if (Progress.total(g.id) == 0) 0f else Progress.correct(g.id).toFloat() / Progress.total(g.id)
            Bar("${g.emoji} ${g.title}", acc, "$plays مرة", g.color)
        }
        item { Spacer(Modifier.height(if (ver >= 0) 8.dp else 0.dp)) }
    }
}

@Composable
private fun Bar(label: String, frac: Float, right: String, color: Color) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(12.dp)) {
        Row {
            Text(label, Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Ink)
            Text(right, color = Ink.copy(alpha = 0.6f), fontSize = 13.sp)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)).background(Color(0x1A000000))) {
            Box(Modifier.fillMaxWidth(frac.coerceIn(0f, 1f)).height(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
        }
    }
}

// ───────────────────────── Settings ─────────────────────────

@Composable
private fun SettingsTab(speaker: Speaker) {
    var showDev by remember { mutableStateOf(false) }
    if (showDev) { DeveloperScreen { showDev = false }; return }
    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("الإعدادات", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
        Card2 {
            Text("ℹ️ عن المطوّر", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text("قيّم التطبيق، وشاهد تطبيقات أخرى للمطوّر.", color = Ink.copy(alpha = 0.7f), fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                "عرض",
                Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF4361EE)).clickable { Sfx.tap(); showDev = true }
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp,
            )
        }
        var changing by remember { mutableStateOf(false) }
        if (changing) {
            androidx.compose.ui.window.Dialog(onDismissRequest = { changing = false }) {
                Column(Modifier.clip(RoundedCornerShape(28.dp)).background(Color.White).padding(20.dp)) {
                    PasswordSetup("تغيير كلمة المرور", "اختر كلمة مرور جديدة لمنطقة الأهل.", "حفظ") { ParentLock.set(it); changing = false }
                }
            }
        }
        Card2 {
            Text("🔐 كلمة المرور", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    "تغيير كلمة المرور",
                    Modifier.clip(RoundedCornerShape(50)).background(Color(0x14000000)).clickable { changing = true }.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                )
                Text(
                    "🔒 قفل الآن",
                    Modifier.clip(RoundedCornerShape(50)).background(Color(0xFF4361EE)).clickable { gateOpen = false }.padding(horizontal = 16.dp, vertical = 10.dp),
                    color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                )
            }
        }
        Card2 {
            Text("اللغة", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(8.dp))
            LangChips()
        }
        Card2 {
            Text("اسم الطفل", fontWeight = FontWeight.Bold, color = Ink)
            TextField(
                Settings.childName, { Settings.childName = it; Settings.save() },
                Modifier.fillMaxWidth(), singleLine = true,
            )
        }
        Card2 {
            ToggleRow("🔊 صوت نطق الكلمات", Settings.voiceOn) { Settings.voiceOn = it; Settings.save() }
            ToggleRow("🎵 المؤثرات الصوتية", Settings.sfxOn) { Settings.sfxOn = it; Settings.save() }
            ToggleRow("🔤 إظهار الكلمات مكتوبة", Settings.showText) { Settings.showText = it; Settings.save() }
            ToggleRow("🌙 الوضع الهادئ (بدون أصوات الخطأ)", Settings.calm) { Settings.calm = it; Settings.save() }
        }
        Card2 {
            Text("⏰ مدة اللعب في كل جلسة", fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(5, 10, 15, 20, 30, 0).forEach { m ->
                    val sel = Settings.playMinutes == m
                    Text(
                        if (m == 0) "بلا حد" else "$m دقيقة",
                        Modifier.clip(RoundedCornerShape(50)).clickable { Settings.playMinutes = m; Settings.save(); Sfx.tap() }
                            .background(if (sel) Ink else Color(0x14000000)).padding(horizontal = 14.dp, vertical = 8.dp),
                        color = if (sel) Color.White else Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                    )
                }
            }
        }
        Card2 {
            Text("🐢 سرعة النطق", fontWeight = FontWeight.Bold, color = Ink)
            Slider(
                Settings.speechRate, { Settings.speechRate = it },
                valueRange = 0.4f..1.2f, onValueChangeFinished = { Settings.save(); speaker.sayText(WordRepo.all.first().text(Settings.lang)) },
            )
        }
    }
}

@Composable
private fun Card2(content: @Composable () -> Unit) {
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(14.dp)) { content() }
}

@Composable
private fun ToggleRow(label: String, on: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = Ink, fontWeight = FontWeight.Bold)
        Switch(on, onChange)
    }
}

// ───────────────────────── Daily goal & parent gate ─────────────────────────

@Composable
private fun DailyGoal() {
    val n = Progress.version.let { Progress.today() }
    val goal = Settings.dailyGoal
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).padding(12.dp)) {
        Row {
            Text("🎯 هدف اليوم", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = Ink)
            Text(if (n >= goal) "أنجزت الهدف! 🎉" else "‪$n / ${goal}‬", color = Ink.copy(alpha = 0.7f), fontSize = 13.sp)
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0x1A000000))) {
            Box(Modifier.fillMaxWidth((n.toFloat() / goal).coerceIn(0f, 1f)).height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xFFFFB703)))
        }
    }
}

/** The parent area stays closed until the parents pick a password. Unlocked for the rest of the session. */
private var gateOpen by mutableStateOf(false)

@Composable
private fun PasswordField(value: String, onChange: (String) -> Unit, label: String) {
    androidx.compose.material3.OutlinedTextField(
        value = value, onValueChange = onChange, label = { Text(label) }, singleLine = true,
        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
    )
}

/** Choose (or change) the password: two matching fields, at least 4 characters. */
@Composable
private fun PasswordSetup(title: String, intro: String, button: String, onDone: (String) -> Unit) {
    var a by remember { mutableStateOf("") }
    var b by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(title, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center)
        Text(intro, color = Ink.copy(alpha = 0.75f), fontSize = 15.sp, textAlign = TextAlign.Center)
        PasswordField(a, { a = it; error = "" }, "كلمة المرور (4 أحرف أو أرقام على الأقل)")
        PasswordField(b, { b = it; error = "" }, "أعد كتابة كلمة المرور")
        if (error.isNotEmpty()) Text(error, color = Color(0xFFD62828), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        Text(
            button,
            Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0xFF4361EE)).clickable {
                when {
                    a.length < 4 -> error = "كلمة المرور قصيرة، اكتب 4 أحرف أو أرقام على الأقل."
                    a != b -> error = "الكلمتان غير متطابقتين."
                    else -> onDone(a)
                }
            }.padding(vertical = 14.dp),
            color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
        )
        Text(
            "تذكّر كلمة المرور جيداً. إذا نسيتها فيمكنك مسح بيانات التطبيق من إعدادات الجهاز، لكن سيُحذف التقدّم.",
            color = Ink.copy(alpha = 0.55f), fontSize = 12.sp, textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun ParentGate(forceAsk: Boolean = false, content: @Composable () -> Unit) {
    var localOpen by remember { mutableStateOf(false) }
    if ((gateOpen && !forceAsk) || localOpen) { content(); return }
    var hasPw by remember { mutableStateOf(ParentLock.hasPassword()) }
    var pw by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var fails by remember { mutableIntStateOf(0) }
    var lockedUntil by remember { mutableStateOf(0L) }
    fun unlocked() { if (forceAsk) localOpen = true else gateOpen = true }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        if (!hasPw) {
            PasswordSetup(
                "🔒 تفعيل منطقة الأهل",
                "منطقة الأهل غير مفعّلة بعد. اختر كلمة مرور تحمي الإعدادات ومتابعة التقدّم، ولن يستطيع الطفل فتحها.",
                "تفعيل",
            ) { ParentLock.set(it); hasPw = true; unlocked() }
        } else {
            Text("🔒 منطقة الأهل", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
            Spacer(Modifier.height(8.dp))
            Text("اكتب كلمة المرور للدخول", color = Ink.copy(alpha = 0.7f))
            Spacer(Modifier.height(14.dp))
            PasswordField(pw, { pw = it; error = "" }, "كلمة المرور")
            if (error.isNotEmpty()) {
                Spacer(Modifier.height(6.dp))
                Text(error, color = Color(0xFFD62828), fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            }
            Spacer(Modifier.height(14.dp))
            Text(
                "دخول",
                Modifier.fillMaxWidth().clip(RoundedCornerShape(50)).background(Color(0xFF4361EE)).clickable {
                    val now = System.currentTimeMillis()
                    if (now < lockedUntil) {
                        error = "محاولات كثيرة. انتظر ${((lockedUntil - now) / 1000) + 1} ثانية."
                    } else if (ParentLock.check(pw)) {
                        pw = ""; unlocked()
                    } else {
                        Sfx.wrong(); fails++
                        if (fails >= 5) { lockedUntil = now + 30_000; fails = 0; error = "محاولات كثيرة. انتظر 30 ثانية." }
                        else error = "كلمة المرور غير صحيحة."
                    }
                }.padding(vertical = 14.dp),
                color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(10.dp))
            Text(
                "نسيتَ كلمة المرور؟ امسح بيانات التطبيق من إعدادات الجهاز (سيُحذف التقدّم).",
                color = Ink.copy(alpha = 0.55f), fontSize = 12.sp, textAlign = TextAlign.Center,
            )
        }
    }
}

// ───────────────────────── Stickers, tips, time-up ─────────────────────────

@Composable
private fun StickerBadge(w: Word, size: androidx.compose.ui.unit.Dp, unlocked: Boolean, points: Int = 0) {
    val c = w.cat.color
    Box(
        Modifier.size(size).clip(RoundedCornerShape(size * 0.28f))
            .background(
                if (unlocked) Brush.linearGradient(listOf(Color.White, c.copy(alpha = 0.55f), c))
                else Brush.linearGradient(listOf(Color(0xFFEDEDF3), Color(0xFFD5D5DE)))
            ),
        contentAlignment = Alignment.Center,
    ) {
        if (unlocked) Pic(w.emoji, size * 0.62f)
        else Pic(w.emoji, size * 0.55f, Modifier.alpha(if (points > 0) 0.55f else 0.22f))
        if (unlocked) Text("⭐", Modifier.align(Alignment.TopEnd).padding(4.dp), fontSize = 14.sp)
        else Row(Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(Progress.MASTERY) { i ->
                Box(Modifier.size(size * 0.1f).clip(CircleShape).background(if (i < points) Color(0xFFFFB703) else Color(0x33000000)))
            }
        }
    }
}

@Composable
private fun AlbumTab(speaker: Speaker) {
    val ver = Progress.version
    var cat by remember { mutableStateOf<Category?>(null) }
    val words = remember(cat, Settings.lang) { WordRepo.all.filter { cat == null || it.cat == cat } }
    val total = WordRepo.all.size
    val got = if (ver >= 0) WordRepo.all.count { Progress.isMastered(it.id) } else 0
    Column(Modifier.fillMaxSize().statusBarsPadding().padding(horizontal = 16.dp)) {
        Spacer(Modifier.height(12.dp))
        Text("ملصقاتي 🌟", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
        Text(
            "جمعتَ $got من $total ملصقاً. أجب عن الكلمة صحيحاً من أول مرة في لعبة لتفتح ملصقها، أو شاهدها في التعلّم في يومين مختلفين.",
            fontSize = 13.sp, color = Ink.copy(alpha = 0.7f),
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (listOf<Category?>(null) + Category.values().toList()).forEach { c ->
                val sel = cat == c
                Text(
                    if (c == null) "🌈 الكل" else "${c.emoji} ${c.ar}",
                    Modifier.clip(RoundedCornerShape(50)).clickable { cat = c; Sfx.tap() }
                        .background(if (sel) Ink else Color.White).padding(horizontal = 14.dp, vertical = 8.dp),
                    color = if (sel) Color.White else Ink, fontWeight = FontWeight.Bold, fontSize = 14.sp,
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        LazyVerticalGrid(
            GridCells.Fixed(gridCols(118, 3, 9)),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(words) { w ->
                val ok = ver >= 0 && Progress.isMastered(w.id)
                Column(
                    Modifier.clickable(enabled = ok) { speaker.say(w) },
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    StickerBadge(w, 96.dp, ok, if (ver >= 0) Progress.points(w.id) else 0)
                    Text(
                        if (ok) w.text(Settings.lang) else "${minOf(Progress.points(w.id), Progress.MASTERY)}/${Progress.MASTERY}",
                        fontSize = 11.sp, color = Ink, maxLines = 1, fontWeight = FontWeight.Bold,
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun StickerDialog(ws: List<Word>, onDismiss: () -> Unit) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.clip(RoundedCornerShape(32.dp)).background(Color.White).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                if (ws.size == 1) "🎉 ملصق جديد!" else "🎉 ${ws.size} ملصقات جديدة!",
                fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink,
            )
            Spacer(Modifier.height(12.dp))
            if (ws.size == 1) {
                StickerBadge(ws[0], 180.dp, true, Progress.MASTERY)
                Spacer(Modifier.height(8.dp))
                Text(ws[0].text(Settings.lang), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink)
            } else {
                ws.take(6).chunked(3).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { StickerBadge(it, 80.dp, true, Progress.MASTERY) }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                if (ws.size > 6) Text("+${ws.size - 6}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
            Spacer(Modifier.height(16.dp))
            BigButton("رائع!", Color(0xFF2EC4B6), onDismiss)
        }
    }
}

@Composable
private fun TipsDialog(cat: Category, onDismiss: () -> Unit) {
    val en = Settings.lang == Lang.EN
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Column(
            Modifier.clip(RoundedCornerShape(28.dp)).background(Color.White).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(if (en) "💡 Tips for parents" else "💡 نصائح للأهل", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
            (Tips.forCategory(cat, en) + Tips.general(en).take(2)).forEach {
                Text("• $it", fontSize = 15.sp, color = Ink)
            }
            Spacer(Modifier.height(4.dp))
            BigButton(if (en) "Got it" else "حسناً", cat.color, onDismiss)
        }
    }
}

@Composable
private fun TimeUpScreen(onParentUnlock: () -> Unit) {
    var asking by remember { mutableStateOf(false) }
    if (asking) {
        ParentGate(forceAsk = true) { LaunchedEffect(Unit) { onParentUnlock() } }
        return
    }
    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF2B2D6E), Color(0xFF5B3F9E))))
            .statusBarsPadding().navigationBarsPadding().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
    ) {
        Text("🌙", fontSize = 110.sp)
        Spacer(Modifier.height(12.dp))
        Text("انتهى وقت اللعب!", fontSize = 34.sp, fontWeight = FontWeight.Black, color = Color.White, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("أحسنتَ اليوم! حان وقت الراحة 💤", fontSize = 18.sp, color = Color.White.copy(alpha = 0.85f), textAlign = TextAlign.Center)
        Spacer(Modifier.height(40.dp))
        Text(
            "🔒 للأهل: متابعة اللعب",
            Modifier.clip(RoundedCornerShape(50)).background(Color(0x33FFFFFF)).clickable { asking = true }
                .padding(horizontal = 24.dp, vertical = 12.dp),
            color = Color.White, fontWeight = FontWeight.Bold,
        )
    }
}

// ───────────────────────── Tablet layout ─────────────────────────

@Composable
private fun screenWidth(): Int = androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp

@Composable
private fun isTablet(): Boolean = screenWidth() >= 600

/** Number of grid columns for tiles of about [minTile] dp, using the width left after the nav rail. */
@Composable
private fun gridCols(minTile: Int, min: Int, max: Int): Int {
    val w = screenWidth() - (if (isTablet()) 112 + 64 else 32)
    return (w / minTile).coerceIn(min, max)
}

@Composable
private fun NavRail(tab: Int, onTab: (Int) -> Unit) {
    val selected = tab.coerceAtMost(3)
    Column(
        Modifier.fillMaxHeight().width(112.dp).background(Color.White)
            .statusBarsPadding().navigationBarsPadding().padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Box(
            Modifier.size(60.dp).clip(RoundedCornerShape(20.dp))
                .background(Brush.linearGradient(listOf(Color(0xFF1FB5A8), Color(0xFF6A3FD0)))),
            contentAlignment = Alignment.Center,
        ) { Text("🐦", fontSize = 34.sp) }
        Spacer(Modifier.height(12.dp))
        listOf("📚" to "تعلّم", "🎮" to "ألعاب", "🌟" to "ملصقاتي", "📊" to "الأهل").forEachIndexed { i, (e, l) ->
            val sel = selected == i
            Column(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp))
                    .background(if (sel) Color(0xFFFFE08A) else Color.Transparent)
                    .clickable { Sfx.tap(); onTab(i) }.padding(vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(e, fontSize = 30.sp)
                Text(l, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
            }
        }
    }
}

/** Progress and settings side by side (behind the parent gate). */
@Composable
private fun ParentTablet(speaker: Speaker) {
    Row(Modifier.fillMaxSize()) {
        Box(Modifier.weight(1f).fillMaxHeight()) { ProgressTab() }
        Box(Modifier.weight(1f).fillMaxHeight()) { SettingsTab(speaker) }
    }
}

/** Learning with two panes: big picture on one side, every word of the category as a grid on the other. */
@Composable
private fun LearnTablet(cat: Category, speaker: Speaker, onBack: () -> Unit) {
    val words = remember(cat, Settings.lang) { WordRepo.of(cat) }
    var idx by remember(cat) { mutableIntStateOf(0) }
    var showTips by remember { mutableStateOf(false) }
    val i = idx.coerceIn(0, words.lastIndex)
    LaunchedEffect(i, Settings.lang, words) {
        words.getOrNull(i)?.let { speaker.say(it); Progress.markLearned(it.id) }
    }
    if (showTips) TipsDialog(cat) { showTips = false }
    val w = words[i]
    val h = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp
    val bubble = minOf(280, (h * 0.36f).toInt()).dp
    val cols = if (screenWidth() >= 1000) 6 else 5
    Column(
        Modifier.fillMaxSize()
            .background(Brush.verticalGradient(listOf(cat.color.copy(alpha = 0.35f), Color.White)))
            .statusBarsPadding().navigationBarsPadding().padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RoundBtn("➜", cat.color, onBack, 48)
            Spacer(Modifier.width(16.dp))
            Text(
                if (Settings.lang == Lang.EN) cat.en else cat.ar, Modifier.weight(1f),
                fontSize = 30.sp, fontWeight = FontWeight.Black, color = Ink,
            )
            LangChips()
        }
        Spacer(Modifier.height(16.dp))
        Row(Modifier.weight(1f).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(
                Modifier.weight(0.42f).fillMaxHeight().clip(RoundedCornerShape(30.dp))
                    .background(Color.White.copy(alpha = 0.7f)).clickable { speaker.say(w) }.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center,
            ) {
                EmojiBubble(w.emoji, bubble, cat.color)
                Spacer(Modifier.height(14.dp))
                if (Settings.showText) {
                    Text(w.text(Settings.lang), fontSize = 52.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center)
                    if (Settings.lang != Lang.EN) Text(w.en, fontSize = 20.sp, color = Ink.copy(alpha = 0.55f))
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    RoundBtn("🐢", cat.color, { speaker.say(w, slow = true) }, 60)
                    RoundBtn("🔊", cat.color, { speaker.say(w) }, 60)
                    RoundBtn("💡", cat.color, { showTips = true }, 60)
                    if (w.sound != null) RoundBtn("🎵", cat.color, { speaker.saySound(w) }, 60)
                }
                Spacer(Modifier.height(8.dp))
                Text("‪${i + 1} / ${words.size}‬", fontWeight = FontWeight.Bold, color = Ink)
            }
            LazyVerticalGrid(
                GridCells.Fixed(cols), Modifier.weight(0.58f).fillMaxHeight(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                itemsIndexed(words) { k, x ->
                    Box(
                        Modifier.fillMaxWidth().aspectRatio(1f).clip(RoundedCornerShape(18.dp))
                            .background(if (k == i) cat.color else Color.White)
                            .clickable { Sfx.tap(); idx = k },
                        contentAlignment = Alignment.Center,
                    ) { Pic(x.emoji, 44.dp) }
                }
            }
        }
    }
}

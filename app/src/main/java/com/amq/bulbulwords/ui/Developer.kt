package com.amq.bulbulwords.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amq.bulbulwords.data.Lang
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.games.Ink

private class DevApp(
    val pkg: String,
    val emoji: String,
    val color: Color,
    val nameAr: String,
    val nameEn: String,
    val descAr: String,
    val descEn: String,
)

/** Apps by the same developer that are live on Google Play (status "Production"). */
private val DEV_APPS = listOf(
    DevApp(
        "com.amq.tools", "🧰", Color(0xFF4361EE),
        "Utilix: All-in-One Toolbox", "Utilix: All-in-One Toolbox",
        "106 أداة مجانية في تطبيق واحد: ماسح مستندات وQR ومسطرة وفحص الهاتف وحاسبات وألعاب. معظمها يعمل بلا إنترنت.",
        "106 free tools in one app: document scanner, QR, ruler, phone test, calculators and games. Most work offline.",
    ),
    DevApp(
        "aq.store.a3d", "🧪", Color(0xFF06D6A0),
        "Science Lab", "Science Lab",
        "مختبر علوم في جيبك: 65 محاكاة تفاعلية في الكيمياء والفيزياء والأحياء والرياضيات وعلوم الأرض، بلا إنترنت.",
        "A pocket laboratory with 65 interactive simulations in chemistry, physics, biology, math and earth science, fully offline.",
    ),
    DevApp(
        "com.amq.phonecheckup", "📱", Color(0xFF3A86FF),
        "Phone Checkup: Test & Info", "Phone Checkup: Test & Info",
        "افحص صحة هاتفك: البطارية والحساسات ومقياس الصوت والسرعة واختبارات الشاشة واللمس والاهتزاز.",
        "Check your phone's health: battery, sensors, sound meter, GPS speed, and display, touch and vibration tests.",
    ),
    DevApp(
        "com.amq.currencyexchange", "💱", Color(0xFF118AB2),
        "Currency Exchange", "Currency Exchange",
        "حوّل بين أكثر من 160 عملة بأسعار حديثة مع رسوم الاتجاه ووضع بلا إنترنت.",
        "Convert between 160+ currencies with up-to-date rates, trend charts and offline mode.",
    ),
    DevApp(
        "com.qasem.book", "📖", Color(0xFF2A9D8F),
        "القرآن الكريم كامل نص بدون نت", "Holy Quran - Offline Text",
        "المصحف كاملاً بلا إنترنت، مع ختمتي والوضع الليلي والفهرس والبحث والأذكار والمسبحة.",
        "The complete Quran offline, with a daily reading goal, night mode, index and search, adhkar and a digital tasbih.",
    ),
)

private const val DEV_NAME = "Syntax Error"

internal fun openStore(ctx: Context, pkg: String) {
    try {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (_: ActivityNotFoundException) {
        try {
            ctx.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$pkg")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
            )
        } catch (_: Exception) { }
    }
}

/** Developer info: rate this app, contact, and the developer's live apps. Shown inside the parent area only. */
@Composable
fun DeveloperScreen(onBack: () -> Unit) {
    val ctx = LocalContext.current
    val en = Settings.lang == Lang.EN
    androidx.activity.compose.BackHandler(onBack = onBack)
    Column(
        Modifier.fillMaxSize().statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(Color(0xFF4361EE)).clickable { Sfx.tap(); onBack() },
                contentAlignment = Alignment.Center,
            ) { Text("➜", fontSize = 22.sp, color = Color.White) }
            Spacer(Modifier.width(12.dp))
            Text(if (en) "About the developer" else "عن المطوّر", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink)
        }

        Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White).padding(16.dp)) {
            Text(DEV_NAME, fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                if (en) "Thanks for using Bulbul Words. If it helps your child, a rating helps other parents find it."
                else "شكراً لاستخدامك بلبل الكلمات. إذا أفاد طفلك فتقييمك يساعد أهالي آخرين على العثور عليه.",
                color = Ink.copy(alpha = 0.75f), fontSize = 14.sp,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    if (en) "⭐ Rate this app" else "⭐ قيّم التطبيق",
                    Modifier.clip(RoundedCornerShape(50)).background(Color(0xFFFFB703)).clickable { Sfx.tap(); openStore(ctx, ctx.packageName) }
                        .padding(horizontal = 18.dp, vertical = 11.dp),
                    color = Ink, fontWeight = FontWeight.Black, fontSize = 15.sp,
                )
            }
        }

        Text(if (en) "More apps by the developer" else "المزيد من تطبيقات المطوّر", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Ink)

        DEV_APPS.forEach { app ->
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(22.dp)).background(Color.White)
                    .clickable { Sfx.tap(); openStore(ctx, app.pkg) }.padding(14.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Box(
                    Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)).background(app.color),
                    contentAlignment = Alignment.Center,
                ) { Text(app.emoji, fontSize = 32.sp) }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(if (en) app.nameEn else app.nameAr, fontSize = 17.sp, fontWeight = FontWeight.Black, color = Ink)
                    Spacer(Modifier.height(3.dp))
                    Text(if (en) app.descEn else app.descAr, fontSize = 13.sp, color = Ink.copy(alpha = 0.75f))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        if (en) "View on Google Play" else "عرض في Google Play",
                        color = app.color, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

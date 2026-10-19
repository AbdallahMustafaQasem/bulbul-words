package com.amq.bulbulwords.data

/** Short, practical tips for parents. Each tip is at most 140 characters. */
object Tips {

    fun forCategory(c: Category, english: Boolean): List<String> =
        if (english) en(c) else ar(c)

    fun general(english: Boolean): List<String> =
        if (english) listOf(
            "Say the word clearly, then wait about 5 seconds to give your child time to respond.",
            "Praise every attempt, even a sound or a gesture. Every try counts.",
            "Keep sessions short and playful. Stop when your child loses interest.",
            "Never force your child to speak or correct them harshly. Just say the word again warmly.",
            "Repeat words often during daily routines, not only during practice."
        ) else listOf(
            "قل الكلمة بوضوح ثم انتظر نحو 5 ثوانٍ ليأخذ طفلك وقته في الرد.",
            "امدح كل محاولة، حتى لو كانت صوتًا أو إشارة. كل محاولة مهمة.",
            "اجعل الجلسات قصيرة وممتعة، وتوقف عندما يفقد طفلك الاهتمام.",
            "لا تجبر طفلك على الكلام ولا تصحح له بقسوة. أعد الكلمة بلطف فقط.",
            "كرر الكلمات كثيرًا خلال الروتين اليومي، وليس وقت التدريب فقط."
        )

    private fun en(c: Category): List<String> = when (c) {
        Category.FRUITS -> listOf(
            "Let your child touch, smell and taste the fruit while you say its name.",
            "Use real fruit at home or at the market and name it each time.",
            "Say the word, wait about 5 seconds, then praise any sound or try."
        )
        Category.VEGETABLES -> listOf(
            "Let your child wash or touch vegetables while you name them.",
            "Name vegetables while cooking, and say the word again at the table.",
            "Praise any attempt to say the word, and never force tasting."
        )
        Category.FOOD -> listOf(
            "Name the food at mealtime: say the word, then offer a small bite.",
            "Offer choices like 'bread or rice?' and wait for a word or a point.",
            "Keep mealtime talk relaxed and praise every attempt."
        )
        Category.CLOTHES -> listOf(
            "Name each item while dressing your child, and repeat it as you go.",
            "Let your child pick between two clothes and say the name together.",
            "Make it a game: 'Where is your sock?' and praise any try."
        )
        Category.ANIMALS -> listOf(
            "Make the animal sound together first, then say the name.",
            "Use toys, picture books or real animals to repeat the words.",
            "Wait about 5 seconds after the sound and praise any try."
        )
        Category.BIRDS -> listOf(
            "Copy the bird's sound together, like tweet or coo, then say its name.",
            "Watch birds from a window or park and name what you see.",
            "Keep it playful, wait for a response, and praise every try."
        )
        Category.BODY -> listOf(
            "Touch your own body part first, then your child's, while naming it.",
            "Sing songs like 'head, shoulders, knees' to repeat the words.",
            "Play 'Where is your nose?' and praise any point or sound."
        )
        Category.HOME -> listOf(
            "Walk around the house and name things your child can touch.",
            "Ask 'Where is the cup?' and let your child point or fetch it.",
            "Repeat the same words every day in the same places."
        )
        Category.TRANSPORT -> listOf(
            "Make vehicle sounds together, like beep or vroom, then say the name.",
            "Point out cars, buses and bikes on walks and name them.",
            "Play with toy vehicles and say the word before each move."
        )
        Category.FAMILY -> listOf(
            "Look at family photos and name each person together.",
            "Say 'Where is Mama?' and let your child point, then say the word.",
            "Praise any sound that tries to say a name, however small."
        )
        Category.PLACES -> listOf(
            "Name places when you visit them: park, shop, school.",
            "Look at photos of places before and after a trip.",
            "Say the word, wait about 5 seconds, and praise any try."
        )
        Category.ACTIONS -> listOf(
            "Act out each action together, like jump or sleep, while saying it.",
            "Name actions as they happen during the day: 'We are eating.'",
            "Take turns acting while the other guesses, and praise every try."
        )
        Category.FEELINGS -> listOf(
            "Name your own feelings out loud: 'I am happy.' Children learn by watching.",
            "Use a mirror to make happy and sad faces together.",
            "Accept every feeling calmly and name it, without correcting."
        )
        Category.NUMBERS -> listOf(
            "Count real things together: steps, toys, fruit, fingers.",
            "Touch each item as you count so your child sees one number per item.",
            "Keep counting short and fun, and praise any try."
        )
        Category.LETTERS -> listOf(
            "Trace the letter with a finger in sand, flour or on a foggy window.",
            "Say the letter's sound, then a simple word that starts with it.",
            "Keep it short and playful, and praise every try."
        )
    }

    private fun ar(c: Category): List<String> = when (c) {
        Category.FRUITS -> listOf(
            "دع طفلك يلمس الفاكهة ويشمّها ويتذوقها بينما تنطق اسمها.",
            "استخدم فواكه حقيقية في البيت أو السوق وسمّها في كل مرة.",
            "قل الكلمة، وانتظر نحو 5 ثوانٍ، ثم امدح أي صوت أو محاولة."
        )
        Category.VEGETABLES -> listOf(
            "دع طفلك يغسل الخضار أو يلمسها بينما تذكر أسماءها.",
            "سمِّ الخضار أثناء الطبخ، وكرر الكلمة على المائدة.",
            "امدح أي محاولة لنطق الكلمة، ولا تجبره على التذوق."
        )
        Category.FOOD -> listOf(
            "سمِّ الطعام وقت الأكل: قل الكلمة ثم قدّم لقمة صغيرة.",
            "قدّم خيارين مثل: خبز أم أرز؟ وانتظر كلمة أو إشارة.",
            "اجعل جو الأكل هادئًا وامدح كل محاولة."
        )
        Category.CLOTHES -> listOf(
            "سمِّ كل قطعة وأنت تلبّس طفلك، وكررها أثناء ذلك.",
            "دعه يختار بين قطعتين ثم قولا الاسم معًا.",
            "العب لعبة: أين الجورب؟ وامدح أي محاولة."
        )
        Category.ANIMALS -> listOf(
            "قلّدا صوت الحيوان معًا أولًا، ثم قل اسمه.",
            "استخدم ألعابًا أو كتب صور أو حيوانات حقيقية لتكرار الكلمات.",
            "انتظر نحو 5 ثوانٍ بعد الصوت وامدح أي محاولة."
        )
        Category.BIRDS -> listOf(
            "قلّدا صوت الطائر معًا، مثل زقزقة، ثم قل اسمه.",
            "راقبا الطيور من النافذة أو الحديقة وسمِّ ما تريانه.",
            "اجعل الأمر ممتعًا، وانتظر الرد، وامدح كل محاولة."
        )
        Category.BODY -> listOf(
            "المس عضوًا من جسمك أولًا ثم جسم طفلك وأنت تسمّيه.",
            "غنّيا أغاني عن أجزاء الجسم لتكرار الكلمات.",
            "العب: أين أنفك؟ وامدح أي إشارة أو صوت."
        )
        Category.HOME -> listOf(
            "تجولا في البيت وسمِّ الأشياء التي يستطيع طفلك لمسها.",
            "اسأل: أين الكوب؟ ودعه يشير إليه أو يحضره.",
            "كرر الكلمات نفسها كل يوم في الأماكن نفسها."
        )
        Category.TRANSPORT -> listOf(
            "قلّدا أصوات المركبات معًا، مثل بي بي، ثم قل الاسم.",
            "أشر إلى السيارات والحافلات والدراجات في الطريق وسمّها.",
            "العبا بمركبات لعبة وقل الكلمة قبل كل حركة."
        )
        Category.FAMILY -> listOf(
            "انظرا إلى صور العائلة وسمّيا كل شخص معًا.",
            "قل: أين ماما؟ ودعه يشير، ثم قل الكلمة.",
            "امدح أي صوت يحاول به نطق اسم، مهما كان صغيرًا."
        )
        Category.PLACES -> listOf(
            "سمِّ الأماكن عند زيارتها: الحديقة، المتجر، المدرسة.",
            "انظرا إلى صور الأماكن قبل الرحلة وبعدها.",
            "قل الكلمة، وانتظر نحو 5 ثوانٍ، وامدح أي محاولة."
        )
        Category.ACTIONS -> listOf(
            "مثّلا كل فعل معًا، مثل يقفز أو ينام، وأنت تقوله.",
            "سمِّ الأفعال وقت حدوثها خلال اليوم: نحن نأكل.",
            "تبادلا التمثيل والتخمين، وامدح كل محاولة."
        )
        Category.FEELINGS -> listOf(
            "سمِّ مشاعرك بصوت عالٍ: أنا سعيد. الطفل يتعلم بالمشاهدة.",
            "استخدما مرآة لتصنعا وجوهًا سعيدة وحزينة معًا.",
            "تقبّل كل شعور بهدوء وسمِّه، دون تصحيح."
        )
        Category.NUMBERS -> listOf(
            "عدّا أشياء حقيقية معًا: الدرجات، الألعاب، الفواكه، الأصابع.",
            "المس كل شيء وأنت تعدّ ليرى طفلك رقمًا لكل شيء.",
            "اجعل العدّ قصيرًا وممتعًا، وامدح أي محاولة."
        )
        Category.LETTERS -> listOf(
            "ارسما الحرف بالإصبع في الرمل أو الدقيق أو على زجاج مبخّر.",
            "قل صوت الحرف ثم كلمة بسيطة تبدأ به.",
            "اجعل الوقت قصيرًا وممتعًا، وامدح كل محاولة."
        )
    }
}

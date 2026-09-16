package com.example.telugucalendar.panchangam

/**
 * Telugu names for panchangam elements.
 * These name lists themselves (tithi names, nakshatra names, etc.) are standard
 * and widely known; I'm confident in the lists below. What is NOT guaranteed to be
 * precise is exactly *which* tithi/nakshatra/masa applies at a given moment, since
 * that depends on the approximate astronomy engine (see AstronomyUtils.kt).
 */
object TeluguNames {

    val tithiNames = listOf(
        "పాడ్యమి", "విదియ", "తదియ", "చవితి", "పంచమి",
        "షష్ఠి", "సప్తమి", "అష్టమి", "నవమి", "దశమి",
        "ఏకాదశి", "ద్వాదశి", "త్రయోదశి", "చతుర్దశి", "పౌర్ణమి",
        "పాడ్యమి", "విదియ", "తదియ", "చవితి", "పంచమి",
        "షష్ఠి", "సప్తమి", "అష్టమి", "నవమి", "దశమి",
        "ఏకాదశి", "ద్వాదశి", "త్రయోదశి", "చతుర్దశి", "అమావాస్య"
    )

    val pakshaNames = listOf("శుక్ల పక్షం", "కృష్ణ పక్షం")

    val nakshatraNames = listOf(
        "అశ్విని", "భరణి", "కృత్తిక", "రోహిణి", "మృగశిర", "ఆరుద్ర",
        "పునర్వసు", "పుష్యమి", "ఆశ్లేష", "మఖ", "పుబ్బ", "ఉత్తర",
        "హస్త", "చిత్త", "స్వాతి", "విశాఖ", "అనూరాధ", "జ్యేష్ఠ",
        "మూల", "పూర్వాషాఢ", "ఉత్తరాషాఢ", "శ్రవణం", "ధనిష్ఠ", "శతభిషం",
        "పూర్వాభాద్ర", "ఉత్తరాభాద్ర", "రేవతి"
    )

    val rashiNames = listOf(
        "మేషం", "వృషభం", "మిథునం", "కర్కాటకం", "సింహం", "కన్య",
        "తుల", "వృశ్చికం", "ధనుస్సు", "మకరం", "కుంభం", "మీనం"
    )

    val yogaNames = listOf(
        "విష్కంభ", "ప్రీతి", "ఆయుష్మాన్", "సౌభాగ్య", "శోభన", "అతిగండ",
        "సుకర్మ", "ధృతి", "శూల", "గండ", "వృద్ధి", "ధ్రువ",
        "వ్యాఘాత", "హర్షణ", "వజ్ర", "సిద్ధి", "వ్యతీపాత", "వరీయాన్",
        "పరిఘ", "శివ", "సిద్ధ", "సాధ్య", "శుభ", "శుక్ల",
        "బ్రహ్మ", "ఐంద్ర", "వైధృతి"
    )

    val varaNames = listOf(
        "ఆదివారం", "సోమవారం", "మంగళవారం", "బుధవారం",
        "గురువారం", "శుక్రవారం", "శనివారం"
    ) // index 0 = Sunday, matches Calendar.DAY_OF_WEEK - 1

    // Masa names indexed by sidereal Sun rashi (0=Mesha ... 11=Meena).
    // This maps "which rashi the Sun currently occupies" to the amanta lunar month
    // name commonly used in Telugu calendars. This is a simplification — the true
    // masa boundary is the amavasya, not the sankranti, so results can be
    // off by a few days right around a month boundary. Verify against a known source.
    val masaNamesByRashi = listOf(
        "వైశాఖం",   // Mesha
        "జ్యేష్ఠం",  // Vrishabha
        "ఆషాఢం",   // Mithuna
        "శ్రావణం",  // Karka
        "భాద్రపదం", // Simha
        "ఆశ్వయుజం", // Kanya
        "కార్తీకం",  // Tula
        "మార్గశిరం", // Vrischika
        "పుష్యం",   // Dhanu
        "మాఘం",    // Makara
        "ఫాల్గుణం",  // Kumbha
        "చైత్రం"    // Meena
    )
}

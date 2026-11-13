package com.glow.app.media

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val WHATSAPP_AUDIO = Regex("""^AUD-(\d{4})(\d{2})(\d{2})-WA\d+$""", RegexOption.IGNORE_CASE)
private val AUDIO_EXT = Regex("""\.(mp3|m4a|aac|ogg|opus|wav|amr|flac|wma|3gp)$""", RegexOption.IGNORE_CASE)

/** عنوان للعرض فقط: يُخفي الامتداد ويحوّل أسماء واتساب الخام لصيغة مقروءة. الاسم الأصلي في [Track.title] لا يتغير. */
val Track.displayTitle: String
    get() {
        val base = title.trim().replace(AUDIO_EXT, "")
        WHATSAPP_AUDIO.matchEntire(base)?.let { m ->
            val (y, mo, d) = m.destructured
            return "رسالة واتساب الصوتية · $d/$mo/$y"
        }
        return base.replace('_', ' ').trim().ifEmpty { title }
    }

/** سطر ثانوي عند غياب الفنان: تاريخ الإضافة. */
val Track.addedLabel: String?
    get() = if (dateAddedSec <= 0) null else
        "أُضيف " + SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH).format(Date(dateAddedSec * 1000))

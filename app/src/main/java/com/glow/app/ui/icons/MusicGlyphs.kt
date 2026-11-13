package com.glow.app.ui.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * أيقونات الموسيقى (الملف 3 من 4).
 *
 * تعتمد على IconEngine.kt فقط. نمط Duotone: الجسم خافت والتفصيل قوي،
 * كما في مرجع Iconly. في نمط Solid يصبح الجزء الخافت بشفافية، وفي Line
 * يصير كل شيء خطاً. رؤوس النوتات ممتلئة في كل الأنماط.
 *
 * الاستعمال:
 *     GlowGlyph(MusicGlyphs.Note, tint = color, size = 24.dp)
 */

object MusicGlyphs {

    /** ألبوم: مربع بنوتة داخل قوسين جانبيين. */
    val AlbumFrame = glyph("album_frame") {
        stroke(path {
            moveTo(5.6f, 4.8f)
            lineTo(4.6f, 4.8f)
            quadTo(2.8f, 4.8f, 2.8f, 6.6f)
            lineTo(2.8f, 17.4f)
            quadTo(2.8f, 19.2f, 4.6f, 19.2f)
            lineTo(5.6f, 19.2f)
        })
        stroke(path {
            moveTo(18.4f, 4.8f)
            lineTo(19.4f, 4.8f)
            quadTo(21.2f, 4.8f, 21.2f, 6.6f)
            lineTo(21.2f, 17.4f)
            quadTo(21.2f, 19.2f, 19.4f, 19.2f)
            lineTo(18.4f, 19.2f)
        })
        softBody(path { rrect(7.4f, 5f, 9.2f, 14f, 2.4f) })
        dot(11.2f, 14.8f, 1.8f)
        stem(12.9f, 8.8f, 14.8f)
        stroke(path {
            moveTo(12.9f, 8.8f)
            quadTo(13.4f, 10.6f, 15f, 10.8f)
        }, widthUnits = 1.5f)
    }

    /** ألبوم بخطوط جانبية (قائمة الأغاني). */
    val AlbumLines = glyph("album_lines") {
        softBody(path { rrect(3.2f, 4.6f, 12.6f, 14.8f, 2.6f) })
        dot(7.6f, 14.6f, 1.9f)
        stem(9.5f, 8.6f, 14.6f)
        stroke(path {
            moveTo(9.5f, 8.6f)
            quadTo(10.1f, 10.6f, 12f, 10.8f)
        }, widthUnits = 1.5f)
        line(18.6f, 7.8f, 18.6f, 16.2f, Tone.Soft, softAlpha())
        line(21.2f, 9.8f, 21.2f, 14.2f, Tone.Soft, softAlpha())
    }

    /** ألبوم بقرص نصفي. */
    val AlbumDisc = glyph("album_disc") {
        softBody(path { arc(15.4f, 12f, 7f, -90f, 180f); close() })
        stroke(path {
            moveTo(12.6f, 3.6f)
            lineTo(7.6f, 3.6f)
            quadTo(4.2f, 3.6f, 4.2f, 7f)
            lineTo(4.2f, 17f)
            quadTo(4.2f, 20.4f, 7.6f, 20.4f)
            lineTo(12.6f, 20.4f)
            close()
        })
        dot(7.6f, 15.8f, 1.9f)
        stem(9.5f, 8.8f, 15.8f)
    }

    /** نوتة واحدة بعلم. */
    val Note = glyph("note") { singleNote() }

    /** نوتتان متصلتان بعارضة. */
    val NoteDouble = glyph("note_double") {
        softBody(
            path {
                polygon(
                    listOf(Offset(9.4f, 6.8f), Offset(18.8f, 5f), Offset(18.8f, 9f), Offset(9.4f, 10.8f)),
                    0.8f,
                )
            },
        )
        dot(7f, 18f, 2.6f)
        dot(16.4f, 16.2f, 2.6f)
        stem(9.4f, 6.8f, 18f)
        stem(18.8f, 5f, 16.2f)
    }

    /** إضافة نوتة (علامة +). */
    val NoteAdd = glyph("note_add") {
        softBody(
            path {
                polygon(
                    listOf(Offset(8.1f, 8.4f), Offset(14.7f, 6.8f), Offset(14.7f, 10.6f), Offset(8.1f, 12.2f)),
                    0.7f,
                )
            },
        )
        dot(6f, 18.4f, 2.3f)
        dot(12.6f, 16.8f, 2.3f)
        stem(8.1f, 8.4f, 18.4f)
        stem(14.7f, 6.8f, 16.8f)
        line(19.8f, 2.8f, 19.8f, 8.4f)
        line(17f, 5.6f, 22.6f, 5.6f)
    }

    /** نوتة داخل قرص متقطّع. */
    val NoteDisc = glyph("note_disc") {
        for (i in 0 until 8) {
            stroke(path { arc(12f, 12f, 9.2f, i * 45f + 8f, 29f) })
        }
        dot(10.2f, 15.2f, 2.2f)
        stem(12.2f, 8.2f, 15.2f)
        stroke(path {
            moveTo(12.2f, 8.2f)
            quadTo(12.8f, 10.8f, 15.8f, 11.2f)
        }, widthUnits = 1.5f)
    }

    /** نوتة مشطوبة (كتم الموسيقى). */
    val NoteOff = glyph("note_off") {
        singleNote()
        line(4.2f, 3.6f, 19.8f, 20.4f)
    }

    /** سماعات. */
    val Headphones = glyph("headphones") { headphonesBase() }

    /** سماعات بموجة صوت بين الأذنين. */
    val HeadphonesWave = glyph("headphones_wave") {
        headphonesBase()
        bar(8.8f, 15.2f, 1.6f, 3.2f, 0.8f)
        bar(11.2f, 13.4f, 1.6f, 6.8f, 0.8f)
        bar(13.6f, 15.2f, 1.6f, 3.2f, 0.8f)
    }

    /** ميكروفون. */
    val Mic = glyph("mic") { micBase() }

    /** ميكروفون بعلامة تنبيه. */
    val MicAlert = glyph("mic_alert") {
        micBase()
        line(12f, 6.4f, 12f, 9.8f)
        dot(12f, 12.4f, 0.95f)
    }

    /** ميكروفون مكتوم. */
    val MicOff = glyph("mic_off") {
        micBase()
        line(4.2f, 3.6f, 19.8f, 20.4f)
    }

    /** بحث داخل القائمة. */
    val SearchList = glyph("search_list") {
        line(3.6f, 5.4f, 20.4f, 5.4f, Tone.Soft, softAlpha())
        line(3.6f, 11.6f, 9.4f, 11.6f)
        line(3.6f, 17.8f, 9.4f, 17.8f)
        if (spec.style == IconStyle.Duotone) {
            fill(path { circle(15.2f, 15.2f, 3.8f) }, Tone.Soft)
        }
        ring(15.2f, 15.2f, 3.8f)
        line(18.2f, 18.2f, 20.8f, 20.8f)
    }

    /** قرص (أسطوانة). */
    val Disc = glyph("disc") {
        softBody(path { circle(12f, 12f, 9f) })
        ring(12f, 12f, 5.4f, Tone.Strong, 0.55f)
        dot(12f, 12f, 1.7f)
    }

    /** قلب (إعجاب). */
    val Heart = glyph("heart") {
        bodyOf(
            path {
                moveTo(12f, 20.2f)
                cubicTo(5.6f, 15.4f, 3.2f, 12.2f, 3.2f, 8.8f)
                cubicTo(3.2f, 6f, 5.3f, 4f, 7.9f, 4f)
                cubicTo(9.6f, 4f, 11.1f, 4.9f, 12f, 6.4f)
                cubicTo(12.9f, 4.9f, 14.4f, 4f, 16.1f, 4f)
                cubicTo(18.7f, 4f, 20.8f, 6f, 20.8f, 8.8f)
                cubicTo(20.8f, 12.2f, 18.4f, 15.4f, 12f, 20.2f)
                close()
            },
        )
    }

    /** نجمة (تقييم) بزوايا مدوّرة. */
    val Star = glyph("star") {
        bodyOf(path { polygon(starPoints(12f, 12.6f, 9.2f, 4.3f), 1.4f) })
    }

    /** موازن الصوت. */
    val Equalizer = glyph("equalizer") {
        bar(3.6f, 10f, 3.2f, 10f, 1.6f, Tone.Soft, softAlpha())
        bar(8.6f, 4f, 3.2f, 16f, 1.6f)
        bar(13.6f, 7f, 3.2f, 13f, 1.6f, Tone.Soft, softAlpha())
        bar(18.6f, 11f, 3.2f, 9f, 1.6f)
    }

    /** معلومات. */
    val Info = glyph("info") {
        softBody(path { circle(12f, 12f, 9.2f) })
        line(12f, 11f, 12f, 16.6f)
        dot(12f, 7.8f, 1.1f)
    }

    /** قائمة التشغيل: ثلاث نقاط وثلاثة خطوط. */
    val Playlist = glyph("playlist") {
        dot(4.8f, 6.5f, 1.3f)
        dot(4.8f, 12f, 1.3f)
        dot(4.8f, 17.5f, 1.3f)
        line(9f, 6.5f, 20.4f, 6.5f)
        line(9f, 12f, 20.4f, 12f)
        line(9f, 17.5f, 20.4f, 17.5f)
    }

    /** قائمة بنوتة (إضافة للقائمة أو الطابور). */
    val PlaylistNote = glyph("playlist_note") {
        line(3.6f, 6.4f, 14.6f, 6.4f)
        line(3.6f, 11.6f, 14.6f, 11.6f)
        line(3.6f, 16.8f, 10.4f, 16.8f)
        dot(16.2f, 17.4f, 2.3f)
        stem(18.5f, 8.4f, 17.4f)
        stroke(path {
            moveTo(18.5f, 8.4f)
            quadTo(19.4f, 10.8f, 21.6f, 11f)
        }, widthUnits = 1.5f)
    }

    /** كل أيقونات الموسيقى، للسجل الموحّد لاحقاً. */
    val All: List<Glyph> = listOf(
        AlbumFrame, AlbumLines, AlbumDisc, Note, NoteDouble, NoteAdd, NoteDisc, NoteOff,
        Headphones, HeadphonesWave, Mic, MicAlert, MicOff, SearchList,
        Disc, Heart, Star, Equalizer, Info, Playlist, PlaylistNote,
    )
}

/* ───────────── أدوات مشتركة داخل الملف ───────────── */

/** شفافية الجزء الخافت: تُطبَّق في Solid فقط، لأن Duotone يملك لوناً ثانوياً. */
private fun GlyphCanvas.softAlpha(): Float =
    if (spec.style == IconStyle.Solid) GlyphDefaults.SoftAlpha else 1f

/** جسم خافت: خط في Line، وتعبئة خافتة في غيره. */
private fun GlyphCanvas.softBody(p: Path) {
    when (spec.style) {
        IconStyle.Line -> stroke(p)
        IconStyle.Solid -> fill(p, Tone.Soft, GlyphDefaults.SoftAlpha)
        IconStyle.Duotone -> fill(p, Tone.Soft)
    }
}

/** جسم مغلق: خط، تعبئة، أو تعبئة خافتة مع حدّ قوي. */
private fun GlyphCanvas.bodyOf(p: Path) {
    when (spec.style) {
        IconStyle.Line -> stroke(p)
        IconStyle.Solid -> fill(p)
        IconStyle.Duotone -> {
            fill(p, Tone.Soft)
            stroke(p)
        }
    }
}

/** ساق النوتة: خط في Line، وشريط ممتلئ في غيره. */
private fun GlyphCanvas.stem(x: Float, y1: Float, y2: Float) {
    if (spec.style == IconStyle.Line) {
        line(x, y1, x, y2)
    } else {
        bar(x - 1.1f, y1, 2.2f, y2 - y1, 1.1f)
    }
}

private fun GlyphCanvas.singleNote() {
    softBody(
        path {
            polygon(
                listOf(Offset(10.7f, 4.6f), Offset(17.6f, 4.6f), Offset(17.6f, 10.4f), Offset(10.7f, 10.4f)),
                1f,
            )
        },
    )
    dot(8f, 17.8f, 3f)
    stem(10.7f, 4.6f, 17.8f)
}

private fun GlyphCanvas.headphonesBase() {
    stroke(
        path { arc(12f, 12.4f, 8.4f, 180f, 180f) },
        tone = Tone.Soft,
        alpha = softAlpha(),
    )
    bodyOf(path { rrect(3.2f, 12.4f, 4.8f, 8f, 2.4f) })
    bodyOf(path { rrect(16f, 12.4f, 4.8f, 8f, 2.4f) })
}

private fun GlyphCanvas.micBase() {
    softBody(path { rrect(8.6f, 3.2f, 6.8f, 11.8f, 3.4f) })
    stroke(path { arc(12f, 10.8f, 7.4f, 0f, 180f) })
    line(12f, 18.2f, 12f, 21f)
}

private fun starPoints(cx: Float, cy: Float, outer: Float, inner: Float): List<Offset> =
    List(10) { i ->
        val a = -PI / 2.0 + i * PI / 5.0
        val r = if (i % 2 == 0) outer else inner
        Offset(cx + r * cos(a).toFloat(), cy + r * sin(a).toFloat())
    }

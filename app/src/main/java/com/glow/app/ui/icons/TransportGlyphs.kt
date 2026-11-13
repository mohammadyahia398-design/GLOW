package com.glow.app.ui.icons

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/*
 * أيقونات التحكّم بالتشغيل (الملف 2 من 4).
 *
 * تعتمد على IconEngine.kt فقط. كل أيقونة تتبع النمط الحالي (خطّي، ممتلئ،
 * ثنائي النغمة) تلقائياً. الأسهم والأقواس خطوط في كل الأنماط.
 *
 * الاستعمال:
 *     GlowGlyph(TransportGlyphs.Play, tint = color, size = 28.dp)
 */

object TransportGlyphs {

    /** تشغيل. لا تنعكس في RTL لأنها رمز عالمي. */
    val Play = glyph("play") {
        body(path { polygon(listOf(Offset(7.6f, 5f), Offset(7.6f, 19f), Offset(19f, 12f)), 2f) })
    }

    /** إيقاف مؤقت. */
    val Pause = glyph("pause") {
        body(path { rrect(5.8f, 5f, 4.2f, 14f, 1.5f) })
        body(path { rrect(14f, 5f, 4.2f, 14f, 1.5f) })
    }

    /** المقطع التالي. تنعكس في RTL. */
    val Next = glyph("next", directional = true) { skipForward() }

    /** المقطع السابق. تنعكس في RTL. */
    val Previous = glyph("previous", directional = true) {
        scaled(-1f, 1f, 12f, 12f) { skipForward() }
    }

    /** تقديم سريع. تنعكس في RTL. */
    val FastForward = glyph("fast_forward", directional = true) { fastForward() }

    /** ترجيع سريع. تنعكس في RTL. */
    val Rewind = glyph("rewind", directional = true) {
        scaled(-1f, 1f, 12f, 12f) { fastForward() }
    }

    /** إيقاف كامل. */
    val Stop = glyph("stop") {
        body(path { rrect(6f, 6f, 12f, 12f, 2.8f) })
    }

    /** تسجيل. */
    val Record = glyph("record") {
        body(path { circle(12f, 12f, 8.2f) })
        if (spec.style != IconStyle.Solid) dot(12f, 12f, 3.6f)
    }

    /** عشوائي. */
    val Shuffle = glyph("shuffle") {
        stroke(path {
            moveTo(3.5f, 17.5f)
            lineTo(6f, 17.5f)
            cubicTo(10f, 17.5f, 11f, 6.5f, 15f, 6.5f)
            lineTo(18.4f, 6.5f)
        })
        stroke(
            path {
                moveTo(3.5f, 6.5f)
                lineTo(6f, 6.5f)
                cubicTo(10f, 6.5f, 11f, 17.5f, 15f, 17.5f)
                lineTo(18.4f, 17.5f)
            },
            tone = Tone.Soft,
        )
        chevron(19.8f, 6.5f, 1f, 0f, 3f)
        chevron(19.8f, 17.5f, 1f, 0f, 3f)
    }

    /** تكرار الكل. */
    val Repeat = glyph("repeat") { repeatLoop() }

    /** تكرار مقطع واحد. */
    val RepeatOne = glyph("repeat_one") {
        repeatLoop()
        stroke(
            path {
                moveTo(10.5f, 11f)
                lineTo(12.3f, 9.7f)
                lineTo(12.3f, 14.5f)
            },
            widthUnits = 1.5f,
        )
    }

    /** مكبّر الصوت مع كتم (X). */
    val VolumeMute = glyph("volume_mute") {
        speakerBody()
        line(16.2f, 9.6f, 21f, 14.4f)
        line(21f, 9.6f, 16.2f, 14.4f)
    }

    /** مكبّر الصوت بموجة واحدة (مستوى منخفض). */
    val VolumeLow = glyph("volume_low") {
        speakerBody()
        stroke(path { arc(12.4f, 12f, 4.6f, -45f, 90f) })
    }

    /** مكبّر الصوت بموجتين (مستوى عالٍ). */
    val VolumeHigh = glyph("volume_high") {
        speakerBody()
        stroke(path { arc(12.4f, 12f, 4.6f, -45f, 90f) })
        stroke(path { arc(12.4f, 12f, 8.2f, -45f, 90f) }, tone = Tone.Soft)
    }

    /** يختار أيقونة الصوت المناسبة لمستوى 0..1. */
    fun volumeFor(level: Float, muted: Boolean = false): Glyph = when {
        muted || level <= 0f -> VolumeMute
        level < 0.5f -> VolumeLow
        else -> VolumeHigh
    }

    /** إعادة المقطع من البداية (سهم دائري عكس عقارب الساعة). */
    val Replay = glyph("replay") {
        scaled(-1f, 1f, 12f, 12f) { loopArrow() }
    }

    /** تقديم 10 ثوانٍ. */
    val SeekForward10 = glyph("seek_forward_10") {
        loopArrow()
        seekDigits()
    }

    /** إرجاع 10 ثوانٍ. */
    val SeekBack10 = glyph("seek_back_10") {
        scaled(-1f, 1f, 12f, 12f) { loopArrow() }
        seekDigits()
    }

    /** كل أيقونات التحكّم، للسجل الموحّد لاحقاً. */
    val All: List<Glyph> = listOf(
        Play, Pause, Next, Previous, FastForward, Rewind, Stop, Record,
        Shuffle, Repeat, RepeatOne, VolumeMute, VolumeLow, VolumeHigh,
        Replay, SeekForward10, SeekBack10,
    )
}

/* ───────────── أدوات مشتركة داخل الملف ───────────── */

/** يرسم جسماً مغلقاً حسب النمط: خط، تعبئة، أو تعبئة خافتة مع حدّ قوي. */
private fun GlyphCanvas.body(p: Path, tone: Tone = Tone.Strong, alpha: Float = 1f) {
    when (spec.style) {
        IconStyle.Line -> stroke(p, tone, alpha)
        IconStyle.Solid -> fill(p, tone, alpha)
        IconStyle.Duotone -> {
            fill(p, Tone.Soft, alpha)
            if (tone == Tone.Strong) stroke(p, Tone.Strong, alpha)
        }
    }
}

/** عمود رأسي: خط في النمط الخطّي، وشريط ممتلئ في غيره. */
private fun GlyphCanvas.rod(x: Float, y1: Float, y2: Float) {
    if (spec.style == IconStyle.Line) {
        line(x, y1, x, y2)
    } else {
        bar(x - 1.3f, y1, 2.6f, y2 - y1, 1.3f)
    }
}

/** رأس سهم (شيفرون) بقمّته عند (x, y) واتجاهه (dx, dy) المنظَّم. */
private fun GlyphCanvas.chevron(x: Float, y: Float, dx: Float, dy: Float, s: Float) {
    val nx = -dy
    val ny = dx
    stroke(path {
        moveTo(x - dx * s + nx * s * 0.9f, y - dy * s + ny * s * 0.9f)
        lineTo(x, y)
        lineTo(x - dx * s - nx * s * 0.9f, y - dy * s - ny * s * 0.9f)
    })
}

private fun GlyphCanvas.skipForward() {
    body(path { polygon(listOf(Offset(5.2f, 5.8f), Offset(5.2f, 18.2f), Offset(15.4f, 12f)), 1.8f) })
    rod(18.8f, 5.8f, 18.2f)
}

private fun GlyphCanvas.fastForward() {
    body(path { polygon(listOf(Offset(3f, 6.6f), Offset(3f, 17.4f), Offset(11f, 12f)), 1.6f) })
    body(path { polygon(listOf(Offset(11.4f, 6.6f), Offset(11.4f, 17.4f), Offset(19.6f, 12f)), 1.6f) })
}

/** حلقة التكرار بسهمين متقابلين. */
private fun GlyphCanvas.repeatLoop() {
    stroke(path {
        moveTo(5f, 12f)
        lineTo(5f, 10.5f)
        quadTo(5f, 7.5f, 8f, 7.5f)
        lineTo(19f, 7.5f)
    })
    chevron(19.6f, 7.5f, 1f, 0f, 3f)
    stroke(path {
        moveTo(19f, 12f)
        lineTo(19f, 13.5f)
        quadTo(19f, 16.5f, 16f, 16.5f)
        lineTo(5f, 16.5f)
    })
    chevron(4.4f, 16.5f, -1f, 0f, 3f)
}

/** سهم دائري مع اتجاه عقارب الساعة، وفتحة في الأعلى. */
private fun GlyphCanvas.loopArrow() {
    val cx = 12f
    val cy = 12.5f
    val r = 7.2f
    stroke(path { arc(cx, cy, r, -60f, 300f) })
    val a = 240.0 * PI / 180.0
    val ex = cx + r * cos(a).toFloat()
    val ey = cy + r * sin(a).toFloat()
    val dx = (-sin(a)).toFloat()
    val dy = cos(a).toFloat()
    chevron(ex, ey, dx, dy, 3.2f)
}

/** الرقم 10 داخل حلقة التقديم والإرجاع. */
private fun GlyphCanvas.seekDigits() {
    stroke(
        path {
            moveTo(9.6f, 11.2f)
            lineTo(10.9f, 10.4f)
            lineTo(10.9f, 14.9f)
        },
        widthUnits = 1.4f,
    )
    stroke(path { rrect(12.8f, 10.4f, 2.8f, 4.5f, 1.4f) }, widthUnits = 1.4f)
}

/** جسم مكبّر الصوت دون موجات. */
private fun GlyphCanvas.speakerBody() {
    body(
        path {
            polygon(
                listOf(
                    Offset(4f, 9.6f),
                    Offset(7.6f, 9.6f),
                    Offset(12.4f, 5.6f),
                    Offset(12.4f, 18.4f),
                    Offset(7.6f, 14.4f),
                    Offset(4f, 14.4f),
                ),
                1.2f,
            )
        },
    )
}

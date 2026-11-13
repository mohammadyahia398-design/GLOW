package com.glow.app.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.glow.app.ui.icons.Glyph
import com.glow.app.ui.icons.GlowGlyph
import com.glow.app.ui.icons.MusicGlyphs
import com.glow.app.ui.icons.TransportGlyphs
import kotlin.math.cos
import kotlin.math.sin

enum class IconKind {
    Back, Down, Search, Shuffle, Repeat, RepeatOne, Play, Pause, Next, Prev, Note, More, Settings,
    Menu, Album, Artist, Heart, Playlist, Chevron, Check, Speed, Moon, Equalizer, Timer, Refresh, Info,
}

/** أيقونات مرسومة بـ Canvas. الاتجاهية منها (رجوع/تالٍ/سابق) تنعكس في RTL. */
@Composable
fun GlowIcon(kind: IconKind, tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    val glyph = kind.newGlyph()
    if (glyph != null) {
        GlowGlyph(glyph, modifier, tint, size)
        return
    }
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val mirror = rtl && (kind == IconKind.Back || kind == IconKind.Next || kind == IconKind.Prev || kind == IconKind.Chevron)
    Canvas(modifier.size(size)) {
        scale(if (mirror) -1f else 1f, 1f) { paintIcon(kind, tint) }
    }
}

/** الأيقونات التي لها بديل في نظام الأيقونات الجديد. الباقي يبقى بالرسم القديم. */
private fun IconKind.newGlyph(): Glyph? = when (this) {
    IconKind.Play -> TransportGlyphs.Play
    IconKind.Pause -> TransportGlyphs.Pause
    IconKind.Next -> TransportGlyphs.Next
    IconKind.Prev -> TransportGlyphs.Previous
    IconKind.Shuffle -> TransportGlyphs.Shuffle
    IconKind.Repeat -> TransportGlyphs.Repeat
    IconKind.RepeatOne -> TransportGlyphs.RepeatOne
    IconKind.Note -> MusicGlyphs.Note
    IconKind.Heart -> MusicGlyphs.Heart
    IconKind.Playlist -> MusicGlyphs.Playlist
    IconKind.Equalizer -> MusicGlyphs.Equalizer
    IconKind.Info -> MusicGlyphs.Info
    IconKind.Album -> MusicGlyphs.AlbumFrame
    else -> null
}

private fun DrawScope.paintIcon(kind: IconKind, c: Color) {
    val w = size.width
    val stroke = Stroke(width = w * 0.09f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    fun p(x: Float, y: Float) = Offset(x * w, y * w)
    fun line(pts: List<Offset>) {
        val path = Path().apply {
            moveTo(pts[0].x, pts[0].y)
            for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
        }
        drawPath(path, c, style = stroke)
    }
    fun tri(a: Offset, b: Offset, d: Offset) {
        val path = Path().apply {
            moveTo(a.x, a.y); lineTo(b.x, b.y); lineTo(d.x, d.y); close()
        }
        drawPath(path, c)
    }
    when (kind) {
        IconKind.Back -> line(listOf(p(.65f, .2f), p(.3f, .5f), p(.65f, .8f)))
        IconKind.Down -> line(listOf(p(.2f, .35f), p(.5f, .65f), p(.8f, .35f)))
        IconKind.Search -> {
            drawCircle(c, radius = w * .25f, center = p(.43f, .43f), style = stroke)
            line(listOf(p(.62f, .62f), p(.85f, .85f)))
        }
        IconKind.Shuffle -> {
            line(listOf(p(.1f, .3f), p(.35f, .3f), p(.65f, .7f), p(.78f, .7f)))
            line(listOf(p(.1f, .7f), p(.35f, .7f), p(.65f, .3f), p(.78f, .3f)))
            tri(p(.78f, .18f), p(.95f, .3f), p(.78f, .42f))
            tri(p(.78f, .58f), p(.95f, .7f), p(.78f, .82f))
        }
        IconKind.Repeat, IconKind.RepeatOne -> {
            drawRoundRect(
                color = c,
                topLeft = p(.12f, .28f),
                size = Size(w * .76f, w * .44f),
                cornerRadius = CornerRadius(w * .15f),
                style = stroke,
            )
            tri(p(.62f, .12f), p(.82f, .28f), p(.62f, .44f))
            if (kind == IconKind.RepeatOne) drawCircle(c, radius = w * .07f, center = p(.5f, .5f))
        }
        IconKind.Play -> tri(p(.3f, .2f), p(.8f, .5f), p(.3f, .8f))
        IconKind.Pause -> {
            drawRoundRect(c, p(.25f, .2f), Size(w * .18f, w * .6f), CornerRadius(w * .04f))
            drawRoundRect(c, p(.57f, .2f), Size(w * .18f, w * .6f), CornerRadius(w * .04f))
        }
        IconKind.Next -> {
            tri(p(.2f, .2f), p(.65f, .5f), p(.2f, .8f))
            drawRoundRect(c, p(.7f, .2f), Size(w * .1f, w * .6f), CornerRadius(w * .03f))
        }
        IconKind.Prev -> scale(-1f, 1f) { paintIcon(IconKind.Next, c) }
        IconKind.More -> {
            drawCircle(c, radius = w * .07f, center = p(.5f, .22f))
            drawCircle(c, radius = w * .07f, center = p(.5f, .5f))
            drawCircle(c, radius = w * .07f, center = p(.5f, .78f))
        }
        IconKind.Settings -> {
            for (i in 0 until 8) {
                val a = i * Math.PI / 4
                val ca = cos(a).toFloat()
                val sa = sin(a).toFloat()
                line(listOf(p(.5f + .3f * ca, .5f + .3f * sa), p(.5f + .42f * ca, .5f + .42f * sa)))
            }
            drawCircle(c, radius = w * .25f, center = p(.5f, .5f), style = stroke)
            drawCircle(c, radius = w * .09f, center = p(.5f, .5f), style = stroke)
        }
        IconKind.Menu -> {
            // ثلاثة أشرطة مدوّرة، بنسب المرجع (عرض 41% وسماكة 8%).
            val barW = w * .42f
            val barH = w * .08f
            for (cy in listOf(.335f, .5f, .665f)) {
                drawRoundRect(
                    c,
                    topLeft = Offset(w * .5f - barW / 2f, w * cy - barH / 2f),
                    size = Size(barW, barH),
                    cornerRadius = CornerRadius(barH / 2f),
                )
            }
        }
        IconKind.Album -> {
            drawCircle(c, radius = w * .36f, center = p(.5f, .5f), style = stroke)
            drawCircle(c, radius = w * .09f, center = p(.5f, .5f))
        }
        IconKind.Artist -> {
            drawCircle(c, radius = w * .16f, center = p(.5f, .34f), style = stroke)
            drawArc(
                color = c,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = p(.2f, .58f),
                size = Size(w * .6f, w * .6f),
                style = stroke,
            )
        }
        IconKind.Heart -> {
            val path = Path().apply {
                moveTo(w * .5f, w * .80f)
                cubicTo(w * .20f, w * .58f, w * .12f, w * .40f, w * .22f, w * .28f)
                cubicTo(w * .32f, w * .17f, w * .45f, w * .22f, w * .5f, w * .34f)
                cubicTo(w * .55f, w * .22f, w * .68f, w * .17f, w * .78f, w * .28f)
                cubicTo(w * .88f, w * .40f, w * .80f, w * .58f, w * .5f, w * .80f)
                close()
            }
            drawPath(path, c, style = stroke)
        }
        IconKind.Playlist -> {
            line(listOf(p(.14f, .28f), p(.62f, .28f)))
            line(listOf(p(.14f, .48f), p(.62f, .48f)))
            line(listOf(p(.14f, .68f), p(.40f, .68f)))
            drawCircle(c, radius = w * .09f, center = p(.74f, .72f))
            line(listOf(p(.82f, .72f), p(.82f, .40f)))
        }
        IconKind.Chevron -> line(listOf(p(.36f, .2f), p(.66f, .5f), p(.36f, .8f)))
        IconKind.Check -> line(listOf(p(.2f, .52f), p(.42f, .72f), p(.8f, .3f)))
        IconKind.Speed -> {
            drawArc(
                color = c,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = p(.12f, .2f),
                size = Size(w * .76f, w * .76f),
                style = stroke,
            )
            line(listOf(p(.5f, .58f), p(.68f, .38f)))
            drawCircle(c, radius = w * .06f, center = p(.5f, .58f))
        }
        IconKind.Moon -> {
            val outer = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(p(.45f, .5f).minus(Offset(w * .34f, w * .34f)), Size(w * .68f, w * .68f)))
            }
            val cut = Path().apply {
                addOval(androidx.compose.ui.geometry.Rect(p(.64f, .38f).minus(Offset(w * .30f, w * .30f)), Size(w * .60f, w * .60f)))
            }
            drawPath(Path.combine(PathOperation.Difference, outer, cut), c, style = stroke)
        }
        IconKind.Equalizer -> {
            line(listOf(p(.2f, .38f), p(.2f, .62f)))
            line(listOf(p(.4f, .2f), p(.4f, .8f)))
            line(listOf(p(.6f, .3f), p(.6f, .7f)))
            line(listOf(p(.8f, .44f), p(.8f, .56f)))
        }
        IconKind.Timer -> {
            drawCircle(c, radius = w * .33f, center = p(.5f, .57f), style = stroke)
            line(listOf(p(.4f, .14f), p(.6f, .14f)))
            line(listOf(p(.5f, .57f), p(.5f, .38f)))
        }
        IconKind.Refresh -> {
            val r = w * .32f
            drawArc(
                color = c,
                startAngle = -50f,
                sweepAngle = 270f,
                useCenter = false,
                topLeft = p(.5f, .5f).minus(Offset(r, r)),
                size = Size(r * 2f, r * 2f),
                style = stroke,
            )
            val a = Math.toRadians(220.0)
            val ex = w * .5f + r * cos(a).toFloat()
            val ey = w * .5f + r * sin(a).toFloat()
            val dx = -sin(a).toFloat()
            val dy = cos(a).toFloat()
            val nx = cos(a).toFloat()
            val ny = sin(a).toFloat()
            tri(
                Offset(ex + dx * w * .14f, ey + dy * w * .14f),
                Offset(ex + nx * w * .11f, ey + ny * w * .11f),
                Offset(ex - nx * w * .11f, ey - ny * w * .11f),
            )
        }
        IconKind.Info -> {
            drawCircle(c, radius = w * .38f, center = p(.5f, .5f), style = stroke)
            drawCircle(c, radius = w * .05f, center = p(.5f, .32f))
            line(listOf(p(.5f, .46f), p(.5f, .72f)))
        }
        IconKind.Note -> {
            drawCircle(c, radius = w * .14f, center = p(.35f, .72f))
            line(listOf(p(.48f, .72f), p(.48f, .2f)))
            line(listOf(p(.48f, .2f), p(.8f, .32f)))
        }
    }
}

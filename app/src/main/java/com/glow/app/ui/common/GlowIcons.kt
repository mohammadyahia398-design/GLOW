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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

enum class IconKind { Back, Down, Search, Shuffle, Repeat, RepeatOne, Play, Pause, Next, Prev, Note, More, Settings }

/** أيقونات مرسومة بـ Canvas. الاتجاهية منها (رجوع/تالٍ/سابق) تنعكس في RTL. */
@Composable
fun GlowIcon(kind: IconKind, tint: Color, size: Dp = 24.dp, modifier: Modifier = Modifier) {
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    val mirror = rtl && (kind == IconKind.Back || kind == IconKind.Next || kind == IconKind.Prev)
    Canvas(modifier.size(size)) {
        scale(if (mirror) -1f else 1f, 1f) { paintIcon(kind, tint) }
    }
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
        IconKind.Note -> {
            drawCircle(c, radius = w * .14f, center = p(.35f, .72f))
            line(listOf(p(.48f, .72f), p(.48f, .2f)))
            line(listOf(p(.48f, .2f), p(.8f, .32f)))
        }
    }
}

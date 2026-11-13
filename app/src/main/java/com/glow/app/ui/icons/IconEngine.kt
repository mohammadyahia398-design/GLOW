package com.glow.app.ui.icons

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.isSpecified
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.glow.app.ui.glass.core.LocalGlassConfig

/*
 * محرّك أيقونات Glow.
 *
 * كل أيقونة تُرسم على شبكة 24×24 منطقية مهما كان حجمها الفعلي، فتبقى حادّة
 * وبنسب ثابتة. الملفات الثلاثة التالية (التحكّم بالتشغيل، الموسيقى والمكتبة،
 * الأيقونات المتحركة) تبني فوق هذا الملف فقط.
 */

/** نمط الرسم: خطّي، ممتلئ، أو ثنائي النغمة (جزء قوي وجزء خافت). */
enum class IconStyle { Line, Solid, Duotone }

/** درجة العنصر داخل الأيقونة. Soft يظهر خافتاً في نمط Duotone فقط. */
enum class Tone { Strong, Soft }

/** القيم الافتراضية للنظام كله. */
object GlyphDefaults {
    const val Grid = 24f
    const val StrokeUnits = 1.8f
    const val SoftAlpha = 0.38f
    val Size: Dp = 24.dp
}

/** نمط الأيقونات الافتراضي لكل ما تحته في الشجرة. */
val LocalIconStyle = compositionLocalOf { IconStyle.Line }

/** سماكة الخط الافتراضية بوحدات الشبكة (من 24). */
val LocalIconStroke = compositionLocalOf { GlyphDefaults.StrokeUnits }

/** يغيّر نمط الأيقونات وسماكتها لمحتواه كله دفعة واحدة. */
@Composable
fun ProvideIconStyle(
    style: IconStyle,
    strokeUnits: Float = GlyphDefaults.StrokeUnits,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalIconStyle provides style,
        LocalIconStroke provides strokeUnits,
        content = content,
    )
}

/** الألوان والنمط الذي تُرسم به الأيقونة في هذا الإطار. */
@Immutable
class GlyphSpec(
    val primary: Color,
    val secondary: Color,
    val style: IconStyle,
    val strokeUnits: Float,
)

/** أداة بناء مسارات بإحداثيات الشبكة (24×24) تُحوَّل تلقائياً إلى بكسلات. */
class UnitPath internal constructor(private val u: Float) {
    internal val path = Path()

    fun moveTo(x: Float, y: Float) = path.moveTo(x * u, y * u)
    fun lineTo(x: Float, y: Float) = path.lineTo(x * u, y * u)
    fun quadTo(cx: Float, cy: Float, x: Float, y: Float) =
        path.quadraticTo(cx * u, cy * u, x * u, y * u)

    fun cubicTo(x1: Float, y1: Float, x2: Float, y2: Float, x: Float, y: Float) =
        path.cubicTo(x1 * u, y1 * u, x2 * u, y2 * u, x * u, y * u)

    fun close() = path.close()

    /** قوس من الدائرة (cx, cy, r). الزاوية 0 نحو اليمين وتزيد مع عقارب الساعة. */
    fun arc(cx: Float, cy: Float, r: Float, startDeg: Float, sweepDeg: Float, moveTo: Boolean = true) {
        val rect = Rect(Offset((cx - r) * u, (cy - r) * u), Size(2f * r * u, 2f * r * u))
        path.arcTo(rect, startDeg, sweepDeg, moveTo)
    }

    fun circle(cx: Float, cy: Float, r: Float) {
        path.addOval(Rect(Offset((cx - r) * u, (cy - r) * u), Size(2f * r * u, 2f * r * u)))
    }

    fun rrect(x: Float, y: Float, w: Float, h: Float, r: Float) {
        path.addRoundRect(
            RoundRect(x * u, y * u, (x + w) * u, (y + h) * u, CornerRadius(r * u)),
        )
    }

    /** مضلّع مغلق بزوايا مدوّرة بنصف قطر [radius]؛ يُقصّ تلقائياً إن زاد عن نصف الضلع. */
    fun polygon(points: List<Offset>, radius: Float = 0f) {
        val n = points.size
        if (n < 3) return
        var started = false
        for (i in 0 until n) {
            val cur = points[i]
            val toPrev = points[(i + n - 1) % n] - cur
            val toNext = points[(i + 1) % n] - cur
            val lp = toPrev.getDistance()
            val ln = toNext.getDistance()
            if (lp < 1e-4f || ln < 1e-4f) continue
            val cut = minOf(radius, lp * 0.5f, ln * 0.5f)
            val a = cur + toPrev * (cut / lp)
            val b = cur + toNext * (cut / ln)
            if (started) lineTo(a.x, a.y) else moveTo(a.x, a.y)
            started = true
            quadTo(cur.x, cur.y, b.x, b.y)
        }
        if (started) close()
    }
}

/**
 * سطح الرسم الذي تستعمله كل أيقونة. كل العمليات بوحدات الشبكة 24×24.
 *
 * - [shape]: شكل يتبع النمط (خط في Line، تعبئة في Solid وDuotone).
 * - [stroke]: خط دائماً (أقواس وخطوط وأسهم).
 * - [fill]: تعبئة دائماً.
 */
class GlyphCanvas internal constructor(
    private val scope: DrawScope,
    val spec: GlyphSpec,
) {
    val unit: Float = scope.size.minDimension / GlyphDefaults.Grid
    private val lineStyle = Stroke(width = spec.strokeUnits * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)

    fun px(v: Float): Float = v * unit
    fun pt(x: Float, y: Float): Offset = Offset(x * unit, y * unit)

    fun path(build: UnitPath.() -> Unit): Path = UnitPath(unit).apply(build).path

    private fun colorOf(tone: Tone, alpha: Float): Color {
        val base = if (spec.style == IconStyle.Duotone && tone == Tone.Soft) spec.secondary else spec.primary
        return if (alpha >= 1f) base else base.copy(alpha = base.alpha * alpha)
    }

    fun stroke(path: Path, tone: Tone = Tone.Strong, alpha: Float = 1f, widthUnits: Float = spec.strokeUnits) {
        val style = if (widthUnits == spec.strokeUnits) {
            lineStyle
        } else {
            Stroke(width = widthUnits * unit, cap = StrokeCap.Round, join = StrokeJoin.Round)
        }
        scope.drawPath(path, colorOf(tone, alpha), style = style)
    }

    fun fill(path: Path, tone: Tone = Tone.Strong, alpha: Float = 1f) {
        scope.drawPath(path, colorOf(tone, alpha))
    }

    fun shape(path: Path, tone: Tone = Tone.Strong, alpha: Float = 1f) {
        if (spec.style == IconStyle.Line) stroke(path, tone, alpha) else fill(path, tone, alpha)
    }

    fun dot(cx: Float, cy: Float, r: Float, tone: Tone = Tone.Strong, alpha: Float = 1f) {
        scope.drawCircle(colorOf(tone, alpha), radius = r * unit, center = pt(cx, cy))
    }

    fun ring(cx: Float, cy: Float, r: Float, tone: Tone = Tone.Strong, alpha: Float = 1f) {
        scope.drawCircle(colorOf(tone, alpha), radius = r * unit, center = pt(cx, cy), style = lineStyle)
    }

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, tone: Tone = Tone.Strong, alpha: Float = 1f) {
        scope.drawLine(
            color = colorOf(tone, alpha),
            start = pt(x1, y1),
            end = pt(x2, y2),
            strokeWidth = spec.strokeUnits * unit,
            cap = StrokeCap.Round,
        )
    }

    /** مستطيل مدوّر ممتلئ دائماً (أشرطة الإيقاف المؤقت والموازن). */
    fun bar(x: Float, y: Float, w: Float, h: Float, r: Float, tone: Tone = Tone.Strong, alpha: Float = 1f) {
        scope.drawRoundRect(
            color = colorOf(tone, alpha),
            topLeft = pt(x, y),
            size = Size(w * unit, h * unit),
            cornerRadius = CornerRadius(r * unit),
        )
    }

    fun rotated(degrees: Float, cx: Float, cy: Float, block: () -> Unit) {
        scope.withTransform({ rotate(degrees, pt(cx, cy)) }) { block() }
    }

    fun scaled(sx: Float, sy: Float, cx: Float, cy: Float, block: () -> Unit) {
        scope.withTransform({ scale(sx, sy, pt(cx, cy)) }) { block() }
    }

    fun shifted(dx: Float, dy: Float, block: () -> Unit) {
        scope.withTransform({ translate(dx * unit, dy * unit) }) { block() }
    }
}

/**
 * أيقونة واحدة. [painter] يستقبل سطح الرسم وقيمة التقدّم t (0..1) للأيقونات المتحركة.
 * [directional] تعني أن الأيقونة تنعكس في RTL (أسهم وتالٍ وسابق).
 */
@Immutable
class Glyph(
    val name: String,
    val directional: Boolean = false,
    private val painter: GlyphCanvas.(t: Float) -> Unit,
) {
    internal fun paint(canvas: GlyphCanvas, t: Float) = canvas.painter(t)
}

fun glyph(
    name: String,
    directional: Boolean = false,
    painter: GlyphCanvas.(t: Float) -> Unit,
): Glyph = Glyph(name, directional, painter)

private val ZeroProgress: () -> Float = { 0f }

/**
 * يعرض [glyph]. [progress] دالة تُقرأ في مرحلة الرسم فقط، فتتحرك الأيقونة
 * دون إعادة تركيب. اللون الخافت يُشتق من [tint] ما لم يُحدَّد [secondaryTint].
 */
@Composable
fun GlowGlyph(
    glyph: Glyph,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    size: Dp = GlyphDefaults.Size,
    style: IconStyle = LocalIconStyle.current,
    secondaryTint: Color = Color.Unspecified,
    strokeUnits: Float = LocalIconStroke.current,
    progress: () -> Float = ZeroProgress,
    contentDescription: String? = null,
) {
    val mirror = LocalLayoutDirection.current == LayoutDirection.Rtl && glyph.directional
    val soft = if (secondaryTint.isSpecified) {
        secondaryTint
    } else {
        tint.copy(alpha = tint.alpha * GlyphDefaults.SoftAlpha)
    }
    val spec = remember(tint, soft, style, strokeUnits) { GlyphSpec(tint, soft, style, strokeUnits) }
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            this.role = Role.Image
        }
    } else {
        Modifier
    }
    Canvas(modifier.size(size).then(semanticsModifier)) {
        val canvas = GlyphCanvas(this, spec)
        if (mirror) {
            scale(-1f, 1f) { glyph.paint(canvas, progress()) }
        } else {
            glyph.paint(canvas, progress())
        }
    }
}

/** حركة نابضة لقيمة التقدّم، وتصبح فورية عند تفعيل «تقليل الحركة». */
@Composable
fun animateGlyphProgress(target: Float, label: String = "glyphProgress"): State<Float> {
    val reduceMotion = LocalGlassConfig.current.reduceMotion
    val spec: AnimationSpec<Float> = if (reduceMotion) {
        snap()
    } else {
        spring(dampingRatio = 0.62f, stiffness = 520f)
    }
    return animateFloatAsState(targetValue = target, animationSpec = spec, label = label)
}

/** استيفاء خطي بين قيمتين. */
fun mix(a: Float, b: Float, t: Float): Float = a + (b - a) * t

fun mix(a: Offset, b: Offset, t: Float): Offset = Offset(mix(a.x, b.x, t), mix(a.y, b.y, t))

/** استيفاء نقطة بنقطة بين قائمتين متساويتي الطول (تحوّل المضلّعات). */
fun mixPoints(a: List<Offset>, b: List<Offset>, t: Float): List<Offset> =
    List(minOf(a.size, b.size)) { mix(a[it], b[it], t) }

/** يحوّل t إلى 0..1 داخل النافذة [from, to]، لتجزئة الحركة إلى مراحل. */
fun window(t: Float, from: Float, to: Float): Float =
    if (to <= from) (if (t >= to) 1f else 0f) else ((t - from) / (to - from)).coerceIn(0f, 1f)

/** تسارع ثم تباطؤ ناعم. */
fun easeInOutCubic(t: Float): Float {
    val x = t.coerceIn(0f, 1f)
    return if (x < 0.5f) 4f * x * x * x else 1f - (-2f * x + 2f).let { it * it * it } / 2f
}

/** يتجاوز الهدف قليلاً ثم يستقر، للنبضات المرحة (القلب والنجمة). */
fun easeOutBack(t: Float, overshoot: Float = 1.70158f): Float {
    val x = t.coerceIn(0f, 1f) - 1f
    return 1f + (overshoot + 1f) * x * x * x + overshoot * x * x
}

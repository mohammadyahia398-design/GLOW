package com.glow.app.ui.glass.preview

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.glow.app.ui.glass.components.GlassBarItem
import com.glow.app.ui.glass.components.GlassButton
import com.glow.app.ui.glass.components.GlassCard
import com.glow.app.ui.glass.components.GlassFloatingBar
import com.glow.app.ui.glass.components.GlassIconButton
import com.glow.app.ui.glass.components.GlassMiniPlayerPill
import com.glow.app.ui.glass.components.GlassSlider
import com.glow.app.ui.glass.core.GlassConfig
import com.glow.app.ui.glass.core.GlassTier
import com.glow.app.ui.glass.core.ProvideGlassConfig
import com.glow.app.ui.glass.core.rememberGlassTier
import com.glow.app.ui.glass.core.rememberSystemReduceMotion
import com.glow.app.ui.glass.surfaces.glassBackdropSource
import com.glow.app.ui.glass.surfaces.rememberGlassBackdrop

private val Charcoal = Color(0xFF0D0E12)
private val Amber = Color(0xFFFF9F43)
private val Coral = Color(0xFFFF6B6B)
private val TwilightBlue = Color(0xFF4A69BD)
private val Ivory = Color(0xFFF5F6FA)
private val Muted = Color(0xFF8C92A4)

/**
 * معرض بصري لاختبار نظام الزجاج على جهاز فعلي أو في المعاينة.
 *
 * يعرض خلفية ملوّنة متحركة مع خطوط رفيعة (تُظهر الانكسار عند الحواف) وفوقها
 * كل المكونات. ثلاثة أزرار في الأعلى تحدد سقف المستوى (Liquid وFrosted وFallback)
 * لمقارنة المستويات على الجهاز نفسه. السقف لا يرفع المستوى فوق ما يدعمه الجهاز.
 *
 * ملاحظة: معاينة Android Studio لا تدعم RenderEffect وAGSL غالباً،
 * فالمقارنة الحقيقية تكون على جهاز أو محاكٍ API 33+.
 */
@Composable
fun GlassPreviewGallery(modifier: Modifier = Modifier) {
    val deviceTier = rememberGlassTier()
    val reduceMotion = rememberSystemReduceMotion()
    var ceiling by remember { mutableStateOf(GlassTier.Liquid) }

    val tier = if (ceiling.ordinal < deviceTier.ordinal) ceiling else deviceTier
    val config = remember(tier, reduceMotion) {
        GlassConfig(tier = tier, reduceMotion = reduceMotion)
    }

    ProvideGlassConfig(config) {
        GalleryContent(
            activeTier = tier,
            ceiling = ceiling,
            onCeilingChange = { ceiling = it },
            animateBackground = !reduceMotion,
            modifier = modifier,
        )
    }
}

@Composable
private fun GalleryContent(
    activeTier: GlassTier,
    ceiling: GlassTier,
    onCeilingChange: (GlassTier) -> Unit,
    animateBackground: Boolean,
    modifier: Modifier = Modifier,
) {
    val backdrop = rememberGlassBackdrop()
    var playing by remember { mutableStateOf(true) }
    var progress by remember { mutableFloatStateOf(0.35f) }
    var volume by remember { mutableFloatStateOf(0.6f) }
    var selected by remember { mutableStateOf("home") }

    val items = remember {
        listOf(
            GlassBarItem("home", "الرئيسية") { sel -> BarIconPlaceholder(sel) },
            GlassBarItem("library", "المكتبة") { sel -> BarIconPlaceholder(sel) },
            GlassBarItem("search", "البحث") { sel -> BarIconPlaceholder(sel) },
            GlassBarItem("settings", "الإعدادات") { sel -> BarIconPlaceholder(sel) },
        )
    }

    Box(modifier.fillMaxSize()) {
        // المصدر: خلفية حيّة. الأسطح الزجاجية إخوة له وفوقه، لا بداخله.
        GalleryBackground(
            animate = animateBackground,
            modifier = Modifier
                .matchParentSize()
                .glassBackdropSource(backdrop),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 190.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "معرض الزجاج",
                color = Ivory,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            )
            Text(
                text = "المستوى الفعلي: ${activeTier.name}",
                color = Muted,
                fontSize = 13.sp,
                letterSpacing = 0.sp,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (tier in listOf(GlassTier.Liquid, GlassTier.Frosted, GlassTier.Fallback)) {
                    GlassButton(
                        onClick = { onCeilingChange(tier) },
                        backdrop = backdrop,
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = tier.name,
                            color = if (tier == ceiling) Amber else Ivory,
                            fontSize = 13.sp,
                            letterSpacing = 0.sp,
                            maxLines = 1,
                        )
                    }
                }
            }

            GlassCard(modifier = Modifier.fillMaxWidth(), backdrop = backdrop) {
                Text(
                    text = "بطاقة ساكنة",
                    color = Ivory,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                )
                Text(
                    text = "الزجاج هنا يُموَّه ويُكسَر عند الحواف بحسب مستوى الجهاز.",
                    color = Muted,
                    fontSize = 13.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                backdrop = backdrop,
                onClick = { playing = !playing },
                onClickLabel = "تبديل التشغيل",
            ) {
                Text(
                    text = "بطاقة تفاعلية",
                    color = Ivory,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.sp,
                )
                Text(
                    text = "المس البطاقة: يتبع الضوء إصبعك ويصغر السطح قليلاً.",
                    color = Muted,
                    fontSize = 13.sp,
                    letterSpacing = 0.sp,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                GlassButton(onClick = { progress = 0f }, backdrop = backdrop) {
                    Text("إعادة", color = Ivory, fontSize = 14.sp, letterSpacing = 0.sp)
                }
                GlassIconButton(
                    onClick = { playing = !playing },
                    contentDescription = if (playing) "إيقاف مؤقت" else "تشغيل",
                    backdrop = backdrop,
                ) {
                    Box(
                        Modifier
                            .size(14.dp)
                            .background(Ivory, CircleShape),
                    )
                }
            }

            GlassSlider(
                value = volume,
                onValueChange = { volume = it },
                backdrop = backdrop,
                contentDescription = "مستوى الصوت",
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            GlassMiniPlayerPill(
                title = "اسم المقطوعة",
                subtitle = "اسم الفنان",
                isPlaying = playing,
                progress = { progress },
                onTogglePlay = { playing = !playing },
                onClick = {},
                onNext = { progress = 0f },
                backdrop = backdrop,
                playLabel = if (playing) "إيقاف مؤقت" else "تشغيل",
                nextLabel = "التالي",
                modifier = Modifier.fillMaxWidth(),
            )
            GlassFloatingBar(
                items = items,
                selectedId = selected,
                onSelect = { selected = it },
                backdrop = backdrop,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

/** أيقونة مؤقتة للمعاينة فقط؛ الأيقونات الحقيقية تأتي مع التنقل لاحقاً. */
@Composable
private fun BarIconPlaceholder(selected: Boolean) {
    Box(
        Modifier
            .size(20.dp)
            .border(2.dp, if (selected) Ivory else Muted, CircleShape),
    )
}

@Composable
private fun GalleryBackground(animate: Boolean, modifier: Modifier = Modifier) {
    val drift: State<Float>? = if (animate) {
        rememberInfiniteTransition(label = "galleryDrift").animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 14_000, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse,
            ),
            label = "galleryDriftValue",
        )
    } else {
        null
    }

    Canvas(modifier.background(Charcoal)) {
        // القراءة هنا (أثناء الرسم) لا أثناء التركيب.
        val t = drift?.value ?: 0.5f
        val w = size.width
        val h = size.height

        fun blob(color: Color, center: Offset, radius: Float) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.85f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }

        blob(Amber, Offset(w * (0.20f + 0.10f * t), h * 0.18f), w * 0.55f)
        blob(Coral, Offset(w * (0.85f - 0.12f * t), h * 0.45f), w * 0.50f)
        blob(TwilightBlue, Offset(w * 0.30f, h * (0.80f - 0.10f * t)), w * 0.60f)

        // خطوط رفيعة تُظهر الانكسار والتمويه تحت الزجاج.
        val spacing = 40.dp.toPx()
        var x = 0f
        while (x < w) {
            drawLine(
                color = Color.White.copy(alpha = 0.07f),
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 2.dp.toPx(),
            )
            x += spacing
        }
    }
}

@Preview(name = "Glass gallery", showBackground = true, backgroundColor = 0xFF0D0E12, widthDp = 400, heightDp = 860)
@Composable
private fun GlassPreviewGalleryPreview() {
    GlassPreviewGallery()
}

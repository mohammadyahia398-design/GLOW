package com.glow.app.ui.nowplaying

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.horizontalDrag
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/** شريط تقدم رفيع بمقبض دائري. يبقى من اليسار لليمين دائماً، كما في مشغّلات الموسيقى. */
@Composable
fun ArtSeekBar(
    value: Float,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    color: Color,
    description: String,
    modifier: Modifier = Modifier,
) {
    val currentChange by rememberUpdatedState(onValueChange)
    val currentFinished by rememberUpdatedState(onValueChangeFinished)
    val fraction = if (value.isNaN()) 0f else value.coerceIn(0f, 1f)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Canvas(
            modifier
                .fillMaxWidth()
                .height(36.dp)
                .semantics {
                    contentDescription = description
                    progressBarRangeInfo = ProgressBarRangeInfo(fraction, 0f..1f)
                    setProgress { target ->
                        currentChange(target.coerceIn(0f, 1f))
                        currentFinished()
                        true
                    }
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val width = size.width.toFloat().coerceAtLeast(1f)
                        currentChange((down.position.x / width).coerceIn(0f, 1f))
                        down.consume()
                        horizontalDrag(down.id) { change ->
                            currentChange((change.position.x / width).coerceIn(0f, 1f))
                            change.consume()
                        }
                        currentFinished()
                    }
                },
        ) {
            val trackHeight = 3.dp.toPx()
            val thumbRadius = 8.dp.toPx()
            val cy = size.height / 2f
            val x = thumbRadius + (size.width - 2f * thumbRadius) * fraction
            val corner = CornerRadius(trackHeight / 2f)

            drawRoundRect(
                color = color.copy(alpha = 0.30f),
                topLeft = Offset(0f, cy - trackHeight / 2f),
                size = Size(size.width, trackHeight),
                cornerRadius = corner,
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(0f, cy - trackHeight / 2f),
                size = Size(x, trackHeight),
                cornerRadius = corner,
            )
            drawCircle(color = color, radius = thumbRadius, center = Offset(x, cy))
        }
    }
}

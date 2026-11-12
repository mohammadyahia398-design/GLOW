package com.glow.app.ui.common

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.LruCache
import android.util.Size
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.glow.app.media.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private val cache = LruCache<String, Bitmap>(48)
private val missing: MutableSet<String> = java.util.Collections.synchronizedSet(HashSet())

private fun loadArt(context: Context, track: Track, px: Int): Bitmap? {
    val key = "${track.albumId}:$px"
    cache.get(key)?.let { return it }
    if (key in missing) return null
    val bmp = runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            context.contentResolver.loadThumbnail(track.uri, Size(px, px), null)
        } else {
            val art = ContentUris.withAppendedId(
                Uri.parse("content://media/external/audio/albumart"),
                track.albumId,
            )
            context.contentResolver.openInputStream(art)?.use { BitmapFactory.decodeStream(it) }
        }
    }.getOrNull()
    if (bmp != null) cache.put(key, bmp) else missing.add(key)
    return bmp
}

/** غلاف المقطع (أو null إن لم يوجد). يُحمَّل في الخلفية ويُخزَّن مؤقتاً. */
@Composable
fun rememberArt(track: Track?, px: Int): Bitmap? {
    val context = LocalContext.current.applicationContext
    val state = produceState<Bitmap?>(null, track?.albumId, px) {
        value = if (track == null) null else withContext(Dispatchers.IO) { loadArt(context, track, px) }
    }
    return state.value
}

/** متوسط لون الصورة، لتلوين خلفية المشغّل. */
fun Bitmap.averageColor(): Color = runCatching {
    Color(Bitmap.createScaledBitmap(this, 1, 1, true).getPixel(0, 0))
}.getOrDefault(Color(0xFF4A69BD))

/** الحجم يحدده المستدعي عبر [modifier]. عند غياب الغلاف يظهر تدرّج مع أيقونة نغمة. */
@Composable
fun AlbumArt(
    track: Track?,
    modifier: Modifier = Modifier,
    radius: Dp = 12.dp,
    px: Int = 160,
) {
    val bmp = rememberArt(track, px)
    Box(
        modifier
            .clip(RoundedCornerShape(radius))
            .background(Brush.linearGradient(listOf(Color(0xFF12303D), Color(0xFF06B6D4)))),
        contentAlignment = Alignment.Center,
    ) {
        if (bmp != null) {
            Image(
                bitmap = bmp.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            GlowIcon(IconKind.Note, Color.White.copy(alpha = 0.85f), 24.dp)
        }
    }
}

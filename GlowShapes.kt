package com.glow.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/** الأشكال: حواف دائرية كبيرة تعطي طابع Glow الناعم. */
object GlowShapes {
    val Pill = RoundedCornerShape(percent = 50)
    val Card = RoundedCornerShape(24.dp)
    val Artwork = RoundedCornerShape(28.dp)
    val TopSheet = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)

    val Material = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(20.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(32.dp),
    )
}

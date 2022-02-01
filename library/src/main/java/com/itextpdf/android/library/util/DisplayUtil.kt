package com.itextpdf.android.library.util

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt


class DisplayUtil {
    companion object {
        fun dpToPx(dp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
            ).roundToInt()
        }
    }
}
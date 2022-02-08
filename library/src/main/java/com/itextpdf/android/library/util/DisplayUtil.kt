package com.itextpdf.android.library.util

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt


object DisplayUtil {
    /**
     * Converts dp to pixels.
     *
     * @param dp    the dp value that should be converted to pixels
     * @param context   the context that is used to get the displayMetrics
     * @return  the passed dp value converted to pixels
     */
    fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).roundToInt()
    }
}
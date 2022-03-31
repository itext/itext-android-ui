package com.itextpdf.android.library.extensions

import android.graphics.RectF
import com.itextpdf.kernel.geom.Rectangle
import kotlin.math.abs
import kotlin.math.min

fun RectF.toItextRectangle(): Rectangle {
    // make sure to use minimum of left/right and bottom/top to always get bottom left corner and also absolute values for width and height
    return Rectangle(min(left, right), min(bottom, top), abs(width()), abs(height()))
}
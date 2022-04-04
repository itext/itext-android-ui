package com.itextpdf.android.library.extensions

import android.graphics.PointF
import com.itextpdf.kernel.pdf.annot.PdfAnnotation

/**
 * Checks if one annotation is the same as the other even if the object isn't the same based on
 * title, content and rectangle
 *
 * @param other the other annotation
 * @return  true if it's the same annotation
 */
internal fun PdfAnnotation.isSameAs(other: PdfAnnotation): Boolean {
    var sameRectangle = true
    for (obj in rectangle.iterator()) {
        if (!other.rectangle.contains(obj)) {
            sameRectangle = false
            break
        }
    }
    return title == other.title && contents == other.contents && sameRectangle
}

/**
 * Checks if an annotation is at a specific position within the pdf coordinate system
 *
 * @param position
 * @return
 */
fun PdfAnnotation.isAtPosition(position: PointF): Boolean {
    // rectangle should have 4 entries: lower-left and upper-right x and y coordinates: [llx, lly, urx, ury]
    if (rectangle.size() < 4) return false
    val llx = rectangle.getAsNumber(0).floatValue()
    val lly = rectangle.getAsNumber(1).floatValue()
    val urx = rectangle.getAsNumber(2).floatValue()
    val ury = rectangle.getAsNumber(3).floatValue()

    // check if position is in rectangle bounds
    return position.x > llx && position.x < urx && position.y > lly && position.y < ury
}
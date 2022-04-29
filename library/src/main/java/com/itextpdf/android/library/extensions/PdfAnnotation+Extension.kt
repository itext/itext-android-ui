package com.itextpdf.android.library.extensions

import android.graphics.PointF
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.annot.PdfAnnotation

/**
 * Checks if one annotation is the same as the other even if the object isn't the same based on
 * title, content and rectangle
 *
 * @param other the other annotation
 * @return  true if it's the same annotation
 */
internal fun PdfAnnotation.isSameAs(other: PdfAnnotation): Boolean {

    val sameRectangles: Boolean = rectangle.all { other.rectangle.contains(it) }
//    val propertiesComparison: Int = compareValuesBy(this, other,
//        { it.title.value },
//        { it.contents.value })

    val propertiesEqual = this.title == other.title && this.contents == other.contents

    return propertiesEqual && sameRectangles
}

/**
 * Checks if an annotation is at a specific position within the pdf coordinate system
 *
 * @param position
 * @return
 */
fun PdfAnnotation.isAtPosition(position: PointF, pageIndex: Int): Boolean {

    if (this.page.getPageIndex() != pageIndex) {
        return false
    }

    // rectangle should have 4 entries: lower-left and upper-right x and y coordinates: [llx, lly, urx, ury]
    if (rectangle.size() < 4) return false
    val llx = rectangle.getAsNumber(0).floatValue()
    val lly = rectangle.getAsNumber(1).floatValue()
    val urx = rectangle.getAsNumber(2).floatValue()
    val ury = rectangle.getAsNumber(3).floatValue()

    // check if position is in rectangle bounds
    return position.x > llx && position.x < urx && position.y > lly && position.y < ury
}

/**
 * Returns the center point of the annotation within the pdf coordinate system
 *
 * @return  the center point
 */
fun PdfAnnotation.getCenterPoint(): PointF {
    // rectangle should have 4 entries: lower-left and upper-right x and y coordinates: [llx, lly, urx, ury]
    val llx = rectangle.getAsNumber(0).floatValue()
    val lly = rectangle.getAsNumber(1).floatValue()
    val urx = rectangle.getAsNumber(2).floatValue()
    val ury = rectangle.getAsNumber(3).floatValue()

    val annotationWidth = urx - llx
    val annotationHeight = ury - lly
    return PointF(llx + annotationWidth / 2, lly + annotationHeight / 2)
}

package com.itextpdf.android.library.extensions

import com.itextpdf.kernel.pdf.annot.PdfAnnotation

/**
 * Checks if one annotation is the same as the other even if the object isn't the same based on
 * title, content and rectangle
 *
 * @param other the other annotation
 * @return  true if it's the same annotation
 */
fun PdfAnnotation.isSameAs(other: PdfAnnotation): Boolean {
    var sameRectangle = true
    for (obj in rectangle.iterator()) {
        if (!other.rectangle.contains(obj)) {
            sameRectangle = false
            break
        }
    }
    return title == other.title && contents == other.contents && sameRectangle
}
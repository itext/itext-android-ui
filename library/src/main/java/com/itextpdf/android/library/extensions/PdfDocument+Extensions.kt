package com.itextpdf.android.library.extensions

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.annot.PdfAnnotation


/**
 * Returns all pages of the given pdf-document.
 */
fun PdfDocument.getPages(): List<PdfPage> {

    return buildList {
        for (i in 1..numberOfPages) {
            add(getPage(i))
        }
    }

}

/**
 * Returns all annotations of the pdf-document.
 */
fun PdfDocument.getAnnotations(): List<PdfAnnotation> {
    return getPages().flatMap { it.annotations }
}
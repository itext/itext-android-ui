package com.itextpdf.android.library.extensions

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.annot.PdfAnnotation


fun PdfDocument.getPages(): List<PdfPage> {

    return buildList {
        for (i in 1..numberOfPages) {
            add(getPage(i))
        }
    }

}

fun PdfDocument.getAnnotations(): List<PdfAnnotation> {
    return getPages().flatMap { it.annotations }
}
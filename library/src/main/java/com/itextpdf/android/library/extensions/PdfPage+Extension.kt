package com.itextpdf.android.library.extensions

import com.itextpdf.kernel.pdf.PdfPage

fun PdfPage.getPageNumber(): Int {
    return document.getPageNumber(this)
}
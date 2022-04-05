package com.itextpdf.android.library.extensions

import com.itextpdf.kernel.pdf.PdfPage

fun PdfPage.getPageIndex(): Int {
    return document.getPageNumber(this) - 1
}
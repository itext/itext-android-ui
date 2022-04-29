package com.itextpdf.android.library.extensions

import com.itextpdf.kernel.pdf.PdfPage

/**
 * Returns the zero-based page-index of the pdf-page.
 */
fun PdfPage.getPageIndex(): Int {
    return document.getPageNumber(this) - 1
}
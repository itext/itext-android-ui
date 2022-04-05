package com.itextpdf.android.library.annotations

import com.itextpdf.kernel.pdf.annot.PdfAnnotation

sealed class AnnotationAction {
    object ADD : AnnotationAction()
    object HIGHLIGHT : AnnotationAction()
    class EDIT(val annotation: PdfAnnotation) : AnnotationAction()
    object DELETE : AnnotationAction()
}
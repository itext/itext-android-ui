package com.itextpdf.android.library.fragments

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File


sealed class PdfResult : Parcelable {

    @Parcelize
    object CancelledByUser : PdfResult()

    @Parcelize
    class PdfEdited(val file: File) : PdfResult()

    @Parcelize
    class PdfSplit(
        val fileContainingSelectedPages: Uri,
        val fileContainingUnselectedPages: Uri?
    ) : PdfResult()
}
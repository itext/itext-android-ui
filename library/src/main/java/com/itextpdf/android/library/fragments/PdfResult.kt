package com.itextpdf.android.library.fragments

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.io.File


@Parcelize
sealed class PdfResult : Parcelable {
    object CancelledByUser : PdfResult()
    class PdfEdited(val file: File) : PdfResult()
    class PdfSplit(
        val fileContainingSelectedPages: Uri,
        val fileContainingUnselectedPages: Uri?
    ) : PdfResult()
}
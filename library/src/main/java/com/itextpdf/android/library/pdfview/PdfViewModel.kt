package com.itextpdf.android.library.pdfview

import android.content.Context
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.*

class PdfViewModel : ViewModel() {

    var pdfRenderer: PdfRenderer? = null

    fun getPdfRenderer(uri: Uri, context: Context) {
        try {
            val parcelFileDescriptor: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(uri, "r")
            if (parcelFileDescriptor != null) {
                pdfRenderer = PdfRenderer(parcelFileDescriptor)
            }
        } catch (exception: Exception) {
            throwExceptionForPdfRenderer(exception)
        }
    }

    private fun throwExceptionForPdfRenderer(exception: Exception) {
        val message: String = when (exception) {
            is IOException -> exception.localizedMessage
            is SecurityException -> exception.localizedMessage
            is FileNotFoundException -> exception.localizedMessage
            else -> exception.localizedMessage
        }
        Log.d("PDF Renderer exception", message)
    }
}
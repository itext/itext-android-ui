package com.itextpdf.android.library.pdfview

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.lifecycle.ViewModel
import java.io.*

class PdfViewModel : ViewModel() {

    var pdfRenderer: PdfRenderer? = null

    fun getPdfRenderer(filePath: String) {
        try {
            pdfRenderer = PdfRenderer(
                ParcelFileDescriptor.open(
                    File(filePath),
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
            )
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
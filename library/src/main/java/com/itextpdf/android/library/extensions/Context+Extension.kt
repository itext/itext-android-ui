package com.itextpdf.android.library.extensions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.File
import java.io.FileNotFoundException

/**
 * Returns the fileName of the file at the given uri.
 *
 * @param uri   the uri of the file we want a fileName for
 * @return  the fileName if successful, null if not
 */
fun Context.getFileName(uri: Uri): String? {
    val uriString: String = uri.toString()
    val pdfFile = File(uriString)
    var fileName: String? = null

    // get filename
    if (uriString.startsWith("content://")) {
        var cursor: Cursor? = null
        try {
            cursor =
                contentResolver.query(uri, null, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                fileName =
                    cursor.getString(cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            cursor?.close()
        }
    } else if (uriString.startsWith("file://")) {
        fileName = pdfFile.name
    }
    return fileName
}
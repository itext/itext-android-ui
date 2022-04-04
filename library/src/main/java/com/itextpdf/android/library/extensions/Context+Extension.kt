package com.itextpdf.android.library.extensions

import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import com.itextpdf.android.library.Constants
import java.io.File

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
    if (uriString.startsWith(Constants.CONTENT_PREFIX)) {
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
    } else if (uriString.startsWith(Constants.FILE_PREFIX)) {
        fileName = pdfFile.name
    }
    return fileName
}

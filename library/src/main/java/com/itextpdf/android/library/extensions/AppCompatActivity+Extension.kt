package com.itextpdf.android.library.extensions

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

/**
 * An intent that can be used to select a pdf file with the phone's default file explorer.
 */
val selectPdfIntent: Intent
    get() {
        val intentPDF = Intent(Intent.ACTION_GET_CONTENT)
        intentPDF.type = "application/pdf"
        intentPDF.addCategory(Intent.CATEGORY_OPENABLE)
        return intentPDF
    }

/**
 * Registers a request to start an activity for result by calling the AppCompatActivity function registerForActivityResult.
 * Returns an ActivityResultLauncher object with the generic type Intent that can be used to launch the selectPdfIntent intent
 * to select a pdf file with the phone's default file explorer.
 * After the file selection, the callback argument is called which receives the uri to the pdf and the file name of the
 * pdf file if the selection was successful. If not, those values can be null, but the callback is always called.
 *
 * @param callback  a callback that is called after selecting a pdf file that returns the uri to the pdf file and the file
 *                  name in case of a successful selection and null when something went wrong. This callback should be
 *                  used to for any desired action with the selected pdf file.
 * @return          the ActivityResultLauncher to launch the selectPdfIntent
 */
fun AppCompatActivity.registerPdfSelectionResult(callback: (pdfUri: Uri?, fileName: String?) -> Unit): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            // Get the Uri of the selected file
            val uri: Uri? = data?.data
            if (uri != null) {
                val uriString: String = uri.toString()
                val myFile = File(uriString)
                var displayName: String? = null

                // get filename
                if (uriString.startsWith("content://")) {
                    var cursor: Cursor? = null
                    try {
                        cursor =
                            contentResolver.query(uri, null, null, null, null)
                        if (cursor != null && cursor.moveToFirst()) {
                            displayName =
                                cursor.getString(
                                    cursor.getColumnIndexOrThrow(
                                        OpenableColumns.DISPLAY_NAME
                                    )
                                )
                        }
                    } finally {
                        cursor?.close()
                    }
                } else if (uriString.startsWith("file://")) {
                    displayName = myFile.name
                }

                //TODO: reads and prints content (line by line)
//                    val selectedFilename = data.data //The uri with the location of the file
//                    if (selectedFilename != null) {
//                        contentResolver.openInputStream(selectedFilename)?.bufferedReader()?.forEachLine {
//                            Log.i("#####", "filecontent: $it")
//                        }
//                    }

                callback(uri, displayName)
            } else {
                callback(null, null)
            }
        } else {
            callback(null, null)
        }
    }
}
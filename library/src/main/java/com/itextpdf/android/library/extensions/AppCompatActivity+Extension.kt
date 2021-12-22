package com.itextpdf.android.library.extensions

import android.app.Activity
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.File

fun AppCompatActivity.registerPdfSelectionResult(callback: (pdfUri: Uri?, fileName: String?) -> Unit): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            val data: Intent? = result.data
            // Get the Uri of the selected file
            val uri: Uri? = data?.data
            if (uri != null) {
                val uriString: String = uri.toString()
                val myFile = File(uriString)
                val path: String = myFile.absolutePath
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
                Log.i("#####", "file name: $displayName")


                //TODO: reads and prints content (line by line)
//                    val selectedFilename = data.data //The uri with the location of the file
//                    if (selectedFilename != null) {
//                        contentResolver.openInputStream(selectedFilename)?.bufferedReader()?.forEachLine {
//                            Log.i("#####", "filecontent: $it")€
//                        }
//                    }

                callback(uri, displayName ?: "")
            }
        }
        callback(null, null)
    }
}
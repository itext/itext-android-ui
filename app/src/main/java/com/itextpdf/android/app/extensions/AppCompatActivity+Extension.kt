package com.itextpdf.android.app.extensions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.library.extensions.getFileName

/**
 * An intent that can be used to select a pdf file with the phone's default file explorer.
 */
val AppCompatActivity.selectPdfIntent: Intent
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
                val fileName = getFileName(uri)
                callback(uri, fileName)
            } else {
                callback(null, null)
            }
        } else {
            callback(null, null)
        }
    }
}
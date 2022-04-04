package com.itextpdf.android.app.ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import com.itextpdf.android.app.BuildConfig
import com.itextpdf.android.app.R

object ShareUtil {

    private fun createPdfShareIntent(context: Context, pdfUri: Uri): Intent {

        val shareableUri = FileProvider.getUriForFile(
            context,
            BuildConfig.APPLICATION_ID + ".provider",
            pdfUri.toFile()
        )

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.putExtra(Intent.EXTRA_STREAM, shareableUri)
        shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        shareIntent.type = "application/pdf"

        return shareIntent
    }

    /**
     * Use this function to open up the share sheet an share one pdf file
     *
     * @param pdfUri The uri to the pdf that should be shared
     */
    fun sharePdf(activity: Activity, pdfUri: Uri) {

        val title = activity.getString(R.string.share_pdf_title)
        val shareIntent = createPdfShareIntent(activity, pdfUri)

        activity.startActivity(Intent.createChooser(shareIntent, title))
    }

}
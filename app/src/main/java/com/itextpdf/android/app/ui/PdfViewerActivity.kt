package com.itextpdf.android.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.app.R
import com.itextpdf.android.app.databinding.ActivityPdfViewerBinding
import com.itextpdf.android.library.fragments.PdfFragment

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileName = intent.extras?.getString(EXTRA_PDF_TITLE) ?: ""
        val pdfUri = Uri.parse(intent.extras?.getString(EXTRA_PDF_URI) ?: "")
        val pdfIndex = intent.extras?.getInt(EXTRA_PDF_INDEX) ?: -1

        // for pdf with index 0 use the params set within the xml file, for the other pdfs, replace the fragment
        if (pdfIndex != 0) {
            // set fragment in code
            if (savedInstanceState == null) {
                val fragment = PdfFragment()
                fragment.pdfUri = pdfUri
                fragment.fileName = fileName

                // set some settings for demonstration
                if (pdfIndex == 1) {
                    fragment.displayFileName = true
                    fragment.pageSpacing = 100
                    fragment.enableAnnotationRendering = false
                    fragment.enableDoubleTapZoom = false
                    fragment.primaryColor = "#295819"
                    fragment.secondaryColor = "#950178"
                    fragment.backgroundColor = "#119191"
                } else if (pdfIndex == 3) {
                    fragment.enableThumbnailNavigationView = false
                }

                val fm = supportFragmentManager.beginTransaction()
                fm.replace(R.id.pdf_fragment_container, fragment)
                fm.commit()
            }
        }
    }

    companion object {
        private const val EXTRA_PDF_URI = "EXTRA_PDF_URI"
        private const val EXTRA_PDF_TITLE = "EXTRA_PDF_TITLE"
        private const val EXTRA_PDF_INDEX = "EXTRA_PDF_INDEX"

        /**
         * Convenience function to launch the PdfViewerActivity. Adds the passed uri and the filename
         * as a String extra to the intent and starts the activity.
         *
         * @param context   the context
         * @param uri       the uri to the pdf file
         * @param fileName  the name of the pdf file
         * @param pdfIndex  the index of the pdf file within the list
         */
        fun launch(context: Context, uri: Uri, fileName: String?, pdfIndex: Int?) {
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra(EXTRA_PDF_URI, uri.toString())
            intent.putExtra(EXTRA_PDF_TITLE, fileName)
            intent.putExtra(EXTRA_PDF_INDEX, pdfIndex)
            context.startActivity(intent)
        }
    }
}
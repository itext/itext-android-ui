package com.itextpdf.android.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.app.R
import com.itextpdf.android.app.databinding.ActivityPdfViewerBinding
import com.itextpdf.android.library.ui.PdfFragment

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fileName = intent.extras?.getString(EXTRA_PDF_TITLE) ?: ""
        val pdfUri = Uri.parse(intent.extras?.getString(EXTRA_PDF_URI) ?: "")

        // set fragment in code
        val fragment = PdfFragment()
        fragment.text = getString(R.string.filename, fileName)
        fragment.pdfUri = pdfUri
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.pdf_fragment_container, fragment)
        fm.commit()
    }

    companion object {
        private const val EXTRA_PDF_URI = "EXTRA_PDF_URI"
        private const val EXTRA_PDF_TITLE = "EXTRA_PDF_TITLE"

        /**
         * Convenience function to launch the PdfViewerActivity. Adds the passed uri and the filename
         * as a String extra to the intent and starts the activity.
         *
         * @param context   the context
         * @param uri       the uri to the pdf file
         * @param fileName  the name of the pdf file
         */
        fun launch(context: Context, uri: Uri, fileName: String) {
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra(EXTRA_PDF_URI, uri.toString())
            intent.putExtra(EXTRA_PDF_TITLE, fileName)
            context.startActivity(intent)
        }
    }
}
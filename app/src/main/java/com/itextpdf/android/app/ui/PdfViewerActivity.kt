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

        val title = intent.extras?.getString(EXTRA_PDF_TITLE) ?: ""
        val pdfUri = Uri.parse(intent.extras?.getString(EXTRA_PDF_URI) ?: "")

        // set fragment in code
        val fragment = PdfFragment()
        fragment.text = title
        fragment.pdfUri = pdfUri
        val fm = supportFragmentManager.beginTransaction()
        fm.replace(R.id.pdf_fragment_container, fragment)
        fm.commit()
    }

    companion object {
        private const val EXTRA_PDF_URI = "EXTRA_PDF_URI"
        private const val EXTRA_PDF_TITLE = "EXTRA_PDF_TITLE"

        fun launch(context: Context, uri: Uri, title: String) {
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra(EXTRA_PDF_URI, uri.toString())
            intent.putExtra(EXTRA_PDF_TITLE, title)
            context.startActivity(intent)
        }
    }
}
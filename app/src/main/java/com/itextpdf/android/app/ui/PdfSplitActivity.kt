package com.itextpdf.android.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import com.itextpdf.android.app.R
import com.itextpdf.android.app.databinding.ActivitySplitPdfBinding
import com.itextpdf.android.library.fragments.PdfConfig
import com.itextpdf.android.library.fragments.SplitDocumentFragment

class PdfSplitActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplitPdfBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySplitPdfBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.extras?.let { extras ->
            val pdfUriString = extras.getString(EXTRA_PDF_URI)
            if (!pdfUriString.isNullOrEmpty()) {

                val pdfUri = Uri.parse(pdfUriString)
                val fileName = extras.getString(EXTRA_PDF_TITLE)
                val config = PdfConfig(pdfUri = pdfUri, fileName = fileName)

                // set fragment in code
                if (savedInstanceState == null) {
                    val fragment: SplitDocumentFragment = SplitDocumentFragment.newInstance(config)
                    val fm = supportFragmentManager.beginTransaction()
                    fm.replace(R.id.pdf_splitter_container, fragment, SPLIT_FRAGMENT_TAG)
                    fm.commit()
                }
            }
        }

        // listen for the fragment result from the SplitDocumentFragment to get a list pdfUris resulting from the split
        supportFragmentManager.setFragmentResultListener(SplitDocumentFragment.SPLIT_DOCUMENT_RESULT, this) { requestKey, bundle ->

            val pdfUriList: List<Uri> = bundle.getParcelableArrayList<Uri>(SplitDocumentFragment.SPLIT_PDF_URI_LIST).orEmpty()
            val first: Uri = pdfUriList.first()

            Toast.makeText(
                this,
                getString(R.string.split_document_success, "${first.path}/"),
                Toast.LENGTH_LONG
            ).show()

            supportFragmentManager.clearFragmentResult(requestKey)

            ShareUtil.sharePdf(this, first)



        }
    }

    companion object {
        private const val EXTRA_PDF_URI = "EXTRA_PDF_URI"
        private const val EXTRA_PDF_TITLE = "EXTRA_PDF_TITLE"

        private const val SPLIT_FRAGMENT_TAG = "splitFragment"

        /**
         * Convenience function to launch the PdfViewerActivity. Adds the passed uri and the filename
         * as a String extra to the intent and starts the activity.
         *
         * @param context   the context
         * @param uri       the uri to the pdf file
         * @param fileName  the name of the pdf file
         * @param pdfIndex  the index of the pdf file within the list
         */
        fun launch(context: Context, uri: Uri, fileName: String?) {
            val intent = createIntent(context, uri, fileName)
            context.startActivity(intent)
        }

        fun createIntent(context: Context, uri: Uri, fileName: String?): Intent {
            val intent = Intent(context, PdfSplitActivity::class.java)
            intent.putExtra(EXTRA_PDF_URI, uri.toString())
            intent.putExtra(EXTRA_PDF_TITLE, fileName)

            return intent
        }
    }
}
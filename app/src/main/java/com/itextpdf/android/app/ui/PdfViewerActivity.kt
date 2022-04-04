package com.itextpdf.android.app.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.net.toUri
import com.itextpdf.android.app.R
import com.itextpdf.android.app.databinding.ActivityPdfViewerBinding
import com.itextpdf.android.library.fragments.PdfConfig
import com.itextpdf.android.library.fragments.PdfFragment
import com.itextpdf.android.library.fragments.PdfResult
import com.itextpdf.android.library.fragments.SplitDocumentFragment
import java.io.File

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        listenForPdfResults()

        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        intent?.extras?.let { extras ->
            val pdfUriString = extras.getString(EXTRA_PDF_URI)
            if (!pdfUriString.isNullOrEmpty()) {

                val pdfUri = Uri.parse(pdfUriString)
                val fileName = extras.getString(EXTRA_PDF_TITLE)
                val pdfIndex = extras.getInt(EXTRA_PDF_INDEX, -1)

                // for pdf with index 0 (Sample 1) use the params set within the xml file, for the other pdfs, replace the fragment
                if (pdfIndex != 0) {
                    // set fragment in code
                    if (savedInstanceState == null) {
                        val fragment: PdfFragment
                        // setup the fragment with different settings based on the index of the selected pdf
                        when (pdfIndex) {
                            1 -> { // Sample 2

                                val config = PdfConfig.build {
                                    this.pdfUri = pdfUri
                                    this.fileName = fileName
                                    displayFileName = true
                                    pageSpacing = 100
                                    enableAnnotationRendering = false
                                    enableDoubleTapZoom = false
                                    primaryColor = "#295819"
                                    secondaryColor = "#950178"
                                    backgroundColor = "#119191"
                                    helpDialogText = getString(R.string.custom_help_text)
                                }

                                fragment = PdfFragment.newInstance(config)
                            }
                            3 -> {
                                // Sample 4

                                val config = PdfConfig.build {
                                    this.pdfUri = pdfUri
                                    this.fileName = fileName
                                    enableThumbnailNavigationView = false
                                    enableHelpDialog = false
                                }

                                fragment = PdfFragment.newInstance(config)
                            }
                            else -> { // Sample 3 and pdfs from file explorer

                                val config = PdfConfig.build {
                                    this.pdfUri = pdfUri
                                    this.fileName = fileName
                                    helpDialogTitle = getString(R.string.custom_help_title)
                                }

                                fragment = PdfFragment.newInstance(config)
                            }
                        }

                        val fm = supportFragmentManager.beginTransaction()
                        fm.replace(R.id.pdf_fragment_container, fragment, "pdfFragment")
                        fm.commit()
                    }
                }
            }
        }
    }

    private fun listenForPdfResults() {

        supportFragmentManager.setFragmentResultListener(PdfFragment.REQUEST_KEY, this) { requestKey, bundle ->
            val result: PdfResult? = bundle.getParcelable(PdfFragment.RESULT_FILE)
            handlePdfResult(result)
            supportFragmentManager.clearFragmentResult(requestKey)
            finish()
        }

    }

    private fun handlePdfResult(result: PdfResult?) {

        when (result) {
            PdfResult.CancelledByUser -> Toast.makeText(this, R.string.cancelled_by_user, Toast.LENGTH_LONG).show()
            is PdfResult.PdfEdited -> ShareUtil.sharePdf(this, result.file.toUri())
            is PdfResult.PdfSplit -> ShareUtil.sharePdf(this, result.fileContainingSelectedPages)
            null -> Toast.makeText(this, R.string.no_result, Toast.LENGTH_LONG).show()
        }

    }
    
    companion object {

        private const val LOG_TAG = "PdfViewActivity"

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
        fun launch(context: Context, uri: Uri, fileName: String?, pdfIndex: Int? = null) {
            val intent = Intent(context, PdfViewerActivity::class.java)
            intent.putExtra(EXTRA_PDF_URI, uri.toString())
            intent.putExtra(EXTRA_PDF_TITLE, fileName)
            intent.putExtra(EXTRA_PDF_INDEX, pdfIndex)
            context.startActivity(intent)
        }
    }
}
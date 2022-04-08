package com.itextpdf.android.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.app.BuildConfig
import com.itextpdf.android.app.R
import com.itextpdf.android.app.databinding.ActivityMainBinding
import com.itextpdf.android.app.extensions.registerPdfSelectionResult
import com.itextpdf.android.app.extensions.selectPdfIntent
import com.itextpdf.android.app.ui.MainActivity.PdfRecyclerItem.Companion.TYPE_PDF
import com.itextpdf.android.library.util.PdfManipulator
import com.itextpdf.android.library.views.PdfThumbnailView
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * An ActivityResultLauncher<Intent> object that is used to launch the selectPdfIntent to select a
     * pdf file with the default file explorer and open the PdfViewerActivity with that pdf file.
     */
    private val pdfSelectionResultLauncher = registerPdfSelectionResult { pdfUri, fileName ->
        if (pdfUri != null) {
            PdfViewerActivity.launch(this, pdfUri, fileName, null)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        // prepare pre-defined pdf files from the assets folder to display them in a recyclerView
        val data = mutableListOf<PdfRecyclerItem>()
        pdfFileNames.forEachIndexed { i, fileName ->
            val file = loadFileFromAssets(fileName)
            val uri = Uri.fromFile(file)

            data.add(PdfItem(pdfTitles[i], fileName, pdfDescriptions[i], uri) {
                when (i) {
                    4 -> {
                        // element at index 4 should open the split view directly
                        PdfSplitActivity.launch(this, uri, fileName)
                    }
                    5 -> {
                        // element at index 5 should directly split document without any UI
                        // create a list with only index 0, which results in a split document with the first page and another one with the other pages
                        noUISplit(uri, fileName, listOf(0))
                    }
                    else -> {
                        // open the pdf viewer for the remaining elements
                        PdfViewerActivity.launch(this, uri, fileName, i)
                    }
                }
            })
        }

        val adapter = PdfAdapter(data)
        binding.rvPdfList.adapter = adapter
        binding.rvPdfList.layoutManager = LinearLayoutManager(this)
    }

    private fun loadFileFromAssets(fileName: String): File {

        val cacheDir: File = ContextCompat.getExternalCacheDirs(this).first()

        // remove .pdf suffix
        val tempFileNameSB = StringBuilder(fileName.removeSuffix(".pdf"))
        // add _copy text
        tempFileNameSB.append("_copy")
        // add .pdf again
        tempFileNameSB.append(".pdf")

        val tempFile = File(cacheDir, tempFileNameSB.toString())

        assets.open(fileName).use { inputStream ->
            Files.copy(inputStream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }

        return tempFile
    }

    /**
     * Helper function to split a pdf file with the given uri and the selectedPageIndices
     *
     * @param uri   the uri of the pdf file
     * @param fileName  the filename of the pdf
     * @param selectedPageIndices   the page indices that should be in the one pdf document. All the other indices will be in the other document
     */
    private fun noUISplit(uri: Uri, fileName: String, selectedPageIndices: List<Int>) {
        // specify the path where the newly created pdf files will be stored -> Cache
        val storageFolderPath = (externalCacheDir ?: cacheDir).absolutePath
        val pdfUriList = PdfManipulator.create(this, uri).splitPdfWithSelection(
            fileName = fileName,
            selectedPageIndices = selectedPageIndices,
            storageFolderPath = storageFolderPath
        )
        // check if uris were returned
        if (pdfUriList.isNotEmpty()) {
            // open the share sheet to share the first pdf file which contains of the selected pages
            ShareUtil.sharePdf(this, pdfUriList.first())
        } else {
            Log.e(
                TAG,
                getString(com.itextpdf.android.library.R.string.split_document_error)
            )
        }
    }

    /**
     * Use this function to open up the share sheet an share multiple pdf files
     *
     * @param pdfUriList    the list of uris to the pdfs that should be shared
     */
    private fun shareMultiplePdfs(pdfUriList: List<Uri>) {
        // prepare a sharable list of uris with the FileProvider (also make sure to setup provider_paths.xml and set FileProvider in Manifest)
        val shareableUriList = arrayListOf<Uri>()
        pdfUriList.forEach {
            shareableUriList.add(
                FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".provider",
                    it.toFile()
                )
            )
        }

        val shareIntent = Intent(Intent.ACTION_SEND_MULTIPLE)
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, shareableUriList)
        shareIntent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        shareIntent.type = "application/pdf"
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_pdf_title)))
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_open_pdf -> {
            // use the pdfSelectionResultLauncher with the selectPdfIntent to open a file explorer and look for pdf files
            pdfSelectionResultLauncher.launch(selectPdfIntent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        /**
         * The pre-defined titles of the pdf files that are stored in the assets folder.
         */
        private val pdfTitles = mutableListOf(
            "Pdf View: Sample 1",
            "Pdf View: Sample 2",
            "Pdf View: Sample 3",
            "Pdf View: Sample 4",
            "Pdf Split View: Sample 3",
            "No UI Split + Share: Sample 2"
        )

        /**
         * The pre-defined descriptions of the pdf files that are stored in the assets folder.
         */
        private val pdfDescriptions = mutableListOf(
            "Sample 1 shows a the pdf view that was customised within the xml file.",
            "Sample 2 shows a the pdf view that was customised within the code.",
            "Sample 3 shows the pdf view with it's default settings.",
            "Sample 4 shows the view without an option to open the thumbnail navigation view.",
            "Split Sample 3 opens the split document view directly for Sample 3.",
            "No UI split + Share Sample 2 directly splits the two-page document into two documents with one page each without showing a split document view and opens the share sheet."
        )

        /**
         * The file names of the pdf files that are stored in the assets folder.
         */
        private val pdfFileNames =
            mutableListOf(
                "sample_1.pdf",
                "sample_2.pdf",
                "sample_3.pdf",
                "sample_4.pdf",
                "sample_3.pdf",
                "sample_2.pdf"
            )
    }

    /**
     * Data class that hold all the information for a pdf file.
     *
     * @property title          title of the pdf file
     * @property fileName       file name of the pdf file
     * @property description    description of the pdf file
     * @property pdfUri         uri to the pdf file
     * @property action         action that should be called when pdf item is selected in the list
     */
    private data class PdfItem(
        val title: String,
        val fileName: String,
        val description: String,
        val pdfUri: Uri,
        val action: () -> Unit
    ) : PdfRecyclerItem {
        override val type: Int
            get() = TYPE_PDF
    }

    /**
     * Interface that is used to define the type of the item, which in turn is used to specify the layout
     * of the item.
     */
    private interface PdfRecyclerItem {
        val type: Int

        companion object {
            const val TYPE_PDF = R.layout.recycler_item_pdf_selection
        }
    }

    /**
     * Base class of a viewHolder for displaying pdfs in a recyclerView.
     *
     * @param view  the view class required by the viewHolder
     */
    private abstract class PdfBaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        protected val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        protected val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        protected val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        protected val thumbnailView: PdfThumbnailView = view.findViewById(R.id.thumbnail)

        abstract fun bind(item: PdfRecyclerItem)
    }

    /**
     * Concrete viewHolder class for displaying pdfs in a recyclerView
     *
     * @property view   the view class required by the viewHolder
     */
    private class PdfViewHolder(val view: View) : PdfBaseViewHolder(view) {
        override fun bind(item: PdfRecyclerItem) {
            if (item is PdfItem) {
                tvTitle.text = item.title
                tvFileName.text = view.context.getString(R.string.filename, item.fileName)
                tvDescription.text = item.description
                thumbnailView.set(item.pdfUri)

                itemView.setOnClickListener {
                    item.action()
                }
            }
        }
    }

    /**
     * The adapter class used to display pdf files in a recyclerView.
     *
     * @property data   the pdf data
     */
    private class PdfAdapter(val data: List<PdfRecyclerItem>) :
        RecyclerView.Adapter<PdfBaseViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfBaseViewHolder {

            val view = LayoutInflater.from(parent.context).inflate(
                viewType,
                parent,
                false
            )
            return when (viewType) {
                TYPE_PDF -> PdfViewHolder(view)
                else -> throw IllegalStateException("Unsupported viewType $viewType")
            }
        }

        override fun getItemCount(): Int = data.size

        override fun getItemViewType(position: Int): Int {
            return data[position].type
        }

        override fun onBindViewHolder(holder: PdfBaseViewHolder, position: Int) {
            val item = data[position]
            holder.bind(item)
        }
    }
}
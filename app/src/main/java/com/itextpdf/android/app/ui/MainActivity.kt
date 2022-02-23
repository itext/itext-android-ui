package com.itextpdf.android.app.ui

import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.app.R
import com.itextpdf.android.app.databinding.ActivityMainBinding
import com.itextpdf.android.app.ui.MainActivity.PdfRecyclerItem.Companion.TYPE_PDF
import com.itextpdf.android.app.util.FileUtil
import com.itextpdf.android.library.extensions.registerPdfSelectionResult
import com.itextpdf.android.library.extensions.selectPdfIntent
import com.itextpdf.android.library.fragments.SplitDocumentFragment
import com.itextpdf.android.library.views.PdfThumbnailView
import java.io.File
import java.io.IOException


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
            val path = loadPdfFromAssets(fileName)
            val uri = Uri.fromFile(File(path))

            data.add(PdfItem(pdfTitles[i], fileName, pdfDescriptions[i], uri) {
                // the 5th element should open the split view directly
                if (i == 4) {
                    PdfSplitActivity.launch(this, uri, fileName)
                } else {
                    PdfViewerActivity.launch(this, uri, fileName, i)
                }
            })
        }

        val adapter = PdfAdapter(data)
        binding.rvPdfList.adapter = adapter
        binding.rvPdfList.layoutManager = LinearLayoutManager(this)
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

    /**
     * Loads a pdf with the given fileName from the assets folder to a location the app can access and
     * returns the absolute path to that file if the operation was successful or throws an IOException
     * if something went wrong.
     *
     * @param fileName  the name of the pdf file that should be loaded from the assets folder
     * @return          the absolute path to the loaded file
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun loadPdfFromAssets(fileName: String): String {
        // create file object to read and write on in the cache directory of the app
        val file = File(cacheDir, fileName)
        if (!file.exists()) {
            val assetManager: AssetManager = assets
            // copy pdf file from assets to location of the previously created file
            FileUtil.copyAsset(assetManager, fileName, file.absolutePath)
        }
        return file.absolutePath
    }

    companion object {
        /**
         * The pre-defined titles of the pdf files that are stored in the assets folder.
         */
        private val pdfTitles = mutableListOf("Sample 1", "Sample 2", "Sample 3", "Sample 4", "Split Sample 3")

        /**
         * The pre-defined descriptions of the pdf files that are stored in the assets folder.
         */
        private val pdfDescriptions = mutableListOf(
            "Sample 1 shows a the pdf view that was customised within the xml file.",
            "Sample 2 shows a the pdf view that was customised within the code.",
            "Sample 3 shows the pdf view with it's default settings.",
            "Sample 4 shows the view without an option to open the thumbnail navigation view.",
            "Split Sample 3 opens the split document view directly for Sample 3."
        )

        /**
         * The file names of the pdf files that are stored in the assets folder.
         */
        private val pdfFileNames =
            mutableListOf("sample_1.pdf", "sample_2.pdf", "sample_3.pdf", "sample_4.pdf", "sample_3.pdf")
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
package com.itextpdf.android.app.ui

import android.content.res.AssetManager
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.app.R
import com.itextpdf.android.app.databinding.ActivityMainBinding
import com.itextpdf.android.app.ui.MainActivity.PdfRecyclerItem.Companion.TYPE_PDF
import com.itextpdf.android.app.util.FileUtil
import com.itextpdf.android.library.extensions.registerPdfSelectionResult
import com.itextpdf.android.library.extensions.selectPdfIntent
import com.itextpdf.android.library.views.PdfThumbnailView
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val pdfSelectionResultLauncher = registerPdfSelectionResult { pdfUri, fileName ->
        if (pdfUri != null) {
            PdfViewerActivity.launch(this, pdfUri, fileName ?: "")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.mainToolbar)

        val data = mutableListOf<PdfRecyclerItem>()

        pdfFileNames.forEachIndexed { i, fileName ->
            val path = loadPdfFromAssets(fileName)
            if (path != null) {
                val uri = Uri.fromFile(File(path))

                data.add(PdfItem(pdfTitles[i], fileName, pdfDescriptions[i], uri) {
                    PdfViewerActivity.launch(this, uri, fileName)
                })
            }
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

    @Throws(IOException::class)
    private fun loadPdfFromAssets(fileName: String): String? {
        // Create file object to read and write on
        val file = File(cacheDir, fileName)
        if (!file.exists()) {
            val assetManager: AssetManager = assets
            FileUtil.copyAsset(assetManager, fileName, file.absolutePath)
        }
        return file.absolutePath
    }

    companion object {
        private val pdfTitles = mutableListOf("Sample 1", "Sample 2", "Sample 3", "Sample 4")
        private val pdfDescriptions = mutableListOf(
            "Description for sample 1.",
            "Description for sample 2.",
            "Description for sample 3.",
            "Description for sample 4."
        )
        private val pdfFileNames =
            mutableListOf("sample_1.pdf", "sample_2.pdf", "sample_3.pdf", "sample_4.pdf")
    }

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

    private interface PdfRecyclerItem {
        val type: Int

        companion object {
            const val TYPE_PDF = R.layout.recycler_item_pdf
        }
    }

    private abstract class PdfBaseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        protected val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        protected val tvFileName: TextView = view.findViewById(R.id.tvFileName)
        protected val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        protected val thumbnailView: PdfThumbnailView = view.findViewById(R.id.thumbnail)

        abstract fun bind(item: PdfRecyclerItem)
    }

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
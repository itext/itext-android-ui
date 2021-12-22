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
import com.itextpdf.android.library.extensions.selectPdfIntent
import com.itextpdf.android.library.extensions.registerPdfSelectionResult
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
        binding.rvPdfList.layoutManager = LinearLayoutManager(this)

        val pdfTitles = mutableListOf("sample_1", "sample_2", "sample_3", "sample_4")
        val data = mutableListOf<PdfRecyclerItem>()

        pdfTitles.forEach { title ->
            val path = loadPdfFromAssets(title)
            if (path != null) {
                val uri = Uri.fromFile(File(path))

                data.add(PdfItem(title, uri) {
                    PdfViewerActivity.launch(this, uri, title)
                })
            }
        }

        val adapter = PdfAdapter(data)
        binding.rvPdfList.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_open_pdf -> {
            pdfSelectionResultLauncher.launch(selectPdfIntent)
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    @Throws(IOException::class)
    private fun loadPdfFromAssets(title: String): String? {
        val fileName = "$title.pdf"

        // Create file object to read and write on
        val file = File(cacheDir, fileName)
        if (!file.exists()) {
            val assetManager: AssetManager = assets
            FileUtil.copyAsset(assetManager, fileName, file.absolutePath)
        }
        return file.absolutePath
    }

    private fun setupTestThumbnailView() {
//        val pdf = pdfDocumentWriter(fileName)
//        if (pdf != null) {
//            val document = Document(pdf)
//
//            val paragraph = Paragraph("Test paragraph\nTest 2nd paragraph")
//            paragraph.setFontSize(50f)
//            document.add(paragraph)
//
//            document.close()
//            pdf.close()
//
//            //TODO: --------- START: this section was commented out ---------
////            val renderer = paragraph.createRendererSubTree().setParent(document.renderer)
////
////            val width = document.getPageEffectiveArea(PageSize.LETTER).width;
////            val height = document.getPageEffectiveArea(PageSize.LETTER).height;
////            val layoutResult = renderer.layout(
////                LayoutContext(
////                    LayoutArea(
////                        1,
////                        Rectangle(36f, 36f, width, height)
////                    )
////                )
////            )
////
////            print(layoutResult)
//
////            val numberOfPdfObjects: Int = pdf.getNumberOfPdfObjects()
////            for (i in 1..numberOfPdfObjects) {
////                val obj: PdfObject = pdf.getPdfObject(i)
////                println(obj)
////            }
//
////            val pRenderer: IRenderer = paragraph.createRendererSubTree().setParent(document.getRenderer())
////            val pLayoutResult: LayoutResult = pRenderer.layout(LayoutContext(LayoutArea(0, Rectangle(500f, 500f))))
////
////            val y: Float = pLayoutResult.getOccupiedArea().getBBox().getY()
////            val x: Float = pLayoutResult.getOccupiedArea().getBBox().getX()
//            //TODO: --------- END: this section was commented out ---------
//
//
//            val readPdf = pdfDocumentReader(fileName)
//            val width = readPdf?.defaultPageSize?.width ?: 0f
//            val height = readPdf?.defaultPageSize?.height ?: 0f
//
//            Log.i(Constants.LOG_TAG, "PDF page size: $width x $height")
//
//            var rect: Rectangle? = null
//
//            val parser = PdfDocumentContentParser(readPdf)
//            parser.processContent(1, object : IEventListener {
//                override fun eventOccurred(data: IEventData?, type: EventType?) {
//
//                    val textRenderInfo = data as? TextRenderInfo
//                    if (textRenderInfo != null) {
//
//                        val ascentStart = textRenderInfo.ascentLine.startPoint
//                        val ascentEnd = textRenderInfo.ascentLine.endPoint
//                        val x1 = ascentStart.get(0)
//                        val y1 = ascentStart.get(1)
//
//                        Log.i(
//                            Constants.LOG_TAG,
//                            "Text start line, " +
//                                    "x1: ${ascentStart.get(0)}, y1: ${ascentStart.get(1)}, " +
//                                    "x2: ${ascentEnd.get(0)}, y2: ${ascentEnd.get(1)}"
//                        )
//
//                        val descentStart = textRenderInfo.descentLine.startPoint
//                        val descentEnd = textRenderInfo.descentLine.endPoint
//                        val x2 = descentStart.get(0)
//                        val y2 = descentStart.get(1)
//
//                        Log.i(
//                            Constants.LOG_TAG,
//                            "Text end line, " +
//                                    "x1: ${descentStart.get(0)}, y1: ${descentStart.get(1)}, " +
//                                    "x2: ${descentEnd.get(0)}, y2: ${descentEnd.get(1)}"
//                        )
//
//                        val rectangle =
//                            Rectangle(x1, y1, textRenderInfo.unscaledWidth, abs(y1 - y2))
//                        Log.i(
//                            Constants.LOG_TAG,
//                            "Rectangle: $rectangle"
//                        )
//
//                        if (rect == null) {
//                            rect = rectangle
//                        }
//                    }
//                }
//
//                override fun getSupportedEvents(): MutableSet<EventType> {
//                    return mutableSetOf(EventType.RENDER_TEXT) // return null here to support all events
//                }
//            })
//
//            binding.thumbnail.setOnClickListener {
//
//                val intentPDF = Intent(Intent.ACTION_GET_CONTENT)
//                intentPDF.type = "application/pdf"
//                intentPDF.addCategory(Intent.CATEGORY_OPENABLE)
//                resultLauncher.launch(intentPDF)
//
////                val newName = "new.pdf"
////                val new = try {
////                    val readFile = getFileStreamPath(fileName).absoluteFile
////                    val output = openFileOutput(newName, MODE_PRIVATE)
////                    PdfDocument(PdfReader(readFile), PdfWriter(output))
////                } catch (e: FileNotFoundException) {
////                    e.printStackTrace()
////                    null
////                }
////
////                val newDoc = Document(new)
////                val r = rect
////
////                if (new != null && r != null) {
////
////                    val p = Paragraph("EDITED")
////                    p.setFontSize(50f)
////                    p.setFixedPosition(r.left, r.bottom - r.height, r.width)
////                    newDoc.add(p)
////
////                    new.close()
////                    newDoc.close()
////
////                    val file = getFileStreamPath(newName).absoluteFile
////                    binding.thumbnail.set(file)
////                }
//            }
//        }
//
//        val file = getFileStreamPath(fileName).absoluteFile
//        binding.thumbnail.set(file)
    }

    companion object {
        private const val fileName = "test.pdf"
        private const val PICK_PDF_CODE = 5
    }

    private data class PdfItem(
        val title: String,
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
        protected val thumbnailView: PdfThumbnailView = view.findViewById(R.id.thumbnail)

        abstract fun bind(item: PdfRecyclerItem)
    }

    private class PdfViewHolder(view: View) : PdfBaseViewHolder(view) {
        override fun bind(item: PdfRecyclerItem) {
            if (item is PdfItem) {
                tvTitle.text = item.title
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
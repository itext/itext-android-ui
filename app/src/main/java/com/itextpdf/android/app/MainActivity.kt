package com.itextpdf.android.app

import android.app.Activity
import android.content.Intent
import android.content.res.AssetManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.app.MainActivity.PdfRecyclerItem.Companion.TYPE_PDF
import com.itextpdf.android.app.databinding.ActivityMainBinding
import com.itextpdf.android.app.ui.PdfViewerActivity
import com.itextpdf.android.app.util.FileUtil
import com.itextpdf.android.library.views.PdfThumbnailView
import java.io.File
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * Render a page of a PDF into ImageView
     * @param targetView
     * @throws IOException
     */
    @Throws(IOException::class)
    private fun loadPdf(title: String) {

        //open file in assets
        val fileDescriptor: ParcelFileDescriptor
        val fileName = "$title.pdf"

        // Create file object to read and write on
        val file = File(cacheDir, fileName)
        if (!file.exists()) {
            val assetManager: AssetManager = assets
            FileUtil.copyAsset(assetManager, fileName, file.absolutePath)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.rvPdfList.layoutManager = LinearLayoutManager(this)

        val pdfTitles = mutableListOf("sample_1", "sample_2", "sample_3", "sample_4")
        val data = mutableListOf<PdfRecyclerItem>()

        pdfTitles.forEach { title ->
            data.add(PdfItem(title, Uri.EMPTY) {
                Toast.makeText(this, "$title clicked", Toast.LENGTH_SHORT).show()
            })
            loadPdf(title)
        }

        val adapter = PdfAdapter(data)
        binding.rvPdfList.adapter = adapter

        binding.btnShow.setOnClickListener {
            PdfViewerActivity.launch(this)
        }

        var resultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    // There are no request codes
                    val data: Intent? = result.data
                    // Get the Uri of the selected file
                    val uri: Uri? = data?.data
                    if (uri != null) {
                        val uriString: String = uri.toString()
                        val myFile = File(uriString)
                        val path: String = myFile.absolutePath
                        var displayName: String? = null

                        // get filename
                        if (uriString.startsWith("content://")) {
                            var cursor: Cursor? = null
                            try {
                                cursor =
                                    contentResolver.query(uri, null, null, null, null)
                                if (cursor != null && cursor.moveToFirst()) {
                                    displayName =
                                        cursor.getString(
                                            cursor.getColumnIndexOrThrow(
                                                OpenableColumns.DISPLAY_NAME
                                            )
                                        )
                                }
                            } finally {
                                cursor?.close()
                            }
                        } else if (uriString.startsWith("file://")) {
                            displayName = myFile.name
                        }
                        Log.i("#####", "file name: $displayName")


                        //TODO: reads and prints content (line by line)
//                    val selectedFilename = data.data //The uri with the location of the file
//                    if (selectedFilename != null) {
//                        contentResolver.openInputStream(selectedFilename)?.bufferedReader()?.forEachLine {
//                            Log.i("#####", "filecontent: $it")€
//                        }
//                    }

                        // load selected pdf into thumbnail
//                    binding.thumbnail.set(uri)
                    }
                }
            }
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
                loadPdf(item.title, thumbnailView)

                itemView.setOnClickListener {
                    item.action()
                }
            }
        }

        /**
         * Render a page of a PDF into ImageView
         * @param targetView
         * @throws IOException
         */
        @Throws(IOException::class)
        private fun loadPdf(title: String, targetView: PdfThumbnailView) {
            //open file in assets
            val fileName = "$title.pdf"

            // Create file object to read and write on
            val file = File(targetView.context.cacheDir, fileName)
            if (!file.exists()) {
                val assetManager: AssetManager = targetView.context.assets
                FileUtil.copyAsset(assetManager, fileName, file.absolutePath)
            }

            targetView.set(file)
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
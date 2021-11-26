package com.itextpdf.android.app

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.app.databinding.ActivityMainBinding
import com.itextpdf.android.library.Constants
import com.itextpdf.android.library.pdfDocumentReader
import com.itextpdf.android.library.pdfDocumentWriter
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.canvas.parser.EventType
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData
import com.itextpdf.kernel.pdf.canvas.parser.data.TextRenderInfo
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.FileNotFoundException
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val fileName = "test.pdf"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pdf = pdfDocumentWriter(fileName)
        if (pdf != null) {
            val document = Document(pdf)

            val paragraph = Paragraph("Test paragraph\nTest 2nd paragraph")
            paragraph.setFontSize(50f)
            document.add(paragraph)

            document.close()
            pdf.close()

//            val renderer = paragraph.createRendererSubTree().setParent(document.renderer)
//
//            val width = document.getPageEffectiveArea(PageSize.LETTER).width;
//            val height = document.getPageEffectiveArea(PageSize.LETTER).height;
//            val layoutResult = renderer.layout(
//                LayoutContext(
//                    LayoutArea(
//                        1,
//                        Rectangle(36f, 36f, width, height)
//                    )
//                )
//            )
//
//            print(layoutResult)

//            val numberOfPdfObjects: Int = pdf.getNumberOfPdfObjects()
//            for (i in 1..numberOfPdfObjects) {
//                val obj: PdfObject = pdf.getPdfObject(i)
//                println(obj)
//            }

//            val pRenderer: IRenderer = paragraph.createRendererSubTree().setParent(document.getRenderer())
//            val pLayoutResult: LayoutResult = pRenderer.layout(LayoutContext(LayoutArea(0, Rectangle(500f, 500f))))
//
//            val y: Float = pLayoutResult.getOccupiedArea().getBBox().getY()
//            val x: Float = pLayoutResult.getOccupiedArea().getBBox().getX()

            val readPdf = pdfDocumentReader(fileName)
            val width = readPdf?.defaultPageSize?.width ?: 0f
            val height = readPdf?.defaultPageSize?.height ?: 0f

            Log.i(Constants.LOG_TAG, "PDF page size: $width x $height")

            var rect: Rectangle? = null

            val parser = PdfDocumentContentParser(readPdf)
            parser.processContent(1, object : IEventListener {
                override fun eventOccurred(data: IEventData?, type: EventType?) {

                    val textRenderInfo = data as? TextRenderInfo
                    if (textRenderInfo != null) {

                        val ascentStart = textRenderInfo.ascentLine.startPoint
                        val ascentEnd = textRenderInfo.ascentLine.endPoint
                        val x1 = ascentStart.get(0)
                        val y1 = ascentStart.get(1)

                        Log.i(
                            Constants.LOG_TAG,
                            "Text start line, " +
                                    "x1: ${ascentStart.get(0)}, y1: ${ascentStart.get(1)}, " +
                                    "x2: ${ascentEnd.get(0)}, y2: ${ascentEnd.get(1)}"
                        )

                        val descentStart = textRenderInfo.descentLine.startPoint
                        val descentEnd = textRenderInfo.descentLine.endPoint
                        val x2 = descentStart.get(0)
                        val y2 = descentStart.get(1)

                        Log.i(
                            Constants.LOG_TAG,
                            "Text end line, " +
                                    "x1: ${descentStart.get(0)}, y1: ${descentStart.get(1)}, " +
                                    "x2: ${descentEnd.get(0)}, y2: ${descentEnd.get(1)}"
                        )

                        val rectangle =
                            Rectangle(x1, y1, textRenderInfo.unscaledWidth, abs(y1 - y2))
                        Log.i(
                            Constants.LOG_TAG,
                            "Rectangle: $rectangle"
                        )

                        if (rect == null) {
                            rect = rectangle
                        }
                    }
                }

                override fun getSupportedEvents(): MutableSet<EventType> {
                    return mutableSetOf(EventType.RENDER_TEXT) // return null here to support all events
                }
            })

            binding.thumbnail.setOnClickListener {

                val newName = "new.pdf"
                val new = try {
                    val readFile = getFileStreamPath(fileName).absoluteFile
                    val output = openFileOutput(newName, MODE_PRIVATE)
                    PdfDocument(PdfReader(readFile), PdfWriter(output))
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                    null
                }

                val newDoc = Document(new)
                val r = rect

                if (new != null && r != null) {

                    val p = Paragraph("EDITED")
                    p.setFontSize(50f)
                    p.setFixedPosition(r.left, r.bottom - r.height, r.width)
                    newDoc.add(p)

                    new.close()
                    newDoc.close()

                    val file = getFileStreamPath(newName).absoluteFile
                    binding.thumbnail.set(file)
                }
            }
        }

        val file = getFileStreamPath(fileName).absoluteFile
        binding.thumbnail.set(file)

    }
}
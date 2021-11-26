package com.itextpdf.android.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.app.databinding.ActivityMainBinding
import com.itextpdf.android.library.pdfDocumentReader
import com.itextpdf.android.library.pdfDocumentWriter
import com.itextpdf.kernel.pdf.canvas.parser.EventType
import com.itextpdf.kernel.pdf.canvas.parser.PdfDocumentContentParser
import com.itextpdf.kernel.pdf.canvas.parser.data.IEventData
import com.itextpdf.kernel.pdf.canvas.parser.listener.IEventListener
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val fileName = "test.pdf"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        writePdf()

        val file = getFileStreamPath(fileName).absoluteFile
        binding.thumbnail.set(file)
    }

    private fun writePdf() {

        val pdf = pdfDocumentWriter(fileName)
        if (pdf != null) {
            val document = Document(pdf)

            val paragraph = Paragraph("Test paragraph\nTest 2nd paragraph")
            paragraph.setFontSize(50f)
            document.add(paragraph)

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

            document.close()
            pdf.close()

            val readPdf = pdfDocumentReader(fileName)

            val parser = PdfDocumentContentParser(readPdf)
            parser.processContent(1, object : IEventListener {
                override fun eventOccurred(data: IEventData?, type: EventType?) {
                    println(data)
                }

                override fun getSupportedEvents(): MutableSet<EventType> {
                    return mutableSetOf(EventType.RENDER_TEXT)
                }
            })
        }
    }
}
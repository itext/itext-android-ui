package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import androidx.annotation.ColorInt
import androidx.core.net.toUri
import com.itextpdf.android.library.R
import com.itextpdf.android.library.extensions.getPageIndex
import com.itextpdf.android.library.extensions.isSameAs
import com.itextpdf.android.library.xmlparser.BareXmlParserFactory
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.*
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation
import com.itextpdf.kernel.pdf.annot.PdfTextMarkupAnnotation
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject
import com.itextpdf.kernel.utils.PageRange
import com.itextpdf.kernel.utils.PdfSplitter
import com.itextpdf.kernel.utils.XmlProcessorCreator
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


internal class PdfManipulatorImpl constructor(private val context: Context, originalFileUri: Uri) : PdfManipulator {

    private val fileUtil = FileUtil.getInstance()

    override val workingCopy: File = fileUtil.createTempCopyIfNotExists(context, originalFileUri = originalFileUri)
    private val workingCopyUri: Uri = workingCopy.toUri()

    init {
        // Workaround to fix the error: java.lang.UnsupportedOperationException: This parser does not support specification "Unknown" version "0.0"
        XmlProcessorCreator.setXmlParserFactory(BareXmlParserFactory())
    }

    /**
     * Splits the pdf file at the given uri and creates a new document with the selected page indices and another one for the unselected indices.
     * If selected page indices are empty or contains all the pages, there will only be one document with all pages.
     *
     * @param fileName  the name of the file that will be split. Only relevant for naming the new split documents.
     * @param selectedPageIndices   the list of selected page indices that will be used to create a document with selected and another document
     *  with not selected pages.
     * @param storageFolderPath    the path where the newly created pdf files will be stored
     * @return  the list of uris of the newly created split documents
     */
    override fun splitPdfWithSelection(fileName: String, selectedPageIndices: List<Int>, storageFolderPath: String): List<Uri> {

        val pdfDocument = getPdfDocumentInReadingMode()
        val pdfUriList = mutableListOf<Uri>()
        val selectedPagesNumbers = mutableListOf<Int>()
        val unselectedPageNumbers = mutableListOf<Int>()

        val numberOfPages = pdfDocument.numberOfPages
        val pdfSplitter: PdfSplitter = object : PdfSplitter(pdfDocument) {
            var partNumber = 1
            override fun getNextPdfWriter(documentPageRange: PageRange?): PdfWriter? {
                return try {
                    val name = getSplitDocumentName(
                        fileName,
                        partNumber,
                        selectedPagesNumbers,
                        unselectedPageNumbers
                    )
                    partNumber++

                    val pdfFile = File("$storageFolderPath/$name")
                    pdfUriList.add(pdfFile.toUri())

                    val newOutput = FileOutputStream(pdfFile)
                    PdfWriter(newOutput)
                } catch (ignored: FileNotFoundException) {
                    throw RuntimeException()
                }
            }
        }

        // for splitting we need the actual page number and not the index, therefore add 1 to each index
        selectedPageIndices.forEach { selectedPagesNumbers.add(it + 1) }

        for (i in 1..numberOfPages) {
            if (!selectedPagesNumbers.contains(i)) {
                unselectedPageNumbers.add(i)
            }
        }

        // get page ranges for selected and unselected pages and split document
        val documents = pdfSplitter.extractPageRanges(
            getPageRanges(
                selectedPagesNumbers,
                unselectedPageNumbers,
                numberOfPages
            )
        )
        for (doc in documents) {
            doc.close()
        }
        pdfDocument.close()
        return pdfUriList
    }

    /**
     * Creates the name for the new document created during the split based on initial name and partNumber
     *
     * @param initialFileName   the name of the original document
     * @param partNumber    the part number of the document for which a name should be created. 1 is the first document, 2 the second, ...
     * @param selectedPagesNumbers  the list of selected page numbers
     * @param unselectedPageNumbers  the list of unselected page numbers
     * @return  the name for the split document
     */
    override fun getSplitDocumentName(
        initialFileName: String,
        partNumber: Int,
        selectedPagesNumbers: List<Int>,
        unselectedPageNumbers: List<Int>
    ): String {
        // remove .pdf suffix
        val nameSB = StringBuilder(initialFileName.removeSuffix(".pdf"))
        when (partNumber) {
            1 -> {
                when {
                    selectedPagesNumbers.isNotEmpty() -> {
                        nameSB.append("_selected")
                    }
                    unselectedPageNumbers.isNotEmpty() -> {
                        nameSB.append("_unselected")
                    }
                    else -> {
                        nameSB.append("_part_$partNumber")
                    }
                }
            }
            2 -> {
                if (unselectedPageNumbers.isNotEmpty()) {
                    nameSB.append("_unselected")
                } else {
                    nameSB.append("_part_$partNumber")
                }
            }
            else -> {
                nameSB.append("_part_$partNumber")
            }
        }
        // add .pdf again
        nameSB.append(".pdf")

        return nameSB.toString()
    }

    /**
     * Returns the page ranges for selected and unselected pages that can be used for splitting
     *
     * @param selectedPagesNumbers  a list of page numbers (NOT indices) that were selected
     * @param unselectedPageNumbers a list of page numbers (NOT indices) that were not selected
     * @param numberOfPages the number of pages this pdf document has
     * @return  the list of page ranges (can be empty if selected pages and unselected pages were empty or the numbers were higher than the numberOfPages)
     */
    override fun getPageRanges(
        selectedPagesNumbers: List<Int>,
        unselectedPageNumbers: List<Int>,
        numberOfPages: Int
    ): List<PageRange> {
        // setup page ranges for selected and unselected pages
        val pageRanges = mutableListOf<PageRange>()
        if (selectedPagesNumbers.isNotEmpty()) {
            val selectedRange = PageRange()
            selectedPagesNumbers.forEach {
                if (it <= numberOfPages) selectedRange.addSinglePage(
                    it
                )
            }
            pageRanges.add(selectedRange)
        }
        if (unselectedPageNumbers.isNotEmpty()) {
            val unselectedRange = PageRange()
            unselectedPageNumbers.forEach {
                if (it <= numberOfPages) unselectedRange.addSinglePage(
                    it
                )
            }
            pageRanges.add(unselectedRange)
        }
        return pageRanges
    }

    @Throws(java.lang.Exception::class)
    override fun addTextAnnotationToPdf(
        title: String?,
        text: String,
        pageIndex: Int,
        x: Float,
        y: Float,
        bubbleSize: Float,
        @ColorInt bubbleColor: Int
    ): File {
        val tempFile = fileUtil.createTempCopy(context, workingCopy)
        val resultingFile: File = getPdfDocumentInStampingMode(tempFile)
            .use { pdfDoc ->

                val appearance = getTextAnnotationAppearance(pdfDoc, bubbleColor, bubbleSize)
                val annotation: PdfAnnotation =
                    PdfTextAnnotation(Rectangle(x, y, bubbleSize, bubbleSize))
                        .setContents(text)
                        .setNormalAppearance(appearance?.pdfObject)

                if (title != null) {
                    annotation.title = PdfString(title)
                }

                pdfDoc.getPage(pageIndex + 1).addAnnotation(annotation)
                pdfDoc.close()

                tempFile
            }

        return fileUtil.overrideFile(resultingFile, workingCopyUri)
    }

    override fun addMarkupAnnotationToPdf(pageIndex: Int, rect: Rectangle, color: Color): File {

        val tempFile = fileUtil.createTempCopy(context, workingCopy)
        val resultingFile: File = getPdfDocumentInStampingMode(tempFile)
            .use { pdfDoc ->

                // Specify quad points in Z-like order
                // [0,1] x1,y1   [2,3] x2,y2
                // [4,5] x3,y3   [6,7] x4,y4
                // Specify quad points in Z-like order
                // [0,1] x1,y1   [2,3] x2,y2
                // [4,5] x3,y3   [6,7] x4,y4
                val quads = FloatArray(8)
                quads[0] = rect.x
                quads[1] = rect.y + rect.height
                quads[2] = rect.x + rect.width
                quads[3] = quads[1]
                quads[4] = quads[0]
                quads[5] = rect.y
                quads[6] = quads[2]
                quads[7] = quads[5]

                val appearance = getHighlightAppearance(pdfDoc, rect, color)
                val markupAnnotation = PdfTextMarkupAnnotation(rect, PdfName.Highlight, quads)
                    .setColor(color)
                    .setNormalAppearance(appearance.pdfObject)

                pdfDoc.getPage(pageIndex + 1).addAnnotation(markupAnnotation)
                pdfDoc.close()

                tempFile
            }

        return fileUtil.overrideFile(resultingFile, workingCopyUri)
    }

    override fun removeAnnotationFromPdf(annotationToRemove: PdfAnnotation): File {


        val tempFile = fileUtil.createTempCopy(context, workingCopy)
        val resultingFile: File = getPdfDocumentInStampingMode(tempFile).use { pdfDocument ->

            val pageNumber = annotationToRemove.page.getPageIndex() + 1
            val page = pdfDocument.getPage(pageNumber)

            for (ann in page.annotations) {
                if (annotationToRemove.isSameAs(ann)) {
                    page.removeAnnotation(ann)
                    break
                }
            }
            tempFile
        }

        return fileUtil.overrideFile(resultingFile, workingCopyUri)
    }

    override fun editAnnotationFromPdf(annotation: PdfAnnotation, title: String?, text: String): File {

        val tempFile = fileUtil.createTempCopy(context, workingCopy)

        val resultingFile: File = getPdfDocumentInStampingMode(tempFile).use { pdfDocument ->

            val pageNumber = annotation.page.getPageIndex() + 1
            val page = pdfDocument.getPage(pageNumber)

            for (ann in page.annotations) {
                if (annotation.isSameAs(ann)) {
                    ann.setContents(text)
                    if (title != null) {
                        ann.title = PdfString(title)
                    }
                }
            }
            tempFile
        }

        return fileUtil.overrideFile(resultingFile, workingCopyUri)
    }

    private fun getHighlightAppearance(
        pdfDocument: PdfDocument,
        rectangle: Rectangle,
        color: Color
    ): PdfFormXObject {
        val commentXObj = PdfFormXObject(rectangle)
        val canvas = PdfCanvas(commentXObj, pdfDocument)
        val extGState = PdfExtGState()

        extGState.blendMode = PdfExtGState.BM_MULTIPLY
        canvas.setExtGState(extGState)
        canvas.rectangle(rectangle.x.toDouble(), rectangle.y.toDouble(), rectangle.width.toDouble(), rectangle.height.toDouble())
        canvas.setFillColor(color)
        canvas.fill()
        canvas.release()

        return commentXObj
    }

    private fun getTextAnnotationAppearance(pdfDocument: PdfDocument, @ColorInt color: Int, bubbleSize: Float): PdfFormXObject? {
        var commentXObj: PdfFormXObject? = null
        // set imageSize larger to increase quality
        val imageSize = bubbleSize * 10
        val imageByteArray = ImageUtil.getResourceAsByteArray(context, R.drawable.ic_speech_bubble, imageSize.toInt(), color)

        if (imageByteArray != null) {
            val itextImageData = ImageDataFactory.createPng(imageByteArray)

            // draw a speech bubble on a 30x30 canvas
            commentXObj = PdfFormXObject(Rectangle(imageSize, imageSize))
            val canvas = PdfCanvas(commentXObj, pdfDocument)
            canvas.addImageAt(itextImageData, 0f, 0f, true)
        }
        return commentXObj
    }

    /**
     * Returns a pdfDocument object with the given uri in reading mode.
     *
     * @return  the pdf document in reading mode
     */
    override fun getPdfDocumentInReadingMode(): PdfDocument {
        val pdfReader = PdfReader(workingCopy)
        return PdfDocument(pdfReader)
    }

    override fun getPdfDocumentInStampingMode(destFile: File): PdfDocument {

        val inputStream = workingCopy.inputStream()
        val outputStream = FileOutputStream(destFile)

        return PdfDocument(
            PdfReader(inputStream),
            PdfWriter(outputStream)
        )

    }
}
package com.itextpdf.android.library.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import com.itextpdf.android.library.R
import com.itextpdf.android.library.extensions.pdfDocumentInReadingMode
import com.itextpdf.android.library.extensions.pdfDocumentInStampingMode
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfString
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject
import com.itextpdf.kernel.utils.PageRange
import com.itextpdf.kernel.utils.PdfSplitter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


object PdfManipulator {

    /**
     * Splits the pdf file at the given uri and creates a new document with the selected page indices and another one for the unselected indices.
     * If selected page indices are empty or contains all the pages, there will only be one document with all pages.
     *
     * @param context   the android context
     * @param fileUri   the uri of the pdf file that should be split
     * @param fileName  the name of the file that will be split. Only relevant for naming the new split documents.
     * @param selectedPageIndices   the list of selected page indices that will be used to create a document with selected and another document
     *  with not selected pages.
     * @param storageFolderPath    the path where the newly created pdf files will be stored
     * @return  the list of uris of the newly created split documents
     */
    fun splitPdfWithSelection(
        context: Context,
        fileUri: Uri,
        fileName: String,
        selectedPageIndices: List<Int>,
        storageFolderPath: String
    ): List<Uri> {
        val pdfDocument = context.pdfDocumentInReadingMode(fileUri)
        val pdfUriList = mutableListOf<Uri>()
        if (pdfDocument != null) {
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
        }
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
    private fun getSplitDocumentName(
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
    private fun getPageRanges(
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
    fun addTextAnnotationToPdf(
        context: Context,
        fileUri: Uri,
        destinationFile: File,
        title: String?,
        text: String,
        pageNumber: Int,
        x: Float,
        y: Float,
        bubbleSize: Float,
        bubbleColor: String
    ) {
        val pdfDocument = context.pdfDocumentInStampingMode(fileUri, destinationFile)
        pdfDocument?.let { pdfDoc ->
            val appearance = getCommentAppearance(context, pdfDoc, bubbleColor, bubbleSize)
            val ann: PdfAnnotation =
                PdfTextAnnotation(Rectangle(x, y, bubbleSize, bubbleSize))
                    .setContents(text)
                    .setNormalAppearance(appearance?.pdfObject)

            if (title != null) {
                ann.title = PdfString(title)
            }

            pdfDoc.getPage(pageNumber).addAnnotation(ann)
            pdfDoc.close()

//            val page: PdfPage = pdfDoc.firstPage
//            val sticky = page.annotations[0]
//            val stickyRectangle: Rectangle = sticky.rectangle.toRectangle()
//            val replySticky = PdfTextAnnotation(stickyRectangle)
//                .setStateModel(PdfString("Review"))
//                .setState(PdfString("Accepted"))
//                .setIconName(PdfName("Comment")) // This method sets an annotation to which the current annotation is "in reply".
//                // Both annotations shall be on the same page of the document.
//                .setInReplyTo(sticky) // This method sets the text label that will be displayed in the title bar of the annotation's pop-up window
//                // when open and active. This entry shall identify the user who added the annotation.
//                .setText(PdfString("Bruno")) // This method sets the text that will be displayed for the annotation or the alternate description,
//                // if this type of annotation does not display text.
//                .setContents("Accepted by Bruno") // This method sets a complete set of enabled and disabled flags at once. If not set specifically
//                // the default value is 0.
//                // The argument is an integer interpreted as set of one-bit flags
//                // specifying various characteristics of the annotation.
//                .setFlags(sticky.flags + PdfAnnotation.HIDDEN)
//            pdfDoc.firstPage.addAnnotation(replySticky)
//            pdfDoc.close()


//            val rect = Rectangle(150f, 770f, 50f, 50f)
//            val annotation: PdfAnnotation = PdfCircleAnnotation(rect)
//                .setBorderStyle(PdfAnnotation.STYLE_DASHED)
//                .setDashPattern(
//                    PdfArray(
//                        intArrayOf(
//                            3,
//                            2
//                        )
//                    )
//                ) // This method sets the text that will be displayed for the annotation or the alternate description,
//                // if this type of annotation does not display text.
//                .setContents("Circle")
//                .setTitle(PdfString("Circle"))
//                .setColor(ColorConstants.BLUE) // Set to print the annotation when the page is printed
//                .setFlags(PdfAnnotation.PRINT)
//                .setBorder(PdfArray(floatArrayOf(0f, 0f, 2f))) // Set the interior color
//                .put(PdfName.IC, PdfArray(intArrayOf(1, 0, 0)))
        }
    }

    private fun getCommentAppearance(
        context: Context,
        pdfDocument: PdfDocument,
        colorString: String,
        bubbleSize: Float
    ): PdfFormXObject? {
        var commentXObj: PdfFormXObject? = null

        val d = AppCompatResources.getDrawable(
            context,
            R.drawable.ic_annotation
        )?.constantState?.newDrawable()?.mutate()
        if (d != null) {
            val imageSize = bubbleSize * 3

            val wrappedDrawable = DrawableCompat.wrap(d)
            DrawableCompat.setTint(
                wrappedDrawable,
                Color.parseColor(colorString)
            )

            val bitmap = d.toBitmap(imageSize.toInt(), imageSize.toInt(), Bitmap.Config.ARGB_8888)
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            val imageByteArray: ByteArray = stream.toByteArray()
            val itextImageData = ImageDataFactory.createPng(imageByteArray)

            // draw a speech bubble on a 30x30 canvas
            commentXObj = PdfFormXObject(Rectangle(imageSize, imageSize))
            val canvas = PdfCanvas(commentXObj, pdfDocument)
            canvas.addImageAt(itextImageData, 0f, 0f, true)
        }
        return commentXObj
    }

    //TODO: only for testing
    fun writePdf(output: FileOutputStream) {
        try {
            val pdf = PdfDocument(PdfWriter(output))
            val document = Document(pdf)

            val paragraph = Paragraph("Test paragraph")
            paragraph.setFontSize(50f)
            document.add(paragraph)

            document.close()
            pdf.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.itextpdf.android.library.R
import com.itextpdf.android.library.extensions.isSameAs
import com.itextpdf.android.library.extensions.pdfDocumentInReadingMode
import com.itextpdf.android.library.extensions.pdfDocumentInStampingMode
import com.itextpdf.forms.xfdf.AnnotObject
import com.itextpdf.forms.xfdf.XfdfConstants
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfName
import com.itextpdf.kernel.pdf.PdfString
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.itextpdf.kernel.pdf.annot.PdfMarkupAnnotation
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation
import com.itextpdf.kernel.pdf.annot.PdfTextMarkupAnnotation
import com.itextpdf.kernel.pdf.canvas.PdfCanvas
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject
import com.itextpdf.kernel.utils.PageRange
import com.itextpdf.kernel.utils.PdfSplitter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream


object PdfManipulator {

    private val fileUtil = FileUtil.getInstance()

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
        title: String?,
        text: String,
        pageNumber: Int,
        x: Float,
        y: Float,
        bubbleSize: Float,
        bubbleColor: String
    ): File {
        val tempFile = fileUtil.createTempCopy(context, File(fileUri.path))
        val resultingFile: File = context.pdfDocumentInStampingMode(fileUri, tempFile)
            .use { pdfDoc ->

                val appearance = getCommentAppearance(context, pdfDoc, bubbleColor, bubbleSize)
                val annotation: PdfAnnotation =
                    PdfTextAnnotation(Rectangle(x, y, bubbleSize, bubbleSize))
                        .setContents(text)
                        .setNormalAppearance(appearance?.pdfObject)

                if (title != null) {
                    annotation.title = PdfString(title)
                }

                pdfDoc.getPage(pageNumber).addAnnotation(annotation)
                pdfDoc.close()

                tempFile
            }

        return fileUtil.overrideFile(resultingFile, fileUri)

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

    fun removeAnnotationFromPdf(
        context: Context,
        fileUri: Uri,
        pageNumber: Int,
        annotation: PdfAnnotation
    ): File {
        val tempFile = fileUtil.createTempCopy(context, File(fileUri.path))
        val resultingFile: File =
            context.pdfDocumentInStampingMode(fileUri, tempFile).use { pdfDocument ->
                val page = pdfDocument.getPage(pageNumber)
                for (ann in page.annotations) {
                    if (annotation.isSameAs(ann)) {
                        page.removeAnnotation(ann)
                        break
                    }
                }
                tempFile
            }

        return fileUtil.overrideFile(resultingFile, fileUri)
    }

    fun editAnnotationFromPdf(
        context: Context,
        fileUri: Uri,
        pageNumber: Int,
        annotation: PdfAnnotation,
        title: String?,
        text: String
    ): File {
        val tempFile = fileUtil.createTempCopy(context, File(fileUri.path))
        val resultingFile: File =
            context.pdfDocumentInStampingMode(fileUri, tempFile).use { pdfDocument ->
                val page = pdfDocument.getPage(pageNumber)
                for (ann in page.annotations) {
                    if (annotation.isSameAs(ann)) {
                        ann.setContents(text)
                        if (title != null) {
                            annotation.title = PdfString(title)
                        }
                    }
                }
                tempFile
            }

        return fileUtil.overrideFile(resultingFile, fileUri)
    }

    fun addTextMarkupAnnotationToPdf(
        context: Context,
        fileUri: Uri,
        pageNumber: Int,
        x: Float,
        y: Float,
        size: Float,
        color: String
    ): File {

        val tempFile = fileUtil.createTempCopy(context, File(fileUri.path))
        val resultingFile: File = context.pdfDocumentInStampingMode(fileUri, tempFile)
            .use { pdfDoc ->

                val rect = Rectangle(x, y, size, size)
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

                val page = pdfDoc.getPage(pageNumber)
                val highlightColor: Color = DeviceRgb(1f, 1f, 0f)

                val markupAnnotation = PdfTextMarkupAnnotation(rect, PdfName.Highlight, quads)
                    .setColor(highlightColor)
                page.addAnnotation(markupAnnotation);


                //TODO: this is only for testing -> adds color at correct position that cannot be removed afterwards
//                val canvas = PdfCanvas(page)
//                val extGState = PdfExtGState()
//                extGState.blendMode = PdfExtGState.BM_MULTIPLY
//                canvas.setExtGState(extGState)
//                canvas.rectangle(rect.x.toDouble(), rect.y.toDouble(), rect.width.toDouble(), rect.height.toDouble())
//                canvas.setFillColor(highlightColor)
//                canvas.fill()
//                canvas.release()

                pdfDoc.close()

                tempFile
            }

        return fileUtil.overrideFile(resultingFile, fileUri)
    }

    private fun getCommentAppearance(
        context: Context,
        pdfDocument: PdfDocument,
        colorString: String,
        bubbleSize: Float
    ): PdfFormXObject? {
        var commentXObj: PdfFormXObject? = null
        val imageSize = bubbleSize * 3
        val imageByteArray = ImageUtil.getResourceAsByteArray(context, R.drawable.ic_annotation, imageSize.toInt(), colorString)

        if (imageByteArray != null) {
            val itextImageData = ImageDataFactory.createPng(imageByteArray)

            // draw a speech bubble on a 30x30 canvas
            commentXObj = PdfFormXObject(Rectangle(imageSize, imageSize))
            val canvas = PdfCanvas(commentXObj, pdfDocument)
            canvas.addImageAt(itextImageData, 0f, 0f, true)
        }
        return commentXObj
    }

//    private fun addTextMarkupAnnotationToPdf(subtype: PdfName, annotObject: AnnotObject, color: Color) {
//        val rect: Rectangle = readAnnotRect(annotObject)
//        val quads: FloatArray = readAnnotQuadPoints(annotObject)
//        val pdfAnnot = PdfTextMarkupAnnotation(rect, subtype, quads)
//        addCommonAnnotationAttributes(pdfAnnot, annotObject, color)
//        addMarkupAnnotationAttributes(pdfAnnot, annotObject)
//        val page: Int = readAnnotPage(annotObject)
//        pdfDocument.getPage(page).addAnnotation(pdfAnnot)
//        addPopupAnnotation(page, pdfAnnot, annotObject.popup)
//    }
//    private fun readAnnotRect(annotObject: AnnotObject): Rectangle {
//        val rect: String = annotObject.getAttributeValue(XfdfConstants.RECT)
//        return XfdfObjectReadingUtils.convertRectFromString(rect, this.transform)
//    }
//
//    private fun readAnnotQuadPoints(annotObject: AnnotObject): FloatArray {
//        val coords: String = annotObject.getAttributeValue(XfdfConstants.COORDS)
//        return XfdfObjectReadingUtils.convertQuadPointsFromCoordsString(coords, this.transform)
//    }
//
//    private fun readAnnotPage(annotObject: AnnotObject): Int {
//        // iText pages are 1-indexed
//        val page: Int = 1 + annotObject.getAttribute(XfdfConstants.PAGE).getValue().toInt()
//        return this.pageShift + page
//    }

//    private fun addCommonAnnotationAttributes(annotation: PdfAnnotation, annotObject: AnnotObject, color: Color) {
//        annotation.flags = XfdfObjectReadingUtils.convertFlagsFromString(annotObject.getAttributeValue(XfdfConstants.FLAGS))
//        annotation.setColor(color)
//        val dateString = annotObject.getAttributeValue(XfdfConstants.DATE)
//        if (dateString != null) {
//            annotation.date = PdfString(dateString)
//        }
//        val name = annotObject.getAttributeValue(XfdfConstants.NAME)
//        if (name != null) {
//            annotation.name = PdfString(name)
//            annotMap.put(name, annotation)
//            // add pending replies
//            for (reply in replyMap.getOrDefault(name, Collections.emptyList())) {
//                reply.inReplyTo = annotation
//            }
//            replyMap.remove(name)
//        }
//        val titleString = annotObject.getAttributeValue(XfdfConstants.TITLE)
//        if (titleString != null) {
//            annotation.title = PdfString(titleString)
//        }
//    }

//    private fun addPopupAnnotation(page: Int, parent: PdfMarkupAnnotation, popup: AnnotObject?) {
//        if (popup != null) {
//            val pdfPopupAnnot = PdfPopupAnnotation(readAnnotRect(popup))
//            val openProp = popup.getAttributeValue("open")
//            val open = "yes" == openProp
//            pdfPopupAnnot.setOpen(open).flags = XfdfObjectReadingUtils.convertFlagsFromString(popup.getAttributeValue(XfdfConstants.FLAGS))
//            parent.popup = pdfPopupAnnot
//            pdfDocument.getPage(page).addAnnotation(pdfPopupAnnot)
//        }
//    }

//    private fun addMarkupAnnotationAttributes(annotation: PdfMarkupAnnotation, annotObject: AnnotObject) {
//        val creationDateString = annotObject.getAttributeValue(XfdfConstants.CREATION_DATE)
//        if (creationDateString != null) {
//            annotation.creationDate = PdfString(creationDateString)
//        }
//        val subjectString = annotObject.getAttributeValue(XfdfConstants.SUBJECT)
//        if (subjectString != null) {
//            annotation.subject = PdfString(subjectString)
//        }
//        val intent = annotObject.getAttributeValue("IT")
//        if (intent != null && !intent.isBlank()) {
//            annotation.intent = PdfName(intent)
//        }
//        val irpt = annotObject.getAttributeValue(XfdfConstants.IN_REPLY_TO)
//        if (irpt != null && !irpt.isBlank()) {
//            if ("group".equals(annotObject.getAttributeValue(XfdfConstants.REPLY_TYPE), ignoreCase = true)) {
//                annotation.replyType = PdfName.Group
//            }
//            val inReplyToAnnot: PdfAnnotation = annotMap.get(irpt)
//            if (inReplyToAnnot != null) {
//                annotation.inReplyTo = inReplyToAnnot
//            } else {
//                // queue for later
//                var queued: MutableList<PdfMarkupAnnotation?>? = replyMap.get(irpt)
//                if (queued == null) {
//                    queued = ArrayList()
//                    queued.add(annotation)
//                    replyMap.put(irpt, queued)
//                } else {
//                    queued.add(annotation)
//                }
//            }
//        }
//        val rc = annotObject.contentsRichText
//        if (rc != null && !rc.toString().isBlank()) {
//            val rcString = rc.toString().trim { it <= ' ' }
//            annotation.richText = PdfString(rcString)
//        }
//        val plainContents = annotObject.contents
//        if (plainContents != null && !plainContents.toString().isBlank()) {
//            val pcString = plainContents.toString().trim { it <= ' ' }
//            annotation.contents = PdfString(pcString)
//        }
//    }

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
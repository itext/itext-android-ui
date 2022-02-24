package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.itextpdf.android.library.extensions.pdfDocumentReader
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.kernel.utils.PageRange
import com.itextpdf.kernel.utils.PdfSplitter
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
     * @return  the list of uris of the newly created split documents
     */
    fun splitPdfWithSelection(
        context: Context,
        fileUri: Uri,
        fileName: String,
        selectedPageIndices: List<Int>
    ): List<Uri> {
        val pdfDocument = context.pdfDocumentReader(fileUri)
        val pdfUriList = mutableListOf<Uri>()
        val cacheFolderPath = context.externalCacheDir?.absolutePath
        if (pdfDocument != null && cacheFolderPath != null) {
            val numberOfPages = pdfDocument.numberOfPages
            val pdfSplitter: PdfSplitter = object : PdfSplitter(pdfDocument) {
                var partNumber = 1
                override fun getNextPdfWriter(documentPageRange: PageRange?): PdfWriter? {
                    return try {
                        val name = getSplitDocumentName(fileName, partNumber)
                        partNumber++

                        val pdfFile = File("$cacheFolderPath/$name")
                        pdfUriList.add(pdfFile.toUri())

                        val newOutput = FileOutputStream(pdfFile)
                        PdfWriter(newOutput)
                    } catch (ignored: FileNotFoundException) {
                        throw RuntimeException()
                    }
                }
            }

            val selectedPagesNumbers = mutableListOf<Int>()
            // for splitting we need the actual page number and not the index, therefore add 1 to each index
            selectedPageIndices.forEach { selectedPagesNumbers.add(it + 1) }

            val unselectedPageNumbers = mutableListOf<Int>()
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
     * @return  the name for the split document
     */
    private fun getSplitDocumentName(initialFileName: String, partNumber: Int): String {
        // remove .pdf suffix
        val nameSB = StringBuilder(initialFileName.removeSuffix(".pdf"))
        when (partNumber) {
            1 -> {
                nameSB.append("_selected")
            }
            2 -> {
                nameSB.append("_unselected")
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
}
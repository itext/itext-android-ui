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
     * TODO
     *
     * @param context
     * @param fileUri
     * @param fileName
     * @param selectedPageIndices
     * @return
     */
    fun splitPdfWithWithSelection(
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
                        // remove .pdf suffix
                        val nameSB = StringBuilder(fileName.removeSuffix(".pdf"))
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

                        val name = nameSB.toString()

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

            // setup page ranges for selected and unselected pages
            val selectedRange = PageRange()
            selectedPagesNumbers.forEach { if (it < numberOfPages) selectedRange.addSinglePage(it) }
            val unselectedRange = PageRange()
            unselectedPageNumbers.forEach { if (it < numberOfPages) unselectedRange.addSinglePage(it) }

            // split document
            val documents = pdfSplitter.extractPageRanges(listOf(selectedRange, unselectedRange))
            for (doc in documents) {
                doc.close()
            }
            pdfDocument.close()
        }
        return pdfUriList
    }
}
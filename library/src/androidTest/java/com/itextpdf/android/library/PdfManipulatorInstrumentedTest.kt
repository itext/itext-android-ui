package com.itextpdf.android.library

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.itextpdf.android.library.extensions.getFileName
import com.itextpdf.android.library.util.FileUtil
import com.itextpdf.android.library.util.PdfManipulator
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Tests the functions in the PdfManipulator
 */
@RunWith(AndroidJUnit4::class)
class PdfManipulatorInstrumentedTest {

    private val appContext = InstrumentationRegistry.getInstrumentation().targetContext
    private val fileName = "sample_3.pdf"
    private val file = FileUtil.getInstance().loadFileFromAssets(appContext, fileName)
    private val uri = Uri.fromFile(file)

    @Test
    fun splitPdfWithSelection() {

        val pdfManipulator = PdfManipulator.create(appContext, uri)
        val pdfFile = pdfManipulator.getPdfDocumentInReadingMode()

        assertEquals(true, file.exists())
        assertEquals(true, file.isFile)
        assertEquals(10, pdfFile.numberOfPages)

        // ##############################################################################
        // ## Test: Valid selection - pages: 1, 2, 6; original pdf number of pages: 10 ##
        // ##############################################################################
        var selectedPageIndices = listOf(0, 1, 5)
        var splitPdfUriList = pdfManipulator.splitPdfWithSelection(
            fileName,
            selectedPageIndices,
            appContext.cacheDir.absolutePath
        )

        assertEquals(2, splitPdfUriList.size)
        assertEquals("sample_3_selected.pdf", appContext.getFileName(splitPdfUriList[0]))
        assertEquals("sample_3_unselected.pdf", appContext.getFileName(splitPdfUriList[1]))

        var selectedPagesPdf = PdfManipulator.create(appContext, splitPdfUriList[0]).getPdfDocumentInReadingMode()
        var unselectedPagesPdf = PdfManipulator.create(appContext, splitPdfUriList[1]).getPdfDocumentInReadingMode()

        assertEquals(3, selectedPagesPdf.numberOfPages)
        assertEquals(7, unselectedPagesPdf.numberOfPages)
        assertEquals(
            pdfFile.getPage(1).contentBytes.size,
            selectedPagesPdf.firstPage.contentBytes.size
        )
        assertEquals(
            pdfFile.getPage(2).contentBytes.size,
            selectedPagesPdf.getPage(2).contentBytes.size
        )
        assertEquals(
            pdfFile.getPage(6).contentBytes.size,
            selectedPagesPdf.lastPage.contentBytes.size
        )
        assertEquals(
            pdfFile.getPage(3).contentBytes.size,
            unselectedPagesPdf.firstPage.contentBytes.size
        )
        assertEquals(
            pdfFile.getPage(10).contentBytes.size,
            unselectedPagesPdf.lastPage.contentBytes.size
        )

        // close pdf documents
        selectedPagesPdf.close()
        unselectedPagesPdf.close()

        // ########################################################################
        // ## Test: Valid selection - no pages; original pdf number of pages: 10 ##
        // ########################################################################
        selectedPageIndices = listOf()
        splitPdfUriList = PdfManipulator.create(appContext, uri).splitPdfWithSelection(
            fileName,
            selectedPageIndices,
            appContext.cacheDir.absolutePath
        )

        assertEquals(1, splitPdfUriList.size)
        assertEquals("sample_3_unselected.pdf", appContext.getFileName(splitPdfUriList[0]))

        unselectedPagesPdf = PdfManipulator.create(appContext, splitPdfUriList[0]).getPdfDocumentInReadingMode()

        assertEquals(10, unselectedPagesPdf.numberOfPages)
        for (i in 1..pdfFile.numberOfPages) {
            assertEquals(
                pdfFile.getPage(i).contentBytes.size,
                unselectedPagesPdf.getPage(i).contentBytes.size
            )
        }

        // close pdf documents
        selectedPagesPdf.close()
        unselectedPagesPdf.close()

        // ##############################################################################
        // ## Test: Invalid selection - pages: 2, 13; original pdf number of pages: 10 ##
        // ##############################################################################
        selectedPageIndices = listOf(1, 12)
        splitPdfUriList = PdfManipulator.create(appContext, uri).splitPdfWithSelection(
            fileName,
            selectedPageIndices,
            appContext.cacheDir.absolutePath
        )

        assertEquals(2, splitPdfUriList.size)
        assertEquals("sample_3_selected.pdf", appContext.getFileName(splitPdfUriList[0]))
        assertEquals("sample_3_unselected.pdf", appContext.getFileName(splitPdfUriList[1]))

        selectedPagesPdf = PdfManipulator.create(appContext, splitPdfUriList[0]).getPdfDocumentInReadingMode()
        unselectedPagesPdf = PdfManipulator.create(appContext, splitPdfUriList[1]).getPdfDocumentInReadingMode()

        assertEquals(1, selectedPagesPdf.numberOfPages)
        assertEquals(9, unselectedPagesPdf.numberOfPages)
        assertEquals(
            pdfFile.getPage(2).contentBytes.size,
            selectedPagesPdf.firstPage.contentBytes.size
        )
        assertEquals(
            pdfFile.getPage(1).contentBytes.size,
            unselectedPagesPdf.firstPage.contentBytes.size
        )
        assertEquals(
            pdfFile.getPage(10).contentBytes.size,
            unselectedPagesPdf.lastPage.contentBytes.size
        )

        // close pdf documents
        selectedPagesPdf.close()
        unselectedPagesPdf.close()

        pdfFile.close()
    }

    /**
     * Test: Valid selection - all pages; original pdf number of pages: 10
     */
    @Test
    fun testSplitAllPages() {

        val pdfFile = PdfManipulator.create(appContext, uri).getPdfDocumentInReadingMode()

        val selectedPageIndices = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val splitPdfUriList = PdfManipulator.create(appContext, uri).splitPdfWithSelection(
            fileName,
            selectedPageIndices,
            appContext.cacheDir.absolutePath
        )

        assertEquals(1, splitPdfUriList.size)
        assertEquals("sample_3_selected.pdf", appContext.getFileName(splitPdfUriList[0]))

        val selectedPagesPdf = PdfManipulator.create(appContext, splitPdfUriList[0]).getPdfDocumentInReadingMode()

        assertEquals(10, selectedPagesPdf.numberOfPages)
        for (i in 1..pdfFile.numberOfPages) {
            assertEquals(
                pdfFile.getPage(i).contentBytes.size,
                selectedPagesPdf.getPage(i).contentBytes.size
            )
        }

        // close pdf documents
        selectedPagesPdf.close()
        //unselectedPagesPdf.close()
    }
}
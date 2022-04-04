package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File


@LargeTest
@RunWith(AndroidJUnit4::class)
class PdfManipulatorImplTest {

    private lateinit var appContext: Context
    private val fileName = "sample_3.pdf"
    private lateinit var file: File
    private lateinit var uri: Uri
    private lateinit var sut: PdfManipulator

    @Deprecated("Storage path will be removed soon.")
    private lateinit var storagePath: String

    @Before
    fun setup() {

        appContext = InstrumentationRegistry.getInstrumentation().targetContext

        clearCacheDir()

        file = FileUtil.getInstance().loadFileFromAssets(appContext, fileName)
        uri = Uri.fromFile(file)
        sut = PdfManipulator.create(appContext, uri)
        storagePath = appContext.cacheDir.absolutePath
    }

    private fun clearCacheDir() {
        val cacheDirs: Array<File> = ContextCompat.getExternalCacheDirs(appContext)
        cacheDirs.forEach { cacheDir ->
            cacheDir.deleteRecursively()
        }
    }


    /**
     * GIVEN file exists
     * AND file is file
     * WHEN file is opened in reading mode
     * THEN pdf-document has 10 pages
     */
    @Test
    fun testOpenDocumentInReadingMode() {

        // GIVEN
        assertThat(file.exists()).isTrue()
        assertThat(file.isFile).isTrue()

        // WHEN
        val pdfFile: PdfDocument = sut.getPdfDocumentInReadingMode()

        // THEN
        assertThat(pdfFile.numberOfPages).isEqualTo(10)

    }

    /**
     * GIVEN user has selected pages 0, 1 and 5.
     * WHEN user split's PDF
     * THEN result contains list with exactly 2 uris
     * AND the first result-uri is PDF with 3 pages
     * AND the second result-uri is PDF with 7 pages
     * AND the pages of the first document are equal to the selected pages of the original document
     * AND the pages of the second document are equal to the unselected pages of the original document
     */
    @Test
    fun testSplitDocument() {

        // GIVEN
        val selectedPageIndices = listOf(0, 1, 5)
        val originalPdf = sut.getPdfDocumentInReadingMode()

        // WHEN
        val result: List<Uri> = sut.splitPdfWithSelection(fileName, selectedPageIndices, storagePath)
        val documentOfSelectedPages = PdfManipulator.create(appContext, result.first()).getPdfDocumentInReadingMode()
        val documentOfUnselectedPages = PdfManipulator.create(appContext, result[1]).getPdfDocumentInReadingMode()

        // THEN
        assertThat(result).hasSize(2)
        assertThat(documentOfSelectedPages.numberOfPages).isEqualTo(3)
        assertThat(documentOfUnselectedPages.numberOfPages).isEqualTo(7)

        // Compare pages of document that contains the selected pages
        assertThatPagesAreEqual(documentOfSelectedPages.getPage(1), originalPdf.getPage(1))
        assertThatPagesAreEqual(documentOfSelectedPages.getPage(2), originalPdf.getPage(2))
        assertThatPagesAreEqual(documentOfSelectedPages.getPage(3), originalPdf.getPage(6))

        // Compare pages of document that contains the unselected  pages
        assertThatPagesAreEqual(documentOfUnselectedPages.getPage(1), originalPdf.getPage(3))
        assertThatPagesAreEqual(documentOfUnselectedPages.getPage(2), originalPdf.getPage(4))
        assertThatPagesAreEqual(documentOfUnselectedPages.getPage(3), originalPdf.getPage(5))
        assertThatPagesAreEqual(documentOfUnselectedPages.getPage(4), originalPdf.getPage(7))
        assertThatPagesAreEqual(documentOfUnselectedPages.getPage(5), originalPdf.getPage(8))
        assertThatPagesAreEqual(documentOfUnselectedPages.getPage(6), originalPdf.getPage(9))
        assertThatPagesAreEqual(documentOfUnselectedPages.getPage(7), originalPdf.getPage(10))

    }

    /**
     * GIVEN user not selected any pages
     * WHEN user splits PDF
     * THEN result list contains exactly 1 uri
     * AND result file has the same page-count as original file
     * AND all pages from resulting file are equal to pages of original file
     */
    @Test
    fun testZeroPagesSelected() {

        // GIVEN
        val selectedPageIndices = emptyList<Int>()
        val originalFile = sut.getPdfDocumentInReadingMode()

        // WHEN
        val resultList: List<Uri> = sut.splitPdfWithSelection(fileName, selectedPageIndices, storagePath)
        val resultFile: PdfDocument = PdfManipulator.create(appContext, resultList.first()).getPdfDocumentInReadingMode()

        // THEN
        assertThat(resultList.size).isEqualTo(1)
        assertThat(resultFile.numberOfPages).isEqualTo(originalFile.numberOfPages)

        for (i in 1..resultFile.numberOfPages) {
            assertThatPagesAreEqual(resultFile.getPage(i), originalFile.getPage(i))
        }

    }

    /**
     * GIVEN user has selected all pages
     * WHEN user splits PDF
     * THEN result list contains exactly 1 uri
     * AND result file has the same page-count as original file
     * AND all pages from resulting file are equal to pages of original file
     */
    @Test
    fun testAllPagesSelected() {

        // GIVEN
        val selectedPageIndices = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
        val originalFile = sut.getPdfDocumentInReadingMode()

        // WHEN
        val resultList: List<Uri> = sut.splitPdfWithSelection(fileName, selectedPageIndices, storagePath)
        val resultFile: PdfDocument = PdfManipulator.create(appContext, resultList.first()).getPdfDocumentInReadingMode()

        // THEN
        assertThat(resultList.size).isEqualTo(1)
        assertThat(resultFile.numberOfPages).isEqualTo(originalFile.numberOfPages)

        for (i in 1..resultFile.numberOfPages) {
            assertThatPagesAreEqual(resultFile.getPage(i), originalFile.getPage(i))
        }
    }

    private fun assertThatPagesAreEqual(first: PdfPage, second: PdfPage) {
        assertThat(first.contentBytes).isEqualTo(second.contentBytes)
    }

}
package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.itextpdf.kernel.pdf.PdfDocument
import org.junit.Assert
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

    @Before
    fun setup() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext
        file = FileUtil.getInstance().loadFileFromAssets(appContext, fileName)
        uri = Uri.fromFile(file)
        sut = PdfManipulator.create(appContext, uri)
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
     * AND the first result-uri is PDF with three pages
     * AND the second result-uri is PDF with seven pages
     */
    @Test
    fun testSelectionOfPages() {

        // GIVEN
        val selectedPageIndices = listOf(0, 1, 5)

        // WHEN
        val result: List<Uri> = sut.splitPdfWithSelection(fileName, selectedPageIndices, appContext.cacheDir.absolutePath)
        val documentOfSelectedPages = PdfManipulator.create(appContext, result.first()).getPdfDocumentInReadingMode()
        val documentOfUnselectedPages = PdfManipulator.create(appContext, result[1]).getPdfDocumentInReadingMode()

        // THEN
        assertThat(result).hasSize(2)
        assertThat(documentOfSelectedPages.numberOfPages).isEqualTo(3)
        assertThat(documentOfUnselectedPages.numberOfPages).isEqualTo(7)

    }

}
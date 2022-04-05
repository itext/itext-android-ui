package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import com.itextpdf.android.library.R
import com.itextpdf.android.library.extensions.getAnnotations
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfPage
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.itextpdf.kernel.pdf.annot.PdfMarkupAnnotation
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation
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

    /**
     * GIVEN pdf does not contain any text annotation
     * WHEN user adds a text annotation
     * THEN text annotation is stored in pdf
     */
    @Test
    fun testAddTextAnnotation() {

        val pdfDocument: PdfDocument = sut.getPdfDocumentInReadingMode()
        val annotations: List<PdfAnnotation> = pdfDocument.getAnnotations()

        // GIVEN
        assertThat(annotations).isEmpty()

        // WHEN
        sut.addTextAnnotationToPdf(
            title = "Lorem Ipsum Title",
            text = "Lorem Ipsum Message",
            pageIndex = 0,
            x = 0f,
            y = 0f,
            bubbleSize = 1f,
            bubbleColor = appContext.getColor(R.color.black)
        )

        val updatedAnnotations = sut.getPdfDocumentInReadingMode().getAnnotations()
        val annotation: PdfTextAnnotation = updatedAnnotations.first() as PdfTextAnnotation

        // THEN
        assertThat(updatedAnnotations.size).isEqualTo(1)
        assertThat(annotation.title.value).isEqualTo("Lorem Ipsum Title")
        assertThat(annotation.contents.value).isEqualTo("Lorem Ipsum Message")


    }

    /**
     * GIVEN pdf contains two annotations
     * WHEN users removes one of those annotations
     * THEN pdf only contains the other, remaining annotation
     */
    @Test
    fun testRemoveAnnotation() {

        sut.addTextAnnotationToPdf(
            title = "Lorem Ipsum Title 1",
            text = "Lorem Ipsum Message 2",
            pageIndex = 0,
            x = 0f,
            y = 0f,
            bubbleSize = 1f,
            bubbleColor = appContext.getColor(R.color.black)
        )

        sut.addTextAnnotationToPdf(
            title = "Lorem Ipsum Title 2",
            text = "Lorem Ipsum Message 2",
            pageIndex = 0,
            x = 0f,
            y = 0f,
            bubbleSize = 1f,
            bubbleColor = appContext.getColor(R.color.black)
        )

        val annotations: List<PdfAnnotation> = sut.getPdfDocumentInReadingMode().getAnnotations()
        val annotationToRemove = annotations.first()

        // GIVEN
        assertThat(annotations).hasSize(2)

        // WHEN
        sut.removeAnnotationFromPdf(annotationToRemove)
        val remaining: List<PdfAnnotation> = sut.getPdfDocumentInReadingMode().getAnnotations()
        val remainingAnnotation: PdfAnnotation = remaining.first()

        // THEN
        assertThat(remaining).hasSize(1)
        assertThat(remainingAnnotation.title.value).isEqualTo("Lorem Ipsum Title 2")
        assertThat(remainingAnnotation.contents.value).isEqualTo("Lorem Ipsum Message 2")

    }

    /**
     * GIVEN pdf contains no annotations
     * WHEN users adds markup-annotation to pdf
     * THEN markup-annotation is saved to pdf
     * AND count of annotations in pdf is 1
     */
    @Test
    fun testAddMarkupAnnotationToPdf() {

        // GIVEN
        val currentAnnotations = sut.getPdfDocumentInReadingMode().getAnnotations()
        assertThat(currentAnnotations).hasSize(0)

        // WHEN
        sut.addMarkupAnnotationToPdf(1, Rectangle(10f, 10f), DeviceRgb.GREEN)

        // THEN
        val annotations = sut.getPdfDocumentInReadingMode().getAnnotations()
        val annotation = annotations.first() as PdfMarkupAnnotation

        assertThat(annotations).hasSize(1)
        assertThat(annotation).isInstanceOf(PdfMarkupAnnotation::class.java)
    }

    /**
     * GIVEN pdf contains exactly one text-annotation
     * WHEN user changes title and text of that annotation
     * THEN annotation is updated in pdf-file
     * AND count of annotations remains 1
     */
    @Test
    fun testEditAnnotationsFromPdf() {

        // GIVEN
        sut.addTextAnnotationToPdf("Title 1", "Message 1", 0, 0f, 0f, 1f, appContext.getColor(R.color.black))

        val existingAnnotations = sut.getPdfDocumentInReadingMode().getAnnotations()
        assertThat(existingAnnotations).hasSize(1)

        // WHEN
        sut.editAnnotationFromPdf(existingAnnotations.first(), "Title 2", "Message 2")

        // THEN
        val updatedAnnotations = sut.getPdfDocumentInReadingMode().getAnnotations()
        val updated = updatedAnnotations.first() as PdfTextAnnotation

        assertThat(updated.title.value).isEqualTo("Title 2")
        assertThat(updated.contents.value).isEqualTo("Message 2")
        assertThat(updatedAnnotations).hasSize(1)

    }

    private fun assertThatPagesAreEqual(first: PdfPage, second: PdfPage) {
        assertThat(first.contentBytes).isEqualTo(second.contentBytes)
    }

}
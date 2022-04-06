package com.itextpdf.android.library.util

import android.content.Context
import android.net.Uri
import androidx.annotation.ColorInt
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.geom.Rectangle
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject
import com.itextpdf.kernel.utils.PageRange
import java.io.File

/**
 * An a manipulator that provides several functions for manipulating PDF files, such as adding text-annotations and splitting pdf-documents.
 */
interface PdfManipulator {

    val workingCopy: File

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
    fun splitPdfWithSelection(fileName: String, selectedPageIndices: List<Int>, storageFolderPath: String): List<Uri>

    /**
     * Creates the name for the new document created during the split based on initial name and partNumber
     *
     * @param initialFileName   the name of the original document
     * @param partNumber    the part number of the document for which a name should be created. 1 is the first document, 2 the second, ...
     * @param selectedPagesNumbers  the list of selected page numbers
     * @param unselectedPageNumbers  the list of unselected page numbers
     * @return  the name for the split document
     */
    fun getSplitDocumentName(initialFileName: String, partNumber: Int, selectedPagesNumbers: List<Int>, unselectedPageNumbers: List<Int>): String

    /**
     * Returns the page ranges for selected and unselected pages that can be used for splitting
     *
     * @param selectedPagesNumbers  a list of page numbers (NOT indices) that were selected
     * @param unselectedPageNumbers a list of page numbers (NOT indices) that were not selected
     * @param numberOfPages the number of pages this pdf document has
     * @return  the list of page ranges (can be empty if selected pages and unselected pages were empty or the numbers were higher than the numberOfPages)
     */
    fun getPageRanges(selectedPagesNumbers: List<Int>, unselectedPageNumbers: List<Int>, numberOfPages: Int): List<PageRange>

    /**
     * Adds a text annotation to the PDF.
     *
     * @param title The title of the annotation.
     * @param text The text of the annotation.
     * @param pageIndex The zero-based page-index, specifying on which PDF page the annotation shall be added.
     * @param x The x-coordinates of the annotation.
     * @param y The y-coordinate of the annotation.
     * @param bubbleSize The size of the highlight-bubble.
     * @param bubbleColor The color of the highlight-bubble.
     */
    fun addTextAnnotationToPdf(title: String?, text: String, pageIndex: Int, x: Float, y: Float, bubbleSize: Float, @ColorInt bubbleColor: Int): File

    /**
     * Ads a markup annotation with [rect] and [color] on the given [pageNumber].
     */
    fun addMarkupAnnotationToPdf(pageNumber: Int, rect: Rectangle, color: Color): File

    /**
     * Remove the given [annotationToRemove] from the PDF file.
     *
     * @param annotationToRemove The annotation to be removed.
     * @return The updated PDF file
     */
    fun removeAnnotationFromPdf(annotationToRemove: PdfAnnotation): File

    /**
     * Updates [title] and [text] of the specified [annotation].
     *
     * @param annotation The annotation to be edited.
     * @param title The title to be set for the annotation.
     * @param text The text to be set for the annotation.
     * @return The updated PDF file.
     */
    fun editAnnotationFromPdf(annotation: PdfAnnotation, title: String?, text: String): File

    /**
     * Returns the [PdfDocument] in reading-mode.
     */
    fun getPdfDocumentInReadingMode(): PdfDocument

    /**
     * Returns the [PdfDocument] in stamping-mode.
     */
    fun getPdfDocumentInStampingMode(destFile: File): PdfDocument

    companion object Factory {

        /**
         * Returns a new instance of [PdfManipulator] for the given pdf-file located at [pdfUri].
         *
         * @param context The context to be used for accessing resources etc.
         * @param pdfUri The uri pointing at the location where the PDF file is located.
         */
        fun create(context: Context, pdfUri: Uri): PdfManipulator {
            return PdfManipulatorImpl(context, pdfUri)
        }
    }


}
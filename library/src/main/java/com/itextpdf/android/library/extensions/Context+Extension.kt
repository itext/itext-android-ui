package com.itextpdf.android.library.extensions

import android.content.Context
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.FileNotFoundException

/**
 * Returns a pdfDocument object with the given filename in writing mode. The document has no pages when initialized.
 *
 * @param fileName  the filename of the pdf that should be written
 * @param mode      the file creation mode that defines who can access the file. Default is MODE_PRIVATE
 *                  which only allows the calling application to access the file.
 * @return  the pdf document in writing mode
 */
fun Context.pdfDocumentWriter(fileName: String, mode: Int = Context.MODE_PRIVATE): PdfDocument? {
    return try {
        val output = openFileOutput(fileName, mode)
        PdfDocument(PdfWriter(output))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}

/**
 * Returns a pdfDocument object with the given filename in reading mode.
 *
 * @param fileName  the filename of the pdf that should be read
 * @return  the pdf document in reading mode
 */
fun Context.pdfDocumentReader(fileName: String): PdfDocument? {
    return try {
        val file = getFileStreamPath(fileName).absoluteFile
        PdfDocument(PdfReader(file))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}

/**
 * Returns a pdfDocument object with the given filename in stamping mode.
 *
 * @param fileName  the filename of the pdf that should be written
 * @param mode      the file creation mode that defines who can access the file. Default is MODE_PRIVATE
 *                  which only allows the calling application to access the file.
 * @return  the pdf document in stamping mode
 */
fun Context.pdfDocumentStamping(fileName: String, mode: Int = Context.MODE_PRIVATE): PdfDocument? {
    return try {
        val readFile = getFileStreamPath(fileName).absoluteFile
        val output = openFileOutput(fileName, mode)
        PdfDocument(PdfReader(readFile), PdfWriter(output))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}
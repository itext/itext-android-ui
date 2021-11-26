package com.itextpdf.android.library

import android.content.Context
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.PdfWriter
import java.io.FileNotFoundException


fun Context.pdfDocumentWriter(fileName: String, mode: Int = Context.MODE_PRIVATE): PdfDocument? {

    return try {
        val output = openFileOutput(fileName, mode)
        PdfDocument(PdfWriter(output))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun Context.pdfDocumentReader(fileName: String): PdfDocument? {

    return try {
        val file = getFileStreamPath(fileName).absoluteFile
        PdfDocument(PdfReader(file))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}

fun Context.pdfDocument(fileName: String, mode: Int = Context.MODE_PRIVATE): PdfDocument? {

    return try {
        val readFile = getFileStreamPath(fileName).absoluteFile
        val output = openFileOutput(fileName, mode)
        PdfDocument(PdfReader(readFile), PdfWriter(output))
    } catch (e: FileNotFoundException) {
        e.printStackTrace()
        null
    }
}
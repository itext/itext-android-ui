package com.itextpdf.android.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.app.databinding.ActivityMainBinding
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        private const val fileName = "test.pdf"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        writePdf()

        val file = getFileStreamPath(fileName).absoluteFile
        binding.thumbnail.set(file)
    }

    private fun writePdf() {
        try {

            val output = openFileOutput(fileName, MODE_PRIVATE)

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
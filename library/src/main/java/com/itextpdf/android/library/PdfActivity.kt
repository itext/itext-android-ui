package com.itextpdf.android.library

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.library.util.FileUtil


internal class PdfActivity : AppCompatActivity() {

    private val fileUtil = FileUtil.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        copyFileFromAssets()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf)
    }

    private fun copyFileFromAssets() {
        fileUtil.loadFileFromAssets(this, "sample_1.pdf")
    }
}
package com.itextpdf.android.library

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.library.fragments.SplitDocumentFragment
import com.itextpdf.android.library.util.FileUtil

/**
 * This class is only used during UI testing regarding XML inflation of [SplitDocumentFragment].
 */
internal class SplitPdfActivity : AppCompatActivity() {

    private val fileUtil = FileUtil.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        copyFileFromAssets()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_pdf)
    }

    private fun copyFileFromAssets() {
        fileUtil.loadFileFromAssets(this, "sample_1.pdf")
    }
}
package com.itextpdf.android.library

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.library.fragments.SplitDocumentFragment

/**
 * This class is only used during UI testing regarding XML inflation of [SplitDocumentFragment].
 */
internal class SplitPdfActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_split_pdf)
    }

}
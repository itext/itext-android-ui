package com.itextpdf.android.app.ui

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.android.app.databinding.ActivityPdfViewerBinding

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPdfViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPdfViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        // set fragment in code
//        val fragment = PdfFragment()
//        fragment.text = "TESTOOOO"
//        val fm = supportFragmentManager.beginTransaction()
//        fm.replace(R.id.pdf_fragment_container, fragment)
//        fm.commit()
    }

    companion object {
        fun launch(context: Context) {
            val intent = Intent(context, PdfViewerActivity::class.java)
            context.startActivity(intent)
        }
    }
}
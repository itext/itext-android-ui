package com.itextpdf.android.library.ui

import androidx.fragment.app.Fragment

import android.content.res.TypedArray

import android.os.Bundle

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentPdfBinding

/**
 * Fragment that can be used to display a pdf file. To pass the pdf file to the fragment set the uri
 * to the pdf via the public variable pdfUri before committing the fragment in code.
 */
class PdfFragment: Fragment() {

    private lateinit var binding: FragmentPdfBinding
    var text = ""
    var pdfUri: Uri? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentPdfBinding.inflate(inflater, container, false)

        if (savedInstanceState != null) {
            // Restore last state
            text = savedInstanceState.getString("my_text") ?: ""
        }

        pdfUri?.let {
            binding.testThumbnail.set(it)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.txtFragment.text = text
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("my_text", text)
    }

    /**
     * Parse attributes during inflation from a view hierarchy into the
     * arguments we handle.
     */
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        Log.v(TAG, "onInflate called")
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PdfFragment)
        val myString = a.getText(R.styleable.PdfFragment_my_string)
        if (myString != null) {
            text = myString.toString()
            Log.v(TAG, "My String Received : $myString")
        }
        a.recycle()
    }

    companion object {
        private const val TAG = "PdfFragment"
    }
}
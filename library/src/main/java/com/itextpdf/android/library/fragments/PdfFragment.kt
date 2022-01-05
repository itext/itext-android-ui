package com.itextpdf.android.library.fragments

import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentPdfBinding

/**
 * Fragment that can be used to display a pdf file. To pass the pdf file to the fragment set the uri
 * to the pdf via the public variable pdfUri before committing the fragment in code or by setting
 * the attribute app:file_uri in xml.
 */
open class PdfFragment : Fragment() {

    private lateinit var binding: FragmentPdfBinding
    var fileName: String? = null
    var pdfUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfBinding.inflate(inflater, container, false)

        if (savedInstanceState != null) {
            // Restore last state
            fileName = savedInstanceState.getString(FILE_NAME) ?: ""
            val storedUri = savedInstanceState.getString(PDF_URI)
            if (!storedUri.isNullOrEmpty()) {
                pdfUri = Uri.parse(storedUri)
            }
        }

        pdfUri?.let {
            binding.testThumbnail.set(it)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvFileName.text = fileName
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FILE_NAME, fileName)
        outState.putString(PDF_URI, pdfUri.toString())
    }

    /**
     * Parse attributes during inflation from a view hierarchy into the
     * arguments we handle.
     */
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        Log.v(TAG, "onInflate called")

        // get the attributes data set via xml
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PdfFragment)
        val attrFileName = a.getText(R.styleable.PdfFragment_file_name)
        if (attrFileName != null) {
            fileName = attrFileName.toString()
            Log.v(TAG, "Filename received : $attrFileName")
        }
        val attrUri = a.getText(R.styleable.PdfFragment_file_uri)
        if (attrUri != null) {
            pdfUri = Uri.parse(attrUri.toString())
            Log.v(TAG, "Pdf uri received : $attrUri")
        }
        a.recycle()
    }

    companion object {
        private const val TAG = "PdfFragment"
        private const val FILE_NAME = "FILE_NAME"
        private const val PDF_URI = "PDF_URI"
    }
}
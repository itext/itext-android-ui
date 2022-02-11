package com.itextpdf.android.library.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentSplitDocumentBinding

open class SplitDocumentFragment : Fragment() {

    private lateinit var binding: FragmentSplitDocumentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplitDocumentBinding.inflate(inflater, container, false)

        setupToolbar()

        return binding.root
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        if (::binding.isInitialized) {
            (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.tbSplitDocumentFragment)
            binding.tbSplitDocumentFragment.setNavigationIcon(R.drawable.ic_close)
            binding.tbSplitDocumentFragment.setNavigationOnClickListener { requireActivity().onBackPressed() }
        }
    }

    companion object {
        private const val PDF_URI = "PDF_URI"

        fun newInstance(
            pdfUri: Uri
        ): SplitDocumentFragment {
            val fragment = SplitDocumentFragment()

            val args = Bundle()
            args.putString(PDF_URI, pdfUri.toString())

            fragment.arguments = args
            return fragment
        }
    }
}
package com.itextpdf.android.library.fragments

import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentSplitDocumentBinding
import com.itextpdf.android.library.navigation.PdfNavigationAdapter
import com.itextpdf.android.library.navigation.PdfPageRecyclerItem
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore

open class SplitDocumentFragment : Fragment() {

    /**
     * The uri of the pdf that should be displayed
     */
    private var pdfUri: Uri? = null

    /**
     * A color string to set the primary color of the view (affects: scroll indicator, navigation thumbnails and loading indicator). Default: #FF9400
     */
    private var primaryColor: String? = PdfFragment.DEFAULT_PRIMARY_COLOR

    /**
     * A color string to set the secondary color of the view (affects: scroll indicator and navigation thumbnails). Default: #FFEFD8
     */
    private var secondaryColor: String? = PdfFragment.DEFAULT_SECONDARY_COLOR

    /**
     * A color string to set the background of the pdf view that will be visible between the pages if pageSpacing > 0. Default: #EAEAEA
     */
    private var backgroundColor: String? = PdfFragment.DEFAULT_BACKGROUND_COLOR

    private lateinit var binding: FragmentSplitDocumentBinding
    private lateinit var splitPdfAdapter: PdfNavigationAdapter

    private lateinit var pdfiumCore: PdfiumCore

    private var navigationPdfDocument: PdfDocument? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSplitDocumentBinding.inflate(inflater, container, false)
        pdfiumCore = PdfiumCore(requireContext())

        // set the parameter from the savedInstanceState, or if it's null, from the arguments
        setParamsFromBundle(savedInstanceState ?: arguments)

        setupToolbar()

        pdfUri?.let {
            val fileDescriptor: ParcelFileDescriptor? =
                requireContext().contentResolver.openFileDescriptor(it, "r")
            if (fileDescriptor != null) {
                try {
                    navigationPdfDocument = pdfiumCore.newDocument(fileDescriptor)
                    setupSplitSelectionList()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }

        setupSplitSelectionList()

        return binding.root
    }

    private fun setParamsFromBundle(bundle: Bundle?) {
        if (bundle != null) {
            val storedUri = bundle.getString(PDF_URI)
            if (!storedUri.isNullOrEmpty()) {
                pdfUri = Uri.parse(storedUri)
            }
            primaryColor = bundle.getString(PRIMARY_COLOR) ?: PdfFragment.DEFAULT_PRIMARY_COLOR
            secondaryColor =
                bundle.getString(SECONDARY_COLOR) ?: PdfFragment.DEFAULT_SECONDARY_COLOR
            backgroundColor =
                bundle.getString(BACKGROUND_COLOR) ?: PdfFragment.DEFAULT_BACKGROUND_COLOR
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (navigationPdfDocument != null) {
            pdfiumCore.closeDocument(navigationPdfDocument)
        }
    }

    /**
     * Parse attributes during inflation from a view hierarchy into the
     * arguments we handle.
     */
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)

        // get the attributes data set via xml
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.SplitDocumentFragment)
        a.getText(R.styleable.SplitDocumentFragment_file_uri)?.let {
            pdfUri = Uri.parse(it.toString())
        }
        a.getText(R.styleable.SplitDocumentFragment_primary_color)?.let {
            primaryColor = it.toString()
        }
        a.getText(R.styleable.SplitDocumentFragment_secondary_color)?.let {
            secondaryColor = it.toString()
        }
        a.getText(R.styleable.SplitDocumentFragment_background_color)?.let {
            backgroundColor = it.toString()
        }
        a.recycle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PDF_URI, pdfUri.toString())
        outState.putString(PRIMARY_COLOR, primaryColor)
        outState.putString(SECONDARY_COLOR, secondaryColor)
        outState.putString(BACKGROUND_COLOR, backgroundColor)
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        if (::binding.isInitialized) {
            (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.tbSplitDocumentFragment)
            binding.tbSplitDocumentFragment.setNavigationIcon(R.drawable.ic_close)
            binding.tbSplitDocumentFragment.setNavigationOnClickListener { requireActivity().onBackPressed() }
        }
    }

    private fun setupSplitSelectionList() {
        navigationPdfDocument?.let {
            val data = mutableListOf<PdfPageRecyclerItem>()
            for (i in 0 until pdfiumCore.getPageCount(it) * 10) {
                //TODO: REMOVE MODULO
                data.add(PdfPageRecyclerItem(pdfiumCore, it, i % pdfiumCore.getPageCount(it)) {
//                    navPageSelected = true
//                    scrollThumbnailNavigationViewToPage(i)
//                    scrollToPage(i)
//                    navPageSelected = false
                })
            }

            splitPdfAdapter = PdfNavigationAdapter(data, primaryColor, secondaryColor)
            binding.rvSplitDocument.adapter = splitPdfAdapter
            binding.rvSplitDocument.layoutManager =
                GridLayoutManager(requireContext(), 3, GridLayoutManager.VERTICAL, false)

            // make selection snappier as view holder can be reused
            val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
            binding.rvSplitDocument.itemAnimator = itemAnimator
        }
    }

    companion object {
        private const val PDF_URI = "PDF_URI"
        private const val PRIMARY_COLOR = "PRIMARY_COLOR"
        private const val SECONDARY_COLOR = "SECONDARY_COLOR"
        private const val BACKGROUND_COLOR = "BACKGROUND_COLOR"

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
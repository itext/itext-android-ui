package com.itextpdf.android.library.fragments

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentSplitDocumentBinding
import com.itextpdf.android.library.lists.PdfAdapter
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.split.PdfSplitRecyclerItem
import com.itextpdf.android.library.paging.Page
import com.itextpdf.android.library.paging.PaginationScrollListener
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.min

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

    private lateinit var binding: FragmentSplitDocumentBinding

    private lateinit var pdfiumCore: PdfiumCore

    private var navigationPdfDocument: PdfDocument? = null

    private var renderJob: Job? = null

    private var currentPage = 0
    private var loading = false

    private var page: Page<PdfRecyclerItem>? = null

    private var pdfPages = mutableListOf<PdfRecyclerItem>()
    private lateinit var splitPdfAdapter: PdfAdapter

    private var documentPageCount = 0
    private var totalPages = 0

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

        splitPdfAdapter = PdfAdapter(pdfPages, true, primaryColor, secondaryColor)

        pdfUri?.let {
            val fileDescriptor: ParcelFileDescriptor? =
                requireContext().contentResolver.openFileDescriptor(it, "r")
            if (fileDescriptor != null) {
                try {
                    navigationPdfDocument = pdfiumCore.newDocument(fileDescriptor)
                    navigationPdfDocument?.let { pdfDocument ->
                        documentPageCount = pdfiumCore.getPageCount(pdfDocument)
                        totalPages = ceil(documentPageCount.toDouble() / PAGE_SIZE).toInt()
                    }
                    setupSplitSelectionList()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }

        if (primaryColor != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.splitPdfLoadingIndicator.indeterminateDrawable.colorFilter =
                    BlendModeColorFilter(Color.parseColor(primaryColor), BlendMode.SRC_ATOP)
            } else {
                binding.splitPdfLoadingIndicator.indeterminateDrawable.setColorFilter(
                    Color.parseColor(primaryColor),
                    PorterDuff.Mode.SRC_ATOP
                )
            }
        }

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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (navigationPdfDocument != null) {
            renderJob?.cancel()
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
        a.recycle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PDF_URI, pdfUri.toString())
        outState.putString(PRIMARY_COLOR, primaryColor)
        outState.putString(SECONDARY_COLOR, secondaryColor)
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
        binding.rvSplitDocument.adapter = splitPdfAdapter
        val layoutManager =
            GridLayoutManager(requireContext(), ROW_NUMBER, GridLayoutManager.VERTICAL, false)
        binding.rvSplitDocument.layoutManager = layoutManager

        // make selection snappier as view holder can be reused
        val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
            override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                return true
            }
        }
        binding.rvSplitDocument.itemAnimator = itemAnimator

        binding.rvSplitDocument.addOnScrollListener(object :
            PaginationScrollListener(layoutManager, LOAD_MORE_OFFSET) {
            override fun loadMoreItems() {
                requestPage(currentPage + 1)
            }

            override val isLastPage: Boolean
                get() = page?.number == page?.totalPages

            override val isLoading: Boolean
                get() = loading
        })

        binding.splitPdfLoadingIndicator.visibility = View.VISIBLE
        requestPage(currentPage)
    }

    private fun requestPage(pageIndex: Int) {

        navigationPdfDocument?.let {
            loading = true
            currentPage = pageIndex
            //TODO
            //            val width = min(200, fragmentWidth / ROW_NUMBER * 2)
            val width = 150
            val height = (width * 1.3).toInt()

            renderJob = viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val pdfItems = mutableListOf<PdfRecyclerItem>()
                    val fromIndex = pdfPages.size
                    val untilIndex = min((currentPage + 1) * PAGE_SIZE, documentPageCount)
                    for (i in fromIndex until untilIndex) {
                        pdfiumCore.openPage(it, i)

                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                        pdfiumCore.renderPageBitmap(it, bitmap, i, 0, 0, width, height, true)

                        pdfItems.add(PdfSplitRecyclerItem(bitmap, i) {
                            splitPdfAdapter.updateSelectedItem(i)
                        })
                    }
                    page = Page(pdfItems, pdfItems.size, pageIndex, totalPages)
                    page?.let {
                        pdfPages.addAll(pdfItems)
                        withContext(Dispatchers.Main) {
                            splitPdfAdapter.notifyItemRangeInserted(
                                pdfPages.size,
                                pdfPages.size + it.size
                            )
                        }
                        loading = false
                        binding.splitPdfLoadingIndicator.visibility = View.GONE
                    }
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                }
            }
        } ?: run {
            binding.splitPdfLoadingIndicator.visibility = View.GONE
        }
    }

    companion object {
        private const val PDF_URI = "PDF_URI"
        private const val PRIMARY_COLOR = "PRIMARY_COLOR"
        private const val SECONDARY_COLOR = "SECONDARY_COLOR"

        private const val ROW_NUMBER = 3
        private const val PAGE_SIZE = 30
        private const val LOAD_MORE_OFFSET = PAGE_SIZE / 2

        fun newInstance(
            pdfUri: Uri,
            primaryColor: String? = null,
            secondaryColor: String? = null
        ): SplitDocumentFragment {
            val fragment = SplitDocumentFragment()

            val args = Bundle()
            args.putString(PDF_URI, pdfUri.toString())
            args.putString(PRIMARY_COLOR, primaryColor)
            args.putString(SECONDARY_COLOR, secondaryColor)

            fragment.arguments = args
            return fragment
        }
    }
}
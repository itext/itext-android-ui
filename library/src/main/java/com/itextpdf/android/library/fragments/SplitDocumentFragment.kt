package com.itextpdf.android.library.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.use
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResult
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentSplitDocumentBinding
import com.itextpdf.android.library.extensions.getBooleanIfAvailable
import com.itextpdf.android.library.extensions.getTextIfAvailable
import com.itextpdf.android.library.lists.PdfAdapter
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.split.PdfSplitRecyclerItem
import com.itextpdf.android.library.paging.Page
import com.itextpdf.android.library.paging.PaginationScrollListener
import com.itextpdf.android.library.util.PdfManipulator
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.math.min


/**
 * Fragment that can be used to split a pdf file into two pieces. One containing of the selected pages
 * another one containing of the not selected pages.
 * To pass the pdf file and other settings to the fragment use the static newInstance() function or set
 * them as attributes in (e.g.: app:file_uri).
 * After the successful split, the SplitDocumentFragment sets a list of pdfUris resulting from the split
 * as a fragmentResult. Therefore set a listener for the key SplitDocumentFragment.SPLIT_DOCUMENT_RESULT
 * to get the uris via: bundle.getParcelableArrayList<Uri>(SplitDocumentFragment.SPLIT_PDF_URI_LIST)
 */
open class SplitDocumentFragment : Fragment() {

    private lateinit var config: PdfConfig
    private lateinit var pdfManipulator: PdfManipulator

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // set the parameter from the savedInstanceState, or if it's null, from the arguments
        setParamsFromBundle(savedInstanceState ?: arguments)
        pdfManipulator = PdfManipulator.create(requireContext(), config.pdfUri)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSplitDocumentBinding.inflate(inflater, container, false)
        pdfiumCore = PdfiumCore(requireContext())

        setupToolbar()

        splitPdfAdapter = PdfAdapter(pdfPages, true, config.primaryColor, config.secondaryColor)

        val fileDescriptor: ParcelFileDescriptor? =
            requireContext().contentResolver.openFileDescriptor(config.pdfUri, "r")
        if (fileDescriptor != null) {
            try {
                navigationPdfDocument = pdfiumCore.newDocument(fileDescriptor)
                navigationPdfDocument?.let { pdfDocument ->
                    documentPageCount = pdfiumCore.getPageCount(pdfDocument)
                    totalPages = ceil(documentPageCount.toDouble() / PAGE_SIZE).toInt()
                }
                setupSplitSelectionList()
            } catch (error: Exception) {
                Log.e(LOG_TAG, null, error)
            }
        }

        adjustColors()

        binding.fabSplit.visibility = View.INVISIBLE
        binding.fabSplit.setOnClickListener {
            splitPdfDocument()
        }

        return binding.root
    }

    private fun adjustColors() {

        val primaryColor = config.getPrimaryColorInt()
        val colorStateList = ColorStateList.valueOf(primaryColor)
        val colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(primaryColor, BlendModeCompat.SRC_ATOP)

        binding.splitPdfLoadingIndicator.indeterminateDrawable.colorFilter = colorFilter
        binding.fabSplit.backgroundTintList = colorStateList
    }

    private fun setParamsFromBundle(bundle: Bundle?) {
        if (bundle != null && bundle.containsKey(EXTRA_PDF_CONFIG)) {
            config = bundle.getParcelable(EXTRA_PDF_CONFIG)!!
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

        val builder = PdfConfig.Builder()

        // get the attributes data set via xml
        context.obtainStyledAttributes(attrs, R.styleable.SplitDocumentFragment).use { a ->
            a.getTextIfAvailable(R.styleable.SplitDocumentFragment_file_uri) { builder.pdfUri = Uri.parse(it.toString()) }
            a.getTextIfAvailable(R.styleable.SplitDocumentFragment_file_name) { builder.fileName = it.toString() }
            a.getTextIfAvailable(R.styleable.SplitDocumentFragment_primary_color) { builder.primaryColor = it.toString() }
            a.getTextIfAvailable(R.styleable.SplitDocumentFragment_secondary_color) { builder.secondaryColor = it.toString() }
            a.getBooleanIfAvailable(R.styleable.SplitDocumentFragment_enable_help_dialog) { builder.enableHelpDialog = it }
            a.getTextIfAvailable(R.styleable.SplitDocumentFragment_help_dialog_title) { builder.helpDialogTitle = it.toString() }
            a.getTextIfAvailable(R.styleable.SplitDocumentFragment_help_dialog_text) { builder.helpDialogText = it.toString() }
        }

        config = builder.build()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(EXTRA_PDF_CONFIG, config)
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)

        val toolbar = binding.tbSplitDocumentFragment

        (requireActivity() as? AppCompatActivity)?.setSupportActionBar(toolbar)

        toolbar.setNavigationIcon(R.drawable.ic_close)
        toolbar.setNavigationContentDescription(R.string.close)
        toolbar.setNavigationOnClickListener {
            setFragmentResult(SPLIT_DOCUMENT_REQUEST_KEY, bundleOf(SPLIT_DOCUMENT_RESULT to PdfResult.CancelledByUser))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_split_document, menu)
        menu.getItem(0).isVisible = config.enableHelpDialog
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_split_help -> {
            showHelpDialog()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun showHelpDialog() {
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle(config.helpDialogTitle ?: getString(R.string.help_dialog_title))
            .setMessage(config.helpDialogText ?: getString(R.string.help_dialog_text))
            .setPositiveButton(android.R.string.ok, null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setTextColor(Color.parseColor(config.primaryColor))
        }

        dialog.show()
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

            val width = min(MAX_THUMBNAIL_WIDTH, THUMBNAIL_WIDTH_BASE / (ROW_NUMBER * 2))
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
                            documentPageSelected(i)
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
                } catch (error: Throwable) {
                    Log.e(LOG_TAG, null, error)
                }
            }
        } ?: run {
            binding.splitPdfLoadingIndicator.visibility = View.GONE
        }
    }

    private fun documentPageSelected(index: Int) {
        splitPdfAdapter.updateSelectedItem(index)
        val selectedPages = splitPdfAdapter.getSelectedPositions()
        if (selectedPages.size == 1) {
            binding.fabSplit.show()
        } else if (selectedPages.isEmpty()) {
            binding.fabSplit.hide()
        }
    }

    /**
     * Splits the document based on the currently selected pages and sets the resulting uris of the
     * split documents as fragment result (requestKey: SPLIT_DOCUMENT_RESULT, bundleKey: SPLIT_PDF_URI_LIST)
     */
    open fun splitPdfDocument() {

        val fileName = config.fileName

        val storageFolderPath = (requireContext().externalCacheDir ?: requireContext().cacheDir).absolutePath
        val name = if (!fileName.isNullOrEmpty()) fileName else UNNAMED_FILE
        val pdfUriList = pdfManipulator.splitPdfWithSelection(
            name,
            splitPdfAdapter.getSelectedPositions(),
            storageFolderPath
        )
        if (pdfUriList.isNotEmpty()) {
            Log.i(LOG_TAG, getString(R.string.split_document_success))
        } else {
            Log.e(LOG_TAG, getString(R.string.split_document_error))
        }

        val selected = pdfUriList.first()
        val unselected = pdfUriList.getOrNull(1)

        val result = PdfResult.PdfSplit(
            fileContainingSelectedPages = selected,
            fileContainingUnselectedPages = unselected
        )

        // set the uris as fragmentResult for any class that is listening
        setFragmentResult(SPLIT_DOCUMENT_REQUEST_KEY, bundleOf(SPLIT_DOCUMENT_RESULT to result))

    }

    companion object {
        private const val LOG_TAG = "SplitDocumentFragment"

        internal const val EXTRA_PDF_CONFIG = "PDF_URI"

        private const val THUMBNAIL_WIDTH_BASE = 1080
        private const val MAX_THUMBNAIL_WIDTH = 150
        private const val ROW_NUMBER = 3
        private const val PAGE_SIZE = 30
        private const val LOAD_MORE_OFFSET = PAGE_SIZE / 2
        private const val UNNAMED_FILE = "unnamed.pdf"

        const val SPLIT_DOCUMENT_REQUEST_KEY = "split_pdf_request_key"
        const val SPLIT_DOCUMENT_RESULT = "SPLIT_DOCUMENT_RESULT"

        /**
         * Static function to create a new instance of the SplitDocumentFragment with the given settings
         *
         * @param config The configuration to be used.
         * @return  in instance of SplitDocumentFragment with the given settings
         */
        fun newInstance(config: PdfConfig): SplitDocumentFragment {

            val fragment = SplitDocumentFragment()
            fragment.arguments = bundleOf(EXTRA_PDF_CONFIG to config)

            return fragment
        }
    }
}
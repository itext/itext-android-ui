package com.itextpdf.android.library.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResult
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

    /**
     * The uri of the pdf that should be displayed
     */
    private var pdfUri: Uri? = null

    /**
     * The name of the file that should be displayed
     */
    private var fileName: String? = null

    /**
     * A color string to set the primary color of the view (affects: scroll indicator, navigation thumbnails and loading indicator). Default: #FF9400
     */
    private var primaryColor: String? = DEFAULT_PRIMARY_COLOR

    /**
     * A color string to set the secondary color of the view (affects: scroll indicator and navigation thumbnails). Default: #FFEFD8
     */
    private var secondaryColor: String? = DEFAULT_SECONDARY_COLOR

    /**
     * A boolean flag to enable/disable the help dialog in the split view. Default: true
     */
    private var enableHelpDialog = DEFAULT_ENABLE_HELP_DIALOG

    /**
     * The title of the help dialog in the split view. If this is null but help dialog is displayed, a default title is used.
     */
    private var helpDialogTitle: String? = null

    /**
     * The text of the help dialog in the split view. If this is null but help dialog is displayed, a default text is used.
     */
    private var helpDialogText: String? = null

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

        adjustColors()

        binding.fabSplit.visibility = View.INVISIBLE
        binding.fabSplit.setOnClickListener {
            splitPdfDocument()
        }

        return binding.root
    }

    private fun adjustColors() {
        if (primaryColor != null) {
            val primary = Color.parseColor(primaryColor)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                binding.splitPdfLoadingIndicator.indeterminateDrawable.colorFilter =
                    BlendModeColorFilter(primary, BlendMode.SRC_ATOP)
            } else {
                binding.splitPdfLoadingIndicator.indeterminateDrawable.setColorFilter(
                    primary,
                    PorterDuff.Mode.SRC_ATOP
                )
            }
            binding.fabSplit.backgroundTintList = ColorStateList.valueOf(primary)
        }
    }

    private fun setParamsFromBundle(bundle: Bundle?) {
        if (bundle != null) {
            val storedUri = bundle.getString(PDF_URI)
            if (!storedUri.isNullOrEmpty()) {
                pdfUri = Uri.parse(storedUri)
            }
            fileName = bundle.getString(FILE_NAME) ?: ""
            primaryColor = bundle.getString(PRIMARY_COLOR) ?: DEFAULT_PRIMARY_COLOR
            secondaryColor =
                bundle.getString(SECONDARY_COLOR) ?: DEFAULT_SECONDARY_COLOR
            enableHelpDialog = bundle.getBoolean(ENABLE_HELP_DIALOG, DEFAULT_ENABLE_HELP_DIALOG)
            helpDialogTitle = bundle.getString(HELP_DIALOG_TITLE)
            helpDialogText = bundle.getString(HELP_DIALOG_TEXT)
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
        a.getText(R.styleable.SplitDocumentFragment_file_name)?.let {
            fileName = it.toString()
        }
        a.getText(R.styleable.SplitDocumentFragment_primary_color)?.let {
            primaryColor = it.toString()
        }
        a.getText(R.styleable.SplitDocumentFragment_secondary_color)?.let {
            secondaryColor = it.toString()
        }
        enableHelpDialog = a.getBoolean(
            R.styleable.SplitDocumentFragment_enable_help_dialog,
            DEFAULT_ENABLE_HELP_DIALOG
        )
        a.getText(R.styleable.SplitDocumentFragment_help_dialog_title)?.let {
            helpDialogTitle = it.toString()
        }
        a.getText(R.styleable.SplitDocumentFragment_help_dialog_text)?.let {
            helpDialogText = it.toString()
        }
        a.recycle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(PDF_URI, pdfUri.toString())
        outState.putString(FILE_NAME, fileName)
        outState.putString(PRIMARY_COLOR, primaryColor)
        outState.putString(SECONDARY_COLOR, secondaryColor)
        outState.putBoolean(ENABLE_HELP_DIALOG, enableHelpDialog)
        outState.putString(HELP_DIALOG_TITLE, helpDialogTitle)
        outState.putString(HELP_DIALOG_TEXT, helpDialogText)
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        if (::binding.isInitialized) {
            (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.tbSplitDocumentFragment)
            binding.tbSplitDocumentFragment.setNavigationIcon(R.drawable.ic_close)
            binding.tbSplitDocumentFragment.setNavigationOnClickListener {
                val fragmentManager = requireActivity().supportFragmentManager
                val pdfFragment = fragmentManager.findFragmentByTag(PdfFragment.TAG)
                // if pdfFragment can be found, show it again, else close activity
                if (pdfFragment != null) {
                    val fragmentTransaction: FragmentTransaction =
                        fragmentManager.beginTransaction()
                    fragmentTransaction.remove(this)
                    fragmentTransaction.show(pdfFragment)
                    fragmentTransaction.commit()
                } else {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_split_document, menu)
        menu.getItem(0).isVisible = enableHelpDialog
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
            .setTitle(helpDialogTitle ?: getString(R.string.help_dialog_title))
            .setMessage(helpDialogText ?: getString(R.string.help_dialog_text))
            .setPositiveButton(android.R.string.ok, null)
            .create()

        // change color to primary if it was set
        if (primaryColor != null) {
            dialog.setOnShowListener {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.parseColor(primaryColor))
            }
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
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
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
        pdfUri?.let { uri ->
            val storageFolderPath =
                (requireContext().externalCacheDir ?: requireContext().cacheDir).absolutePath
            val name = if (!fileName.isNullOrEmpty()) fileName!! else UNNAMED_FILE
            val pdfUriList = PdfManipulator.splitPdfWithSelection(
                requireContext(),
                uri,
                name,
                splitPdfAdapter.getSelectedPositions(),
                storageFolderPath
            )
            if (pdfUriList.isNotEmpty()) {
                val file = pdfUriList.first().toFile()
                Log.i(TAG, getString(R.string.split_document_success, "${file.parent}/"))
            } else {
                Log.e(TAG, getString(R.string.split_document_error))
            }
            // set the uris as fragmentResult for any class that is listening
            setFragmentResult(SPLIT_DOCUMENT_RESULT, bundleOf(SPLIT_PDF_URI_LIST to pdfUriList))
        }
    }

    companion object {
        const val TAG = "SplitDocumentFragment"

        private const val PDF_URI = "PDF_URI"
        private const val FILE_NAME = "FILE_NAME"
        private const val PRIMARY_COLOR = "PRIMARY_COLOR"
        private const val SECONDARY_COLOR = "SECONDARY_COLOR"
        private const val ENABLE_HELP_DIALOG = "ENABLE_HELP_DIALOG"
        private const val HELP_DIALOG_TITLE = "HELP_DIALOG_TITLE"
        private const val HELP_DIALOG_TEXT = "HELP_DIALOG_TEXT"

        private const val THUMBNAIL_WIDTH_BASE = 1080
        private const val MAX_THUMBNAIL_WIDTH = 150
        private const val ROW_NUMBER = 3
        private const val PAGE_SIZE = 30
        private const val LOAD_MORE_OFFSET = PAGE_SIZE / 2
        private const val UNNAMED_FILE = "unnamed.pdf"
        private const val DEFAULT_PRIMARY_COLOR = "#FF9400"
        private const val DEFAULT_SECONDARY_COLOR = "#FFEFD8"

        const val SPLIT_DOCUMENT_RESULT = "SPLIT_DOCUMENT_RESULT"
        const val SPLIT_PDF_URI_LIST = "SPLIT_PDF_URI_LIST"

        const val DEFAULT_ENABLE_HELP_DIALOG = true

        /**
         * Static function to create a new instance of the SplitDocumentFragment with the given settings
         *
         * @param pdfUri    The uri of the pdf that should be split. This is the only required param
         * @param fileName  The name of the file that should be split
         * @param primaryColor  A color string to set the primary color of the view (affects: thumbnail selection and loading indicator). Default: #FF9400
         * @param secondaryColor    A color string to set the secondary color of the view (affects: thumbnail selection and loading indicator). Default: #FFEFD8
         * @param enableHelpDialog  A boolean flag to enable/disable the help dialog
         * @param helpDialogTitle  The title of the help dialog. If this is null but help dialog is displayed, a default title is used.
         * @param helpDialogText  The text of the help dialog. If this is null but help dialog is displayed, a default text is used.
         * @return  in instance of SplitDocumentFragment with the given settings
         */
        fun newInstance(
            pdfUri: Uri,
            fileName: String? = null,
            primaryColor: String? = null,
            secondaryColor: String? = null,
            enableHelpDialog: Boolean? = null,
            helpDialogTitle: String? = null,
            helpDialogText: String? = null
        ): SplitDocumentFragment {
            val fragment = SplitDocumentFragment()

            val args = Bundle()
            args.putString(PDF_URI, pdfUri.toString())
            args.putString(FILE_NAME, fileName)
            args.putString(PRIMARY_COLOR, primaryColor)
            args.putString(SECONDARY_COLOR, secondaryColor)
            if (enableHelpDialog != null)
                args.putBoolean(ENABLE_HELP_DIALOG, enableHelpDialog)
            args.putString(HELP_DIALOG_TITLE, helpDialogTitle)
            args.putString(HELP_DIALOG_TEXT, helpDialogText)

            fragment.arguments = args
            return fragment
        }
    }
}
package com.itextpdf.android.library.fragments

import android.content.Context
import android.content.res.TypedArray
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentPdfBinding
import com.itextpdf.android.library.navigation.PdfNavigationAdapter
import com.itextpdf.android.library.navigation.PdfPageItem
import com.itextpdf.android.library.navigation.PdfPageRecyclerItem
import com.itextpdf.android.library.views.CustomScrollHandle
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore


/**
 * Fragment that can be used to display a pdf file. To pass the pdf file to the fragment set the uri
 * to the pdf via the public variable pdfUri before committing the fragment in code or by setting
 * the attribute app:file_uri in xml.
 */
open class PdfFragment : Fragment() {

    private lateinit var binding: FragmentPdfBinding

    private lateinit var pdfNavigationAdapter: PdfNavigationAdapter

    private val actionsBottomSheet by lazy { binding.includedBottomSheetActions.bottomSheetActions }
    private lateinit var actionsBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private var navViewSetupComplete = false
    private var navPageSelected = false
    private var navViewOpen = false
    private var currentPage = 0

    private lateinit var pdfiumCore: PdfiumCore
    private var navigationPdfDocument: PdfDocument? = null

    var fileName: String? = null
    var pdfUri: Uri? = null
    var displayFileName = DEFAULT_DISPLAY_FILE_NAME
    var pageSpacing = DEFAULT_PAGE_SPACING
    var enableAnnotationRendering = DEFAULT_ENABLE_ANNOTATION_RENDERING
    var enableDoubleTapZoom = DEFAULT_ENABLE_DOUBLE_TAP_ZOOM
    var showScrollIndicator = DEFAULT_SHOW_SCROLL_INDICATOR
    var showScrollIndicatorPageNumber = DEFAULT_SHOW_SCROLL_INDICATOR_PAGE_NUMBER
    var primaryColor: String? = DEFAULT_PRIMARY_COLOR
    var secondaryColor: String? = DEFAULT_SECONDARY_COLOR
    var backgroundColor: String? = DEFAULT_BACKGROUND_COLOR

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfBinding.inflate(inflater, container, false)

        pdfiumCore = PdfiumCore(requireContext())

        setupToolbar()

        if (savedInstanceState != null) {
            // Restore last state
            currentPage = savedInstanceState.getInt(CURRENT_PAGE)
            fileName = savedInstanceState.getString(FILE_NAME) ?: ""
            val storedUri = savedInstanceState.getString(PDF_URI)
            if (!storedUri.isNullOrEmpty()) {
                pdfUri = Uri.parse(storedUri)
            }
            displayFileName = savedInstanceState.getBoolean(DISPLAY_FILE_NAME)
            pageSpacing = savedInstanceState.getInt(PAGE_SPACING)
            enableAnnotationRendering = savedInstanceState.getBoolean(ENABLE_ANNOTATION_RENDERING)
            enableDoubleTapZoom = savedInstanceState.getBoolean(ENABLE_DOUBLE_TAP_ZOOM)
            showScrollIndicator = savedInstanceState.getBoolean(SHOW_SCROLL_INDICATOR)
            showScrollIndicatorPageNumber =
                savedInstanceState.getBoolean(SHOW_SCROLL_INDICATOR_PAGE_NUMBER)
            primaryColor =
                savedInstanceState.getString(PRIMARY_COLOR)
            secondaryColor =
                savedInstanceState.getString(SECONDARY_COLOR)
            backgroundColor = savedInstanceState.getString(BACKGROUND_COLOR)
        }

        setupPdfView()

        pdfUri?.let {
            val fileDescriptor: ParcelFileDescriptor? =
                requireContext().contentResolver.openFileDescriptor(it, "r")
            if (fileDescriptor != null) {
                try {
                    navigationPdfDocument = pdfiumCore.newDocument(fileDescriptor)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }

        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        if (navigationPdfDocument != null) {
            pdfiumCore.closeDocument(navigationPdfDocument)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionsBottomSheetBehavior = BottomSheetBehavior.from(actionsBottomSheet)
        setBottomSheetVisibility(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_PAGE, currentPage)
        outState.putString(FILE_NAME, fileName)
        outState.putString(PDF_URI, pdfUri.toString())
        outState.putBoolean(DISPLAY_FILE_NAME, displayFileName)
        outState.putInt(PAGE_SPACING, pageSpacing)
        outState.putBoolean(ENABLE_ANNOTATION_RENDERING, enableAnnotationRendering)
        outState.putBoolean(ENABLE_DOUBLE_TAP_ZOOM, enableDoubleTapZoom)
        outState.putBoolean(SHOW_SCROLL_INDICATOR, showScrollIndicator)
        outState.putBoolean(SHOW_SCROLL_INDICATOR_PAGE_NUMBER, showScrollIndicatorPageNumber)
        outState.putString(PRIMARY_COLOR, primaryColor)
        outState.putString(SECONDARY_COLOR, secondaryColor)
        outState.putString(BACKGROUND_COLOR, backgroundColor)
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        if (::binding.isInitialized) {
            (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.tbPdfFragment)
            binding.tbPdfFragment.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
            binding.tbPdfFragment.setNavigationOnClickListener { requireActivity().onBackPressed() }
            binding.tbPdfFragment.title = if (displayFileName) fileName else null
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pdf_fragment, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_navigate_pdf -> {
            if (navViewSetupComplete)
                toggleBottomSheetVisibility()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun toggleBottomSheetVisibility() {
        // if bottom sheet is collapsed, set it to visible, if not set it to invisible
        setBottomSheetVisibility(actionsBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
    }

    private fun setBottomSheetVisibility(isVisible: Boolean) {
        if (isVisible) {
            scrollNavViewToPage(getCurrentlyVisibleItemPosition())
        }
        val updatedState =
            if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        actionsBottomSheetBehavior.state = updatedState
        navViewOpen = isVisible
    }

    private fun setupPdfNavigation() {
        navigationPdfDocument?.let {
            val data = mutableListOf<PdfPageRecyclerItem>()
            for (i in 0 until binding.pdfView.pageCount) {
                data.add(PdfPageItem(pdfiumCore, it, i) {
                    navPageSelected = true
                    scrollNavViewToPage(i)
                    scrollToPage(i)
                    navPageSelected = false
                })
            }

            pdfNavigationAdapter = PdfNavigationAdapter(data, primaryColor, secondaryColor)
            binding.includedBottomSheetActions.rvPdfPages.adapter = pdfNavigationAdapter
            binding.includedBottomSheetActions.rvPdfPages.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // make selection snappier as view holder can be reused
            val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
            binding.includedBottomSheetActions.rvPdfPages.itemAnimator = itemAnimator

            navViewSetupComplete = true
        }
    }

    private fun scrollToPage(position: Int) {
        binding.pdfView.jumpTo(position)
    }

    private fun scrollNavViewToPage(position: Int) {
        pdfNavigationAdapter.updateSelectedItem(position)
        (binding.includedBottomSheetActions.rvPdfPages.layoutManager as LinearLayoutManager).scrollToPosition(
            position
        )
    }

    private fun getCurrentlyVisibleItemPosition(): Int {
        return binding.pdfView.currentPage
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

    private fun setupPdfView() {
        pdfUri?.let { pdfUri ->
            val scrollHandle =
                if (showScrollIndicator) {
                    CustomScrollHandle(
                        requireContext(),
                        primaryColor,
                        secondaryColor,
                        showScrollIndicatorPageNumber
                    )
                } else {
                    null
                }

            binding.pdfLoadingIndicator.visibility = VISIBLE
            binding.pdfView.fromUri(pdfUri)
                .defaultPage(currentPage)
                .scrollHandle(scrollHandle)
                .onPageError { page, t ->
                    binding.pdfLoadingIndicator.visibility = GONE
                }
                .onPageScroll { _, _ ->
                    // if user scrolls, close the navView
                    if (!navPageSelected && navViewOpen) {
                        setBottomSheetVisibility(false)
                    }
                }
                .onPageChange { page, _ ->
                    currentPage = page
                }
                .onLoad {
                    setupPdfNavigation()
                    binding.pdfLoadingIndicator.visibility = GONE
                }
                .enableAnnotationRendering(enableAnnotationRendering)
                .spacing(pageSpacing)
                .enableDoubletap(enableDoubleTapZoom)
                .load()

            if (backgroundColor != null) {
                binding.pdfView.setBackgroundColor(Color.parseColor(backgroundColor))
            }
            if (primaryColor != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    binding.pdfLoadingIndicator.indeterminateDrawable.colorFilter =
                        BlendModeColorFilter(Color.parseColor(primaryColor), BlendMode.SRC_ATOP)
                } else {
                    binding.pdfLoadingIndicator.indeterminateDrawable.setColorFilter(
                        Color.parseColor(primaryColor),
                        PorterDuff.Mode.SRC_ATOP
                    )
                }
            }
        }
    }

    companion object {
        private const val TAG = "PdfFragment"
        private const val CURRENT_PAGE = "CURRENT_PAGE"
        private const val FILE_NAME = "FILE_NAME"
        private const val PDF_URI = "PDF_URI"
        private const val DISPLAY_FILE_NAME = "DISPLAY_FILE_NAME"
        private const val PAGE_SPACING = "PAGE_SPACING"
        private const val ENABLE_ANNOTATION_RENDERING = "ENABLE_ANNOTATION_RENDERING"
        private const val ENABLE_DOUBLE_TAP_ZOOM = "ENABLE_DOUBLE_TAP_ZOOM"
        private const val SHOW_SCROLL_INDICATOR = "SHOW_SCROLL_INDICATOR"
        private const val SHOW_SCROLL_INDICATOR_PAGE_NUMBER = "SHOW_SCROLL_INDICATOR_PAGE_NUMBER"
        private const val PRIMARY_COLOR = "PRIMARY_COLOR"
        private const val SECONDARY_COLOR = "SECONDARY_COLOR"
        private const val BACKGROUND_COLOR = "BACKGROUND_COLOR"

        const val DEFAULT_DISPLAY_FILE_NAME = false
        const val DEFAULT_PAGE_SPACING = 10
        const val DEFAULT_ENABLE_ANNOTATION_RENDERING = true
        const val DEFAULT_ENABLE_DOUBLE_TAP_ZOOM = true
        const val DEFAULT_SHOW_SCROLL_INDICATOR = true
        const val DEFAULT_SHOW_SCROLL_INDICATOR_PAGE_NUMBER = true
        const val DEFAULT_PRIMARY_COLOR = "#FF9400"
        const val DEFAULT_SECONDARY_COLOR = "#FFEFD8"
        const val DEFAULT_BACKGROUND_COLOR = "#EAEAEA"
    }
}
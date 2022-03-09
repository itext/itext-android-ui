package com.itextpdf.android.library.fragments

import android.app.AlertDialog
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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.net.toFile
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentPdfBinding
import com.itextpdf.android.library.extensions.pdfDocumentInReadingMode
import com.itextpdf.android.library.lists.PdfAdapter
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.annotations.AnnotationRecyclerItem
import com.itextpdf.android.library.lists.annotations.AnnotationsAdapter
import com.itextpdf.android.library.lists.navigation.PdfNavigationRecyclerItem
import com.itextpdf.android.library.views.PdfViewScrollHandle
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore


/**
 * Fragment that can be used to display a pdf file. To pass the pdf file and other settings to the
 * fragment use the static newInstance() function or set them as attributes in (e.g.: app:file_uri).
 */
open class PdfFragment : Fragment() {

    /**
     * The uri of the pdf that should be displayed
     */
    private var pdfUri: Uri? = null

    /**
     * The name of the file that should be displayed
     */
    private var fileName: String? = null

    /**
     * A boolean flag that defines if the given file name should be displayed in the toolbar. Default: false
     */
    private var displayFileName = DEFAULT_DISPLAY_FILE_NAME

    /**
     * The spacing in px between the pdf pages. Default: 20
     */
    private var pageSpacing = DEFAULT_PAGE_SPACING

    /**
     * A boolean flag to enable/disable pdf thumbnail navigation view. Default: true
     */
    private var enableThumbnailNavigationView = DEFAULT_ENABLE_THUMBNAIL_NAVIGATION_VIEW

    /**
     * A boolean flag to enable/disable pdf split view. Default: true
     */
    private var enableSplitView = DEFAULT_ENABLE_SPLIT_VIEW

    /**
     * A boolean flag to enable/disable annotation rendering. Default: true
     */
    private var enableAnnotationRendering = DEFAULT_ENABLE_ANNOTATION_RENDERING

    /**
     * A boolean flag to enable/disable double tap to zoom. Default: true
     */
    private var enableDoubleTapZoom = DEFAULT_ENABLE_DOUBLE_TAP_ZOOM

    /**
     * A boolean flag to enable/disable a scrolling indicator at the right of the page, that can be used fast scrolling. Default: true
     */
    private var showScrollIndicator = DEFAULT_SHOW_SCROLL_INDICATOR

    /**
     * A boolean flag to enable/disable the page number while the scroll indicator is tabbed. Default: true
     */
    private var showScrollIndicatorPageNumber = DEFAULT_SHOW_SCROLL_INDICATOR_PAGE_NUMBER

    /**
     * A color string to set the primary color of the view (affects: scroll indicator, navigation thumbnails and loading indicator). Default: #FF9400
     */
    private var primaryColor: String? = DEFAULT_PRIMARY_COLOR

    /**
     * A color string to set the secondary color of the view (affects: scroll indicator and navigation thumbnails). Default: #FFEFD8
     */
    private var secondaryColor: String? = DEFAULT_SECONDARY_COLOR

    /**
     * A color string to set the background of the pdf view that will be visible between the pages if pageSpacing > 0. Default: #EAEAEA
     */
    private var backgroundColor: String? = DEFAULT_BACKGROUND_COLOR

    /**
     * A boolean flag to enable/disable the help dialog. Default: true
     */
    private var enableHelpDialog = SplitDocumentFragment.DEFAULT_ENABLE_HELP_DIALOG

    /**
     * The title of the help dialog. If this is null but help dialog is displayed, a default title is used.
     */
    private var helpDialogTitle: String? = null

    /**
     * The text of the help dialog. If this is null but help dialog is displayed, a default text is used.
     */
    private var helpDialogText: String? = null

    private lateinit var binding: FragmentPdfBinding
    private lateinit var pdfNavigationAdapter: PdfAdapter
    private lateinit var annotationAdapter: AnnotationsAdapter

    private val navigateBottomSheet by lazy { binding.includedBottomSheetNavigate.bottomSheetNavigate }
    private lateinit var navigateBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    private val annotationsBottomSheet by lazy { binding.includedBottomSheetAnnotations.bottomSheetAnnotations }
    private lateinit var annotationsBottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>

    private lateinit var pdfiumCore: PdfiumCore

    private var pdfiumPdfDocument: PdfDocument? = null
    private var navViewSetupComplete = false
    private var annotationViewSetupComplete = false

    private var navPageSelected = false
    private var navViewOpen = false
    private var currentPage = 0

    private var textAnnotations = mutableListOf<PdfTextAnnotation>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfBinding.inflate(inflater, container, false)
        pdfiumCore = PdfiumCore(requireContext())

        // set the parameter from the savedInstanceState, or if it's null, from the arguments
        setParamsFromBundle(savedInstanceState ?: arguments)
        currentPage = savedInstanceState?.getInt(CURRENT_PAGE) ?: 0

        setupToolbar()

        pdfUri?.let {
            setupPdfView(it)

            val fileDescriptor: ParcelFileDescriptor? =
                requireContext().contentResolver.openFileDescriptor(it, "r")
            if (fileDescriptor != null) {
                try {
                    pdfiumPdfDocument = pdfiumCore.newDocument(fileDescriptor)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }

        // listen for the fragment result from the SplitDocumentFragment to get a list pdfUris resulting from the split
        setFragmentResultListener(SplitDocumentFragment.SPLIT_DOCUMENT_RESULT) { _, bundle ->
            // get the uri from the pdf files created when splitting the document
            val pdfUriList =
                bundle.getParcelableArrayList<Uri>(SplitDocumentFragment.SPLIT_PDF_URI_LIST)
            if (pdfUriList != null) {
                if (!pdfUriList.isNullOrEmpty()) {
                    // show toast with storage location
                    val file = pdfUriList.first().toFile()
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.split_document_success, "${file.parent}/"),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            // close SplitDocumentFragment to show PdfFragment again
            closeSplitDocumentView()
        }
        return binding.root
    }

    private fun setParamsFromBundle(bundle: Bundle?) {
        if (bundle != null) {
            val storedUri = bundle.getString(PDF_URI)
            if (!storedUri.isNullOrEmpty()) {
                pdfUri = Uri.parse(storedUri)
            }
            fileName = bundle.getString(FILE_NAME) ?: ""
            displayFileName = bundle.getBoolean(DISPLAY_FILE_NAME, DEFAULT_DISPLAY_FILE_NAME)
            pageSpacing = bundle.getInt(PAGE_SPACING, DEFAULT_PAGE_SPACING)
            enableThumbnailNavigationView = bundle.getBoolean(
                ENABLE_THUMBNAIL_NAVIGATION_VIEW,
                DEFAULT_ENABLE_THUMBNAIL_NAVIGATION_VIEW
            )
            enableSplitView = bundle.getBoolean(
                ENABLE_SPLIT_VIEW,
                DEFAULT_ENABLE_SPLIT_VIEW
            )
            enableAnnotationRendering =
                bundle.getBoolean(
                    ENABLE_ANNOTATION_RENDERING,
                    DEFAULT_ENABLE_ANNOTATION_RENDERING
                )
            enableDoubleTapZoom =
                bundle.getBoolean(ENABLE_DOUBLE_TAP_ZOOM, DEFAULT_ENABLE_DOUBLE_TAP_ZOOM)
            showScrollIndicator =
                bundle.getBoolean(SHOW_SCROLL_INDICATOR, DEFAULT_SHOW_SCROLL_INDICATOR)
            showScrollIndicatorPageNumber = bundle.getBoolean(
                SHOW_SCROLL_INDICATOR_PAGE_NUMBER,
                DEFAULT_SHOW_SCROLL_INDICATOR_PAGE_NUMBER
            )
            primaryColor = bundle.getString(PRIMARY_COLOR) ?: DEFAULT_PRIMARY_COLOR
            secondaryColor = bundle.getString(SECONDARY_COLOR) ?: DEFAULT_SECONDARY_COLOR
            backgroundColor = bundle.getString(BACKGROUND_COLOR) ?: DEFAULT_BACKGROUND_COLOR
            enableHelpDialog = bundle.getBoolean(
                ENABLE_HELP_DIALOG,
                SplitDocumentFragment.DEFAULT_ENABLE_HELP_DIALOG
            )
            helpDialogTitle = bundle.getString(HELP_DIALOG_TITLE)
            helpDialogText = bundle.getString(HELP_DIALOG_TEXT)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (pdfiumPdfDocument != null) {
            pdfiumCore.closeDocument(pdfiumPdfDocument)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navigateBottomSheetBehavior = BottomSheetBehavior.from(navigateBottomSheet)
        setThumbnailNavigationViewVisibility(false)

        annotationsBottomSheetBehavior = BottomSheetBehavior.from(annotationsBottomSheet)
        setAnnotationsViewVisibility(false)
    }

    /**
     * Parse attributes during inflation from a view hierarchy into the
     * arguments we handle.
     */
    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)

        // get the attributes data set via xml
        val a: TypedArray = context.obtainStyledAttributes(attrs, R.styleable.PdfFragment)
        a.getText(R.styleable.PdfFragment_file_uri)?.let {
            pdfUri = Uri.parse(it.toString())
        }
        a.getText(R.styleable.PdfFragment_file_name)?.let {
            fileName = it.toString()
        }
        displayFileName =
            a.getBoolean(R.styleable.PdfFragment_display_file_name, DEFAULT_DISPLAY_FILE_NAME)
        pageSpacing = a.getInteger(R.styleable.PdfFragment_page_spacing, DEFAULT_PAGE_SPACING)
        enableThumbnailNavigationView = a.getBoolean(
            R.styleable.PdfFragment_enable_thumbnail_navigation_view,
            DEFAULT_ENABLE_THUMBNAIL_NAVIGATION_VIEW
        )
        enableSplitView = a.getBoolean(
            R.styleable.PdfFragment_enable_split_view,
            DEFAULT_ENABLE_SPLIT_VIEW
        )
        enableAnnotationRendering = a.getBoolean(
            R.styleable.PdfFragment_enable_annotation_rendering,
            DEFAULT_ENABLE_ANNOTATION_RENDERING
        )
        enableDoubleTapZoom = a.getBoolean(
            R.styleable.PdfFragment_enable_double_tap_zoom,
            DEFAULT_ENABLE_DOUBLE_TAP_ZOOM
        )
        showScrollIndicator = a.getBoolean(
            R.styleable.PdfFragment_show_scroll_indicator,
            DEFAULT_SHOW_SCROLL_INDICATOR
        )
        showScrollIndicatorPageNumber = a.getBoolean(
            R.styleable.PdfFragment_show_scroll_indicator_page_number,
            DEFAULT_SHOW_SCROLL_INDICATOR_PAGE_NUMBER
        )
        a.getText(R.styleable.PdfFragment_primary_color)?.let {
            primaryColor = it.toString()
        }
        a.getText(R.styleable.PdfFragment_secondary_color)?.let {
            secondaryColor = it.toString()
        }
        a.getText(R.styleable.PdfFragment_background_color)?.let {
            backgroundColor = it.toString()
        }
        enableHelpDialog = a.getBoolean(
            R.styleable.PdfFragment_enable_help_dialog,
            SplitDocumentFragment.DEFAULT_ENABLE_HELP_DIALOG
        )
        a.getText(R.styleable.PdfFragment_help_dialog_title)?.let {
            helpDialogTitle = it.toString()
        }
        a.getText(R.styleable.PdfFragment_help_dialog_text)?.let {
            helpDialogText = it.toString()
        }
        a.recycle()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_PAGE, currentPage)
        outState.putString(PDF_URI, pdfUri.toString())
        outState.putString(FILE_NAME, fileName)
        outState.putBoolean(DISPLAY_FILE_NAME, displayFileName)
        outState.putInt(PAGE_SPACING, pageSpacing)
        outState.putBoolean(ENABLE_THUMBNAIL_NAVIGATION_VIEW, enableThumbnailNavigationView)
        outState.putBoolean(ENABLE_SPLIT_VIEW, enableSplitView)
        outState.putBoolean(ENABLE_ANNOTATION_RENDERING, enableAnnotationRendering)
        outState.putBoolean(ENABLE_DOUBLE_TAP_ZOOM, enableDoubleTapZoom)
        outState.putBoolean(SHOW_SCROLL_INDICATOR, showScrollIndicator)
        outState.putBoolean(SHOW_SCROLL_INDICATOR_PAGE_NUMBER, showScrollIndicatorPageNumber)
        outState.putString(PRIMARY_COLOR, primaryColor)
        outState.putString(SECONDARY_COLOR, secondaryColor)
        outState.putString(BACKGROUND_COLOR, backgroundColor)
        outState.putBoolean(ENABLE_HELP_DIALOG, enableHelpDialog)
        outState.putString(HELP_DIALOG_TITLE, helpDialogTitle)
        outState.putString(HELP_DIALOG_TEXT, helpDialogText)
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
        menu.getItem(0).isVisible = enableThumbnailNavigationView
        menu.getItem(1).isVisible = false //TODO: highlight
        menu.getItem(2).isVisible = true //TODO: annotate
        menu.getItem(3).isVisible = enableSplitView
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_navigate_pdf -> {
            if (navViewSetupComplete)
                toggleThumbnailNavigationViewVisibility()
            true
        }
        R.id.action_highlight -> {
            Log.i(TAG, "highlight selected")
            true
        }
        R.id.action_annotations -> {
            if (annotationViewSetupComplete)
                toggleAnnotationsViewVisibility()
            true
        }
        R.id.action_split_pdf -> {
            openSplitDocumentView()
            true
        }
        else -> {
            super.onOptionsItemSelected(item)
        }
    }

    private fun setupThumbnailNavigationView() {
        pdfiumPdfDocument?.let {
            val data = mutableListOf<PdfRecyclerItem>()
            for (i in 0 until binding.pdfView.pageCount) {
                data.add(PdfNavigationRecyclerItem(
                    pdfiumCore,
                    it,
                    i
                ) {
                    navPageSelected = true
                    scrollThumbnailNavigationViewToPage(i)
                    scrollToPage(i)
                    navPageSelected = false
                })
            }

            pdfNavigationAdapter = PdfAdapter(data, false, primaryColor, secondaryColor)
            binding.includedBottomSheetNavigate.rvPdfPages.adapter = pdfNavigationAdapter
            binding.includedBottomSheetNavigate.rvPdfPages.layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

            // make selection snappier as view holder can be reused
            val itemAnimator: DefaultItemAnimator = object : DefaultItemAnimator() {
                override fun canReuseUpdatedViewHolder(viewHolder: RecyclerView.ViewHolder): Boolean {
                    return true
                }
            }
            binding.includedBottomSheetNavigate.rvPdfPages.itemAnimator = itemAnimator

            navViewSetupComplete = true
        }
    }

    private fun setupAnnotationView() {
        pdfUri?.let { uri ->
            requireContext().pdfDocumentInReadingMode(uri)?.let { pdfDocument ->
                val data = mutableListOf<AnnotationRecyclerItem>()

                for (i in 1..pdfDocument.numberOfPages) {
                    val annotations = pdfDocument.getPage(i).annotations
                    for (annotation in annotations) {
                        if (annotation is PdfTextAnnotation) {
                            textAnnotations.add(annotation)
                            data.add(
                                AnnotationRecyclerItem(
                                    annotation.title.value,
                                    annotation.contents.value
                                ) {
                                    Log.i("####", "Annotation options selected!")
                                })
                        }
                    }
                }

                annotationAdapter = AnnotationsAdapter(data, primaryColor, secondaryColor)
                binding.includedBottomSheetAnnotations.rvAnnotations.adapter = annotationAdapter

                // disable user scrolling (arrows are used for navigation
                val layoutManager = object :
                    LinearLayoutManager(requireContext(), HORIZONTAL, false) {
                    override fun canScrollHorizontally(): Boolean {
                        return false
                    }
                }
                binding.includedBottomSheetAnnotations.rvAnnotations.layoutManager = layoutManager

                updateAnnotationArrowVisibility()
                binding.includedBottomSheetAnnotations.llLeftArrow.setOnClickListener {
                    if (annotationAdapter.selectedPosition > 0) {
                        scrollAnnotationsViewTo(annotationAdapter.selectedPosition - 1)
                    }
                    updateAnnotationArrowVisibility()
                }
                binding.includedBottomSheetAnnotations.llRightArrow.setOnClickListener {
                    if (annotationAdapter.selectedPosition < textAnnotations.size - 1) {
                        scrollAnnotationsViewTo(annotationAdapter.selectedPosition + 1)
                        updateAnnotationArrowVisibility()
                    }
                }

                annotationViewSetupComplete = true
            }
        }
    }

    private fun updateAnnotationArrowVisibility() {
        binding.includedBottomSheetAnnotations.llLeftArrow.visibility =
            if (annotationAdapter.selectedPosition > 0) VISIBLE else GONE
        binding.includedBottomSheetAnnotations.llRightArrow.visibility =
            if (annotationAdapter.selectedPosition < textAnnotations.size - 1) VISIBLE else GONE
    }

    private fun setupPdfView(pdfUri: Uri) {
        this.pdfUri = pdfUri
        val scrollHandle =
            if (showScrollIndicator) {
                PdfViewScrollHandle(
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
            .onPageError { page, error ->
                binding.pdfLoadingIndicator.visibility = GONE
                showPdfLoadError(error.message ?: "Unknown")
            }
            .onError { error ->
                binding.pdfLoadingIndicator.visibility = GONE
                showPdfLoadError(error.message ?: "Unknown")
            }
            .onPageScroll { _, _ ->
                // if user scrolls, close the navView
                if (!navPageSelected && navViewOpen) {
                    setThumbnailNavigationViewVisibility(false)
                }
            }
            .onPageChange { page, _ ->
                currentPage = page
            }
            .onLoad {
                setupThumbnailNavigationView()
                setupAnnotationView()
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

    private fun showPdfLoadError(reason: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage("DEV: Error loading the pdf file. Reason:\n$reason")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    /**
     * Opens the split document view for the currently visible pdf document
     */
    open fun openSplitDocumentView() {
        pdfUri?.let { uri ->
            val fragmentManager = requireActivity().supportFragmentManager
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            val fragment =
                SplitDocumentFragment.newInstance(
                    uri,
                    fileName,
                    primaryColor,
                    secondaryColor,
                    enableHelpDialog,
                    helpDialogTitle,
                    helpDialogText
                )
            fragmentTransaction.hide(this)
            fragmentTransaction.add(android.R.id.content, fragment, SplitDocumentFragment.TAG)
            fragmentTransaction.commit()
        }
    }

    /**
     * Closes the split document view if the SplitDocumentFragment TAG is found
     */
    open fun closeSplitDocumentView() {
        val fragmentManager = requireActivity().supportFragmentManager
        val splitDocumentFragment = fragmentManager.findFragmentByTag(SplitDocumentFragment.TAG)
        if (splitDocumentFragment != null) {
            val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.show(this)
            fragmentTransaction.remove(splitDocumentFragment)
            fragmentTransaction.commit()
        }
    }

    /**
     * Toggles the visibility state of the thumbnail navigation view
     */
    open fun toggleThumbnailNavigationViewVisibility() {
        // if bottom sheet is collapsed, set it to visible, if not set it to invisible
        setThumbnailNavigationViewVisibility(navigateBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
    }

    /**
     * Toggles the visibility state of the annotations view
     */
    open fun toggleAnnotationsViewVisibility() {
        // if bottom sheet is collapsed, set it to visible, if not set it to invisible
        setAnnotationsViewVisibility(annotationsBottomSheetBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
    }

    /**
     * Sets the visibility state of the thumbnail navigation view
     *
     * @param isVisible True when the view should be visible, false if it shouldn't.
     */
    open fun setThumbnailNavigationViewVisibility(isVisible: Boolean) {
        if (isVisible) {
            setAnnotationsViewVisibility(false)
            scrollThumbnailNavigationViewToPage(getCurrentItemPosition())
        }
        val updatedState =
            if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        navigateBottomSheetBehavior.state = updatedState
        navViewOpen = isVisible
    }

    /**
     * Sets the visibility state of the annotations view
     *
     * @param isVisible True when the view should be visible, false if it shouldn't.
     */
    open fun setAnnotationsViewVisibility(isVisible: Boolean) {
        if (isVisible) {
            setThumbnailNavigationViewVisibility(false)
        }
        val updatedState =
            if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        annotationsBottomSheetBehavior.state = updatedState
    }

    /**
     * Scrolls the pdf view to the given position.
     *
     * @param position  the index of the page this function should scroll to.
     * @param withAnimation boolean flag to define if scrolling should happen with or without animation.
     */
    open fun scrollToPage(position: Int, withAnimation: Boolean = false) {
        binding.pdfView.jumpTo(position, withAnimation)
    }

    /**
     * Scrolls the thumbnail navigation view to the given position
     *
     * @param position  the index of the page this function should scroll to.
     */
    open fun scrollThumbnailNavigationViewToPage(position: Int) {
        pdfNavigationAdapter.updateSelectedItem(position)
        (binding.includedBottomSheetNavigate.rvPdfPages.layoutManager as LinearLayoutManager).scrollToPosition(
            position
        )
    }

    /**
     * Returns the index of the currently visible page
     *
     * @return  the index of the visible page
     */
    open fun getCurrentItemPosition(): Int {
        return binding.pdfView.currentPage
    }

    /**
     * Scrolls the annotations view to the given position
     *
     * @param position  the index of the page this function should scroll to.
     */
    open fun scrollAnnotationsViewTo(position: Int) {
        annotationAdapter.selectedPosition = position
        (binding.includedBottomSheetAnnotations.rvAnnotations.layoutManager as LinearLayoutManager).scrollToPosition(
            position
        )
    }

    companion object {
        const val TAG = "PdfFragment"

        private const val CURRENT_PAGE = "CURRENT_PAGE"
        private const val FILE_NAME = "FILE_NAME"
        private const val PDF_URI = "PDF_URI"
        private const val DISPLAY_FILE_NAME = "DISPLAY_FILE_NAME"
        private const val PAGE_SPACING = "PAGE_SPACING"
        private const val ENABLE_THUMBNAIL_NAVIGATION_VIEW = "ENABLE_THUMBNAIL_NAVIGATION_VIEW"
        private const val ENABLE_SPLIT_VIEW = "ENABLE_SPLIT_VIEW"
        private const val ENABLE_ANNOTATION_RENDERING = "ENABLE_ANNOTATION_RENDERING"
        private const val ENABLE_DOUBLE_TAP_ZOOM = "ENABLE_DOUBLE_TAP_ZOOM"
        private const val SHOW_SCROLL_INDICATOR = "SHOW_SCROLL_INDICATOR"
        private const val SHOW_SCROLL_INDICATOR_PAGE_NUMBER = "SHOW_SCROLL_INDICATOR_PAGE_NUMBER"
        private const val PRIMARY_COLOR = "PRIMARY_COLOR"
        private const val SECONDARY_COLOR = "SECONDARY_COLOR"
        private const val BACKGROUND_COLOR = "BACKGROUND_COLOR"
        private const val ENABLE_HELP_DIALOG = "ENABLE_HELP_DIALOG"
        private const val HELP_DIALOG_TITLE = "HELP_DIALOG_TITLE"
        private const val HELP_DIALOG_TEXT = "HELP_DIALOG_TEXT"

        const val DEFAULT_DISPLAY_FILE_NAME = false
        const val DEFAULT_PAGE_SPACING = 10
        const val DEFAULT_ENABLE_THUMBNAIL_NAVIGATION_VIEW = true
        const val DEFAULT_ENABLE_SPLIT_VIEW = true
        const val DEFAULT_ENABLE_ANNOTATION_RENDERING = true
        const val DEFAULT_ENABLE_DOUBLE_TAP_ZOOM = true
        const val DEFAULT_SHOW_SCROLL_INDICATOR = true
        const val DEFAULT_SHOW_SCROLL_INDICATOR_PAGE_NUMBER = true
        const val DEFAULT_PRIMARY_COLOR = "#FF9400"
        const val DEFAULT_SECONDARY_COLOR = "#FFEFD8"
        const val DEFAULT_BACKGROUND_COLOR = "#EAEAEA"

        /**
         * Static function to create a new instance of the PdfFragment with the given settings
         *
         * @param pdfUri    The uri of the pdf that should be displayed. This is the only required param
         * @param fileName  The name of the file that should be displayed
         * @param displayFileName   A boolean flag that defines if the given file name should be displayed in the toolbar. Default: false
         * @param pageSpacing   The spacing in px between the pdf pages. Default: 20
         * @param enableThumbnailNavigationView A boolean flag to enable/disable pdf thumbnail navigation view. Default: true
         * @param enableSplitView A boolean flag to enable/disable pdf split view. Default: true
         * @param enableAnnotationRendering A boolean flag to enable/disable annotation rendering. Default: true
         * @param enableDoubleTapZoom   A boolean flag to enable/disable double tap to zoom. Default: true
         * @param showScrollIndicator   A boolean flag to enable/disable a scrolling indicator at the right of the page, that can be used fast scrolling. Default: true
         * @param showScrollIndicatorPageNumber A boolean flag to enable/disable the page number while the scroll indicator is tabbed. Default: true
         * @param primaryColor  A color string to set the primary color of the view (affects: scroll indicator, navigation thumbnails and loading indicator). Default: #FF9400
         * @param secondaryColor    A color string to set the secondary color of the view (affects: scroll indicator and navigation thumbnails). Default: #FFEFD8
         * @param backgroundColor   A color string to set the background of the pdf view that will be visible between the pages if pageSpacing > 0. Default: #EAEAEA@
         * @param enableHelpDialog  A boolean flag to enable/disable the help dialog on the split view
         * @param helpDialogTitle  The title of the help dialog on the split view. If this is null but help dialog is displayed, a default title is used.
         * @param helpDialogText  The text of the help dialog on the split view. If this is null but help dialog is displayed, a default text is used.
         * @return  in instance of PdfFragment with the given settings
         */
        fun newInstance(
            pdfUri: Uri,
            fileName: String? = null,
            displayFileName: Boolean? = null,
            pageSpacing: Int? = null,
            enableThumbnailNavigationView: Boolean? = null,
            enableSplitView: Boolean? = null,
            enableAnnotationRendering: Boolean? = null,
            enableDoubleTapZoom: Boolean? = null,
            showScrollIndicator: Boolean? = null,
            showScrollIndicatorPageNumber: Boolean? = null,
            primaryColor: String? = null,
            secondaryColor: String? = null,
            backgroundColor: String? = null,
            enableHelpDialog: Boolean? = null,
            helpDialogTitle: String? = null,
            helpDialogText: String? = null
        ): PdfFragment {
            val fragment = PdfFragment()

            val args = Bundle()
            args.putString(PDF_URI, pdfUri.toString())
            args.putString(FILE_NAME, fileName)
            if (displayFileName != null)
                args.putBoolean(DISPLAY_FILE_NAME, displayFileName)
            if (pageSpacing != null)
                args.putInt(PAGE_SPACING, pageSpacing)
            if (enableThumbnailNavigationView != null)
                args.putBoolean(ENABLE_THUMBNAIL_NAVIGATION_VIEW, enableThumbnailNavigationView)
            if (enableSplitView != null)
                args.putBoolean(ENABLE_SPLIT_VIEW, enableSplitView)
            if (enableAnnotationRendering != null)
                args.putBoolean(ENABLE_ANNOTATION_RENDERING, enableAnnotationRendering)
            if (enableDoubleTapZoom != null)
                args.putBoolean(ENABLE_DOUBLE_TAP_ZOOM, enableDoubleTapZoom)
            if (showScrollIndicator != null)
                args.putBoolean(SHOW_SCROLL_INDICATOR, showScrollIndicator)
            if (showScrollIndicatorPageNumber != null)
                args.putBoolean(SHOW_SCROLL_INDICATOR_PAGE_NUMBER, showScrollIndicatorPageNumber)
            args.putString(PRIMARY_COLOR, primaryColor)
            args.putString(SECONDARY_COLOR, secondaryColor)
            args.putString(BACKGROUND_COLOR, backgroundColor)
            if (enableHelpDialog != null)
                args.putBoolean(ENABLE_HELP_DIALOG, enableHelpDialog)
            args.putString(HELP_DIALOG_TITLE, helpDialogTitle)
            args.putString(HELP_DIALOG_TEXT, helpDialogText)
            fragment.arguments = args

            return fragment
        }
    }
}
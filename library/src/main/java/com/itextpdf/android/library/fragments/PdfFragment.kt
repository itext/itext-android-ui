package com.itextpdf.android.library.fragments

import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.use
import androidx.core.net.toFile
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.link.DefaultLinkHandler
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.itextpdf.android.library.R
import com.itextpdf.android.library.databinding.FragmentPdfBinding
import com.itextpdf.android.library.extensions.*
import com.itextpdf.android.library.lists.PdfAdapter
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.annotations.AnnotationRecyclerItem
import com.itextpdf.android.library.lists.annotations.AnnotationsAdapter
import com.itextpdf.android.library.lists.navigation.PdfNavigationRecyclerItem
import com.itextpdf.android.library.util.PdfManipulator
import com.itextpdf.android.library.views.PdfViewScrollHandle
import com.itextpdf.kernel.pdf.annot.PdfTextAnnotation
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore
import com.shockwave.pdfium.util.SizeF
import java.lang.reflect.Method


/**
 * Fragment that can be used to display a pdf file. To pass the pdf file and other settings to the
 * fragment use the static newInstance() function or set them as attributes in (e.g.: app:file_uri).
 */
open class PdfFragment : Fragment() {

    private lateinit var config: PdfConfig

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
    private var currentPageIndex = 0

    private var textAnnotations = mutableListOf<PdfTextAnnotation>()
    private var longPressPdfPagePosition: PointF? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setParamsFromBundle(savedInstanceState ?: arguments)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentPdfBinding.inflate(inflater, container, false)
        pdfiumCore = PdfiumCore(requireContext())

        currentPageIndex = savedInstanceState?.getInt(CURRENT_PAGE) ?: 0

        setupToolbar()
        setupPdfView()

        val fileDescriptor: ParcelFileDescriptor? = requireContext().contentResolver.openFileDescriptor(config.pdfUri, "r")
        if (fileDescriptor != null) {
            try {
                pdfiumPdfDocument = pdfiumCore.newDocument(fileDescriptor)
            } catch (exception: Exception) {
                exception.printStackTrace()
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

        binding.btnSaveAnnotation.setOnClickListener {
            longPressPdfPagePosition?.let { pdfPagePosition ->
                val annotationText = binding.etTextAnnotation.text.toString()
                createAnnotation(null, annotationText, pdfPagePosition)
                setAnnotationTextViewVisibility(false)
                binding.etTextAnnotation.text.clear()
            }
        }
        return binding.root
    }

    private fun showAnnotationContextMenu(
        v: View,
        @MenuRes menuRes: Int,
        annotation: PdfTextAnnotation
    ) {
        val popup = PopupMenu(requireContext(), v)
        popup.menuInflater.inflate(menuRes, popup.menu)

        enableAnnotationContextMenuIcons(popup)

        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.optionEdit -> {
                    Toast.makeText(
                        requireContext(),
                        "TODO: " + item.title + " -> " + annotation.contents.value,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                R.id.optionDelete -> {
                    removeAnnotation(annotation)
                }
            }
            true
        }
        popup.show()
    }

    private fun enableAnnotationContextMenuIcons(popup: PopupMenu) {
        try {
            val method: Method = popup.menu.javaClass.getDeclaredMethod(
                "setOptionalIconsVisible",
                Boolean::class.javaPrimitiveType
            )
            method.isAccessible = true
            method.invoke(popup.menu, true)
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun setParamsFromBundle(bundle: Bundle?) {

        if (bundle != null && bundle.containsKey(EXTRA_PDF_CONFIG)) {
            config = bundle.getParcelable(EXTRA_PDF_CONFIG)!!
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (pdfiumPdfDocument != null) {
            pdfiumCore.closeDocument(pdfiumPdfDocument)
        }
    }

    override fun onPause() {
        super.onPause()
        showKeyboard(false)
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        setAnnotationTextViewVisibility(false)
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

        val builder = PdfConfig.Builder()

        // get the attributes data set via xml
        context.obtainStyledAttributes(attrs, R.styleable.PdfFragment).use { a: TypedArray ->

            a.getTextIfAvailable(R.styleable.PdfFragment_file_uri) { builder.pdfUri = Uri.parse(it.toString()) }
            a.getTextIfAvailable(R.styleable.PdfFragment_file_name) { builder.fileName = it.toString() }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_display_file_name) { builder.displayFileName = it }
            a.getIntegerIfAvailable(R.styleable.PdfFragment_page_spacing) { builder.pageSpacing = it }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_enable_thumbnail_navigation_view) { builder.enableThumbnailNavigationView = it }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_enable_split_view) { builder.enableSplitView = it }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_enable_annotation_rendering) { builder.enableAnnotationRendering = it }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_enable_double_tap_zoom) { builder.enableDoubleTapZoom = it }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_show_scroll_indicator) { builder.showScrollIndicator = it }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_show_scroll_indicator_page_number) { builder.showScrollIndicatorPageNumber = it }
            a.getTextIfAvailable(R.styleable.PdfFragment_primary_color) { builder.primaryColor = it.toString() }
            a.getTextIfAvailable(R.styleable.PdfFragment_secondary_color) { builder.secondaryColor = it.toString() }
            a.getTextIfAvailable(R.styleable.PdfFragment_background_color) { builder.backgroundColor = it.toString() }
            a.getBooleanIfAvailable(R.styleable.PdfFragment_enable_help_dialog) { builder.enableHelpDialog = it }
            a.getTextIfAvailable(R.styleable.PdfFragment_help_dialog_title) { builder.helpDialogTitle = it.toString() }
            a.getTextIfAvailable(R.styleable.PdfFragment_help_dialog_text) { builder.helpDialogText = it.toString() }

        }

        config = builder.build()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(CURRENT_PAGE, currentPageIndex)
        outState.putParcelable(EXTRA_PDF_CONFIG, config)
    }

    private fun setupToolbar() {

        setHasOptionsMenu(true)

        val toolbar = binding.tbPdfFragment
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
        toolbar.setNavigationOnClickListener { requireActivity().onBackPressed() }
        toolbar.title = if (config.displayFileName) config.fileName else null

        val parentActivity: FragmentActivity? = activity

        when (parentActivity) {
            is AppCompatActivity -> parentActivity.setSupportActionBar(binding.tbPdfFragment)
            else -> Log.d(TAG, "Cannot set toolbar on parent activity $parentActivity.")
        }


    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_pdf_fragment, menu)
        menu.getItem(0).isVisible = config.enableThumbnailNavigationView
        menu.getItem(1).isVisible = config.enableHighlightView
        menu.getItem(2).isVisible = config.enableAnnotationView
        menu.getItem(3).isVisible = config.enableSplitView
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_navigate_pdf -> {
            if (navViewSetupComplete)
                toggleThumbnailNavigationViewVisibility()
            true
        }
        R.id.action_highlight -> {
            // only for testing (Create pdf file with dummy content)
//            val name = "writePdfTest.pdf"
//            val storageFolderPath =
//                (requireContext().externalCacheDir ?: requireContext().cacheDir).absolutePath
//            val pdfFile = File("$storageFolderPath/$name")
//
//            val newOutput = FileOutputStream(pdfFile)
//            writePdf(newOutput)
//
//            setupPdfView(pdfFile.toUri())
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

    private fun createAnnotation(title: String?, text: String, pdfPagePosition: PointF) {

        PdfManipulator.addTextAnnotationToPdf(
            context = requireContext(),
            fileUri = config.pdfUri,
            title = title,
            text = text,
            pageNumber = currentPageIndex + 1,
            x = pdfPagePosition.x,
            y = pdfPagePosition.y,
            bubbleSize = 30f,
            bubbleColor = config.primaryColor
        )

        setupPdfView()

    }

    private fun removeAnnotation(annotation: PdfTextAnnotation) {

        PdfManipulator.removeAnnotationFromPdf(
            context = requireContext(),
            fileUri = config.pdfUri,
            pageNumber = currentPageIndex + 1,
            annotation
        )

        setupPdfView()

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

            pdfNavigationAdapter = PdfAdapter(data, false, config.primaryColor, config.secondaryColor)
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

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.setHeaderTitle("Context Menu")
        menu.add(0, v.id, 0, "Upload")
        menu.add(0, v.id, 0, "Search")
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        Toast.makeText(requireContext(), "Selected Item: " + item.title, Toast.LENGTH_SHORT).show()
        return true
    }

    private fun setupAnnotationView() {

        requireContext().pdfDocumentInReadingMode(config.pdfUri)?.let { pdfDocument ->
            val data = mutableListOf<AnnotationRecyclerItem>()

            for (i in 1..pdfDocument.numberOfPages) {
                val annotations = pdfDocument.getPage(i).annotations
                for (annotation in annotations) {
                    if (annotation is PdfTextAnnotation) {
                        textAnnotations.add(annotation)
                        val title =
                            if (annotation.title != null) annotation.title.value else null
                        val text =
                            if (annotation.contents != null) annotation.contents.value else null

                        data.add(
                            AnnotationRecyclerItem(
                                title,
                                text
                            ) {
                                showAnnotationContextMenu(
                                    it,
                                    R.menu.popup_menu_annotation,
                                    annotation
                                )
                            })
                    }
                }
            }

            if (textAnnotations.size > 0) {
                binding.includedBottomSheetAnnotations.rvAnnotations.visibility = VISIBLE
                binding.includedBottomSheetAnnotations.llNoAnnotations.visibility = GONE
            } else {
                binding.includedBottomSheetAnnotations.rvAnnotations.visibility = GONE
                binding.includedBottomSheetAnnotations.llNoAnnotations.visibility = VISIBLE
            }

            annotationAdapter = AnnotationsAdapter(data, config.primaryColor, config.secondaryColor)
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

    private fun updateAnnotationArrowVisibility() {
        binding.includedBottomSheetAnnotations.llLeftArrow.visibility =
            if (annotationAdapter.selectedPosition > 0) VISIBLE else GONE
        binding.includedBottomSheetAnnotations.llRightArrow.visibility =
            if (annotationAdapter.selectedPosition < textAnnotations.size - 1) VISIBLE else GONE
    }

    private fun setupPdfView() {

        val scrollHandle = if (config.showScrollIndicator) {
            PdfViewScrollHandle(
                requireContext(),
                config.primaryColor,
                config.secondaryColor,
                config.showScrollIndicatorPageNumber
            )
        } else {
            null
        }

        binding.pdfLoadingIndicator.visibility = VISIBLE
        binding.pdfView.fromUri(config.pdfUri)
            .defaultPage(currentPageIndex)
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
                currentPageIndex = page
            }
            .onTap {
                val pdfPagePosition = convertScreenPointToPdfPagePoint(it, binding.pdfView)
                if (pdfPagePosition != null) {
                    val annotationIndex = findAnnotationIndexAtPosition(pdfPagePosition)
                    if (annotationIndex != null) {
                        scrollAnnotationsViewTo(annotationIndex)
                        setAnnotationsViewVisibility(true)
                    }
                }
                setAnnotationTextViewVisibility(false)
                true
            }
            .onLongPress { event ->
                longPressPdfPagePosition = convertScreenPointToPdfPagePoint(event, binding.pdfView)
                setAnnotationTextViewVisibility(true)
            }
            .onLoad {
                setupThumbnailNavigationView()
                setupAnnotationView()
                binding.pdfLoadingIndicator.visibility = GONE
            }
            .linkHandler(DefaultLinkHandler(binding.pdfView))
            .enableAnnotationRendering(config.enableAnnotationRendering)
            .spacing(config.pageSpacing)
            .enableDoubletap(config.enableDoubleTapZoom)
            .load()

        binding.pdfView.setBackgroundColor(Color.parseColor(config.backgroundColor))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.pdfLoadingIndicator.indeterminateDrawable.colorFilter =
                BlendModeColorFilter(Color.parseColor(config.primaryColor), BlendMode.SRC_ATOP)
        } else {
            binding.pdfLoadingIndicator.indeterminateDrawable.setColorFilter(
                Color.parseColor(config.primaryColor),
                PorterDuff.Mode.SRC_ATOP
            )
        }
    }

    private fun findAnnotationIndexAtPosition(position: PointF): Int? {
        for ((index, annotation) in textAnnotations.withIndex()) {
            if (annotation.isAtPosition(position)) {
                return index
            }
        }
        return null
    }

    private fun showPdfLoadError(reason: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error")
            .setMessage("DEV: Error loading the pdf file. Reason:\n$reason")
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    private fun showKeyboard(show: Boolean) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (show) {
            imm?.showSoftInput(binding.etTextAnnotation, InputMethodManager.SHOW_FORCED)
        } else {
            requireActivity().currentFocus?.let { view ->
                imm?.hideSoftInputFromWindow(view.windowToken, 0)
            }
        }
    }

    fun convertScreenPointToPdfPagePoint(e: MotionEvent, pdfView: PDFView): PointF? {
        val x = e.x
        val y = e.y

        val pdfFile = pdfView.pdfFile ?: return null
        val mappedX = -pdfView.currentXOffset + x
        val mappedY = -pdfView.currentYOffset + y
        val page =
            pdfFile.getPageAtOffset(if (pdfView.isSwipeVertical) mappedY else mappedX, pdfView.zoom)
        val pageSize: SizeF = pdfFile.getScaledPageSize(page, pdfView.zoom)
        val pageX: Int
        val pageY: Int
        if (pdfView.isSwipeVertical) {
            pageX = pdfFile.getSecondaryPageOffset(page, pdfView.zoom).toInt()
            pageY = pdfFile.getPageOffset(page, pdfView.zoom).toInt()
        } else {
            pageY = pdfFile.getSecondaryPageOffset(page, pdfView.zoom).toInt()
            pageX = pdfFile.getPageOffset(page, pdfView.zoom).toInt()
        }
        return pdfFile.mapDeviceCoordsToPage(
            page,
            pageX,
            pageY,
            pageSize.width.toInt(),
            pageSize.height.toInt(),
            0, //TODO: use real rotation
            mappedX.toInt(),
            mappedY.toInt()
        )
    }

    /**
     * Opens the split document view for the currently visible pdf document
     */
    open fun openSplitDocumentView() {

        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val fragment = SplitDocumentFragment.newInstance(config)
        fragmentTransaction.hide(this)
        fragmentTransaction.add(android.R.id.content, fragment, SplitDocumentFragment.TAG)
        fragmentTransaction.commit()
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
     * Toggles the visibility state of the annotation text view
     */
    open fun toggleAnnotationTextViewVisibility() {
        setAnnotationTextViewVisibility(binding.clAnnotationInput.visibility == GONE)
    }

    /**
     * Sets the visibility state of the thumbnail navigation view
     *
     * @param isVisible True when the view should be visible, false if it shouldn't.
     */
    open fun setThumbnailNavigationViewVisibility(isVisible: Boolean) {
        if (isVisible) {
            setAnnotationsViewVisibility(false)
            setAnnotationTextViewVisibility(false)
            scrollThumbnailNavigationViewToPage(getCurrentItemPosition())
        }
        view?.postDelayed({
            val updatedState =
                if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
            navigateBottomSheetBehavior.state = updatedState
            navViewOpen = isVisible
        }, OPEN_BOTTOM_SHEET_DELAY_MS)
    }

    /**
     * Sets the visibility state of the annotations view
     *
     * @param isVisible True when the view should be visible, false if it shouldn't.
     */
    open fun setAnnotationsViewVisibility(isVisible: Boolean) {
        if (isVisible) {
            setThumbnailNavigationViewVisibility(false)
            setAnnotationTextViewVisibility(false)
        }
        view?.postDelayed({
            val updatedState =
                if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
            annotationsBottomSheetBehavior.state = updatedState
        }, OPEN_BOTTOM_SHEET_DELAY_MS)
    }

    /**
     * Sets the visibility state of the annotation text view
     *
     * @param isVisible True when the view should be visible, false if it shouldn't.
     */
    open fun setAnnotationTextViewVisibility(isVisible: Boolean) {
        if (isVisible) {
            setThumbnailNavigationViewVisibility(false)
            setAnnotationsViewVisibility(false)
            binding.etTextAnnotation.requestFocus()
        }
        showKeyboard(isVisible)
        binding.clAnnotationInput.visibility = if (isVisible) VISIBLE else GONE
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

        private const val OPEN_BOTTOM_SHEET_DELAY_MS = 200L

        const val EXTRA_PDF_CONFIG = "EXTRA_PDF_CONFIG"

        private const val CURRENT_PAGE = "CURRENT_PAGE"


        /**
         * Static function to create a new instance of the PdfFragment with the given settings.
         *
         * @param pdfConfig The configuration to be used.
         * @return A new instance of [PdfFragment] with the given settings
         */
        fun newInstance(pdfConfig: PdfConfig): PdfFragment {
            val fragment = PdfFragment()
            fragment.arguments = bundleOf(EXTRA_PDF_CONFIG to pdfConfig)
            return fragment
        }
    }
}
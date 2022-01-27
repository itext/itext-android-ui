package com.itextpdf.android.library.fragments

import android.content.Context
import android.content.res.TypedArray
import android.net.Uri
import android.os.Bundle
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

    var fileName: String? = null
    var pdfUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPdfBinding.inflate(inflater, container, false)

        setupToolbar()

        if (savedInstanceState != null) {
            // Restore last state
            fileName = savedInstanceState.getString(FILE_NAME) ?: ""
            val storedUri = savedInstanceState.getString(PDF_URI)
            if (!storedUri.isNullOrEmpty()) {
                pdfUri = Uri.parse(storedUri)
            }
        }

        setupPdfView()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        actionsBottomSheetBehavior = BottomSheetBehavior.from(actionsBottomSheet)
        setBottomSheetVisibility(false)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(FILE_NAME, fileName)
        outState.putString(PDF_URI, pdfUri.toString())
    }

    private fun setupToolbar() {
        setHasOptionsMenu(true)
        if (::binding.isInitialized) {
            (requireActivity() as? AppCompatActivity)?.setSupportActionBar(binding.tbPdfFragment)
            binding.tbPdfFragment.setNavigationIcon(R.drawable.abc_ic_ab_back_material)
            binding.tbPdfFragment.setNavigationOnClickListener { requireActivity().onBackPressed() }
            binding.tbPdfFragment.title = null
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
            val position = getCurrentlyVisibleItemPosition()
            pdfNavigationAdapter.updateSelectedItem(position)
            (binding.includedBottomSheetActions.rvPdfPages.layoutManager as LinearLayoutManager).scrollToPosition(
                position
            )
        }
        val updatedState =
            if (isVisible) BottomSheetBehavior.STATE_EXPANDED else BottomSheetBehavior.STATE_COLLAPSED
        actionsBottomSheetBehavior.state = updatedState
    }

    private fun setupPdfNavigation() {
        pdfUri?.let {
            // prepare pre-defined pdf files from the assets folder to display them in a recyclerView
            val data = mutableListOf<PdfPageRecyclerItem>()
            for (i in 0 until binding.pdfView.pageCount) {
                data.add(PdfPageItem(it, i) {
                    pdfNavigationAdapter.updateSelectedItem(i)
                    scrollToPage(i)
                })
            }

            pdfNavigationAdapter = PdfNavigationAdapter(data)
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
        //TODO: manually let the user set stuff like spacing and color
        pdfUri?.let { pdfUri ->
            binding.pdfLoadingIndicator.visibility = VISIBLE
            binding.pdfView.fromUri(pdfUri)
//                .defaultPage(pageNumber)
//                .onPageChange(this)
                .scrollHandle(CustomScrollHandle(requireContext()))
                .onPageError { page, t ->
                    binding.pdfLoadingIndicator.visibility = GONE
                }
                .onLoad {
                    setupPdfNavigation()
                    binding.pdfLoadingIndicator.visibility = GONE
                }
                .enableAnnotationRendering(true)
                .spacing(10)
                .load()

//            binding.pdfView.setBackgroundColor(Color.LTGRAY)
        }
    }

    companion object {
        private const val TAG = "PdfFragment"
        private const val FILE_NAME = "FILE_NAME"
        private const val PDF_URI = "PDF_URI"
    }
}
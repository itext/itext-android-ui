package com.itextpdf.android.library.navigation

import android.graphics.Typeface
import android.graphics.drawable.DrawableContainer.DrawableContainerState
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.navigation.PdfPageRecyclerItem.Companion.TYPE_PDF_PAGE
import com.itextpdf.android.library.util.DisplayUtil
import com.itextpdf.android.library.views.PdfThumbnailView
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore


/**
 * The view holder for an item in the pdf navigation view.
 *
 * @param view  the view
 */
class PdfNavigationViewHolder(view: View) : PdfBaseNavigationViewHolder(view) {

    private val strokeWidth: Int = DisplayUtil.dpToPx(STROKE_WIDTH_IN_DP, itemView.context)

    override fun bind(item: PdfPageRecyclerItem) {
        if (item is PdfPageItem) {
            val pageNumber = item.pageIndex + 1
            tvPageNumber.text = "$pageNumber"
            thumbnailView.setMultiple(item.pdfiumCore, item.pdfDocument, item.pageIndex)

            itemView.setOnClickListener {
                item.action()
            }
        }
    }

    /**
     * Updates the text size of the view based on the selection state
     *
     * @param selected  boolean flag whether the item is selected or not
     */
    fun updateTextSize(selected: Boolean) {
        if (selected) {
            tvPageNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            tvPageNumber.setTypeface(null, Typeface.BOLD)
        } else {
            tvPageNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            tvPageNumber.setTypeface(null, Typeface.NORMAL)
        }
    }

    /**
     * Updates the border color of the item
     *
     * @param color the color int that should be used for the border
     */
    fun updateBorderColor(color: Int) {
        val drawableContainerState =
            thumbnailView.background.constantState as DrawableContainerState?
        val children = drawableContainerState?.children
        if (!children.isNullOrEmpty()) {
            val selectedDrawable = children[0] as? GradientDrawable
            selectedDrawable?.setStroke(strokeWidth, color)
        }
    }

    companion object {
        private const val STROKE_WIDTH_IN_DP = 1f
    }
}

/**
 * Base class of a viewHolder for displaying pdf thumbnail in a recyclerView.
 *
 * @param view  the view class required by the viewHolder
 */
abstract class PdfBaseNavigationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    protected val tvPageNumber: TextView = view.findViewById(R.id.tvPageNumber)
    protected val thumbnailView: PdfThumbnailView = view.findViewById(R.id.pageThumbnail)

    abstract fun bind(item: PdfPageRecyclerItem)
}

/**
 * The data class holding all the data required for the pdf navigation
 *
 * @property pdfiumCore required for rendering the pdf page that needs to be displayed as a thumbnail
 * @property pdfDocument    the pdfDocument that contains the page that should be rendered
 * @property pageIndex  the index of the page within the pdfDocument
 * @property action the action that should happen when the item is clicked
 */
data class PdfPageItem(
    val pdfiumCore: PdfiumCore,
    val pdfDocument: PdfDocument,
    val pageIndex: Int,
    val action: () -> Unit
) : PdfPageRecyclerItem {
    override val type: Int
        get() = TYPE_PDF_PAGE
}

/**
 * Interface that is used to define the type of the item, which in turn is used to specify the layout
 * of the item.
 */
interface PdfPageRecyclerItem {
    val type: Int

    companion object {
        val TYPE_PDF_PAGE = R.layout.recycler_item_navigation_pdf_page
    }
}

package com.itextpdf.android.library.navigation

import android.graphics.Bitmap
import android.graphics.Typeface
import android.graphics.drawable.DrawableContainer.DrawableContainerState
import android.graphics.drawable.GradientDrawable
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.util.DisplayUtil
import com.itextpdf.android.library.views.PdfThumbnailView
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore


/**
 * The view holder for an item in the pdf navigation view.
 *
 * @param view  the view
 */
abstract class PdfViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    protected val tvPageNumber: TextView = view.findViewById(R.id.tvPageNumber)
    protected val thumbnailView: PdfThumbnailView = view.findViewById(R.id.pageThumbnail)
    protected val strokeWidth: Int = DisplayUtil.dpToPx(STROKE_WIDTH_IN_DP, itemView.context)

    abstract fun bind(item: PdfPageRecyclerItem)

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
 * The data class holding all the data required for the pdf navigation
 *
 * @property pdfiumCore required for rendering the pdf page that needs to be displayed as a thumbnail
 * @property pdfDocument    the pdfDocument that contains the page that should be rendered
 * @property pageIndex  the index of the page within the pdfDocument
 * @property action the action that should happen when the item is clicked
 */
data class PdfPageRecyclerItem(
    val pdfiumCore: PdfiumCore,
    val pdfDocument: PdfDocument,
    val pageIndex: Int,
    val action: () -> Unit
) {
    var bitmap: Bitmap? = null
}

interface RecyclerItem {
    val type: Int

    companion object {
        val TYPE_NAVIGATE = R.layout.recycler_item_navigation_pdf_page
        val TYPE_SPLIT = R.layout.recycler_item_split_pdf_page
    }
}

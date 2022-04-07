package com.itextpdf.android.library.lists

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


/**
 * The view holder for an item in the pdf navigation view.
 *
 * @param view  the view
 */
internal abstract class PdfViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    protected val tvPageNumber: TextView = view.findViewById(R.id.tvPageNumber)
    protected val thumbnailView: PdfThumbnailView = view.findViewById(R.id.pageThumbnail)
    private val strokeWidth: Int = DisplayUtil.dpToPx(STROKE_WIDTH_IN_DP, itemView.context)

    abstract fun bind(item: PdfRecyclerItem)

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

internal interface PdfRecyclerItem {
    val type: Int

    companion object {
        val TYPE_NAVIGATE = R.layout.recycler_item_navigation_pdf_page
        val TYPE_SPLIT = R.layout.recycler_item_split_pdf_page
    }
}

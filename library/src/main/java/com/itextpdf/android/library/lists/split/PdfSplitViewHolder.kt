package com.itextpdf.android.library.lists.split

import android.graphics.Bitmap
import android.view.View
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.PdfViewHolder


/**
 * The view holder for an item in the pdf split view.
 *
 * @param view  the view
 */
class PdfSplitViewHolder(view: View) : PdfViewHolder(view) {

    override fun bind(item: PdfRecyclerItem) {
        if (item is PdfSplitRecyclerItem) {
            val pageNumber = item.pageIndex + 1
            tvPageNumber.text = "$pageNumber"
            thumbnailView.pdfImageView.setImageBitmap(item.bitmap)

            itemView.setOnClickListener {
                item.action()
            }
        }
    }
}

/**
 * The data class holding all the data required for the pdf splitting
 *
 * @property pageIndex  the index of the page within the pdfDocument
 * @property action the action that should happen when the item is clicked
 */
data class PdfSplitRecyclerItem(
    var bitmap: Bitmap,
    val pageIndex: Int,
    val action: () -> Unit
) : PdfRecyclerItem {
    override val type: Int
        get() = PdfRecyclerItem.TYPE_SPLIT
}
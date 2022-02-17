package com.itextpdf.android.library.lists.navigation

import android.view.View
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.PdfViewHolder
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore


/**
 * The view holder for an item in the pdf navigation view.
 *
 * @param view  the view
 */
class PdfNavigationViewHolder(view: View) : PdfViewHolder(view) {

    override fun bind(item: PdfRecyclerItem) {
        if (item is PdfNavigationRecyclerItem) {
            val pageNumber = item.pageIndex + 1
            tvPageNumber.text = "$pageNumber"
            thumbnailView.setMultiple(item.pdfiumCore, item.pdfDocument, item.pageIndex)

            itemView.setOnClickListener {
                item.action()
            }
        }
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
data class PdfNavigationRecyclerItem(
    val pdfiumCore: PdfiumCore,
    val pdfDocument: PdfDocument,
    val pageIndex: Int,
    val action: () -> Unit
) : PdfRecyclerItem {
    override val type: Int
        get() = PdfRecyclerItem.TYPE_NAVIGATE
}
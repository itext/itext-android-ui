package com.itextpdf.android.library.navigation

import android.graphics.Typeface
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.navigation.PdfPageRecyclerItem.Companion.TYPE_PDF_PAGE
import com.itextpdf.android.library.views.PdfThumbnailView

class PdfNavigationViewHolder(val view: View) : PdfBaseNavigationViewHolder(view) {

    override fun bind(item: PdfPageRecyclerItem) {
        if (item is PdfPageItem) {
            val pageNumber = item.pageIndex + 1
            tvPageNumber.text = "$pageNumber"
            thumbnailView.set(item.pdfUri, item.pageIndex)

            itemView.setOnClickListener {
                item.action()
            }
        }
    }
    
    fun updateTextSize(selected: Boolean) {
        if (selected) {
            tvPageNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            tvPageNumber.setTypeface(null, Typeface.BOLD)
        } else {
            tvPageNumber.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            tvPageNumber.setTypeface(null, Typeface.NORMAL)
        }
    }
}

/**
 * Base class of a viewHolder for displaying pdf page in a recyclerView.
 *
 * @param view  the view class required by the viewHolder
 */
abstract class PdfBaseNavigationViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    protected val tvPageNumber: TextView = view.findViewById(R.id.tvPageNumber)
    protected val thumbnailView: PdfThumbnailView = view.findViewById(R.id.pageThumbnail)

    abstract fun bind(item: PdfPageRecyclerItem)
}

data class PdfPageItem(
    val pdfUri: Uri,
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

package com.itextpdf.android.library.navigation

import android.view.View


/**
 * The view holder for an item in the pdf navigation view.
 *
 * @param view  the view
 */
class PdfNavigationViewHolder(view: View) : PdfViewHolder(view) {

    override fun bind(item: PdfPageRecyclerItem) {
        val pageNumber = item.pageIndex + 1
        tvPageNumber.text = "$pageNumber"
        //TODO: this is required for for thumbnails to work in preview and in activity (but bad performance)
//        thumbnailView.setMultiple(item.pdfiumCore, item.pdfDocument, item.pageIndex)

        // TODO: this has better performance
        thumbnailView.pdfImageView.setImageBitmap(item.bitmap)

        itemView.setOnClickListener {
            item.action()
        }
    }
}
package com.itextpdf.android.library.pdfview

import android.graphics.pdf.PdfRenderer
import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.extensions.renderAndClose

class PdfReaderViewHolder(itemView: View, private val pdfRenderer: PdfRenderer, private val pageWidth: Int) :
    RecyclerView.ViewHolder(itemView) {

    fun setPdfPage() {
        (itemView as ImageView).setImageBitmap(pdfRenderer.openPage(adapterPosition).renderAndClose(pageWidth))
    }
}
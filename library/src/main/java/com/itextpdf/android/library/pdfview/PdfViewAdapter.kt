package com.itextpdf.android.library.pdfview

import android.graphics.pdf.PdfRenderer
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R

class PdfViewAdapter(
    private val renderer: PdfRenderer,
    private val pageWidth: Int
) : RecyclerView.Adapter<PdfViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.recycler_item_pdf_page, parent, false)
        return PdfViewHolder(view, renderer, pageWidth)
    }

    override fun getItemCount(): Int {
        return renderer.pageCount
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        holder.setPdfPage()
    }
}
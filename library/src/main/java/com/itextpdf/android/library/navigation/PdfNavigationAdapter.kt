package com.itextpdf.android.library.navigation

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.navigation.PdfPageRecyclerItem.Companion.TYPE_PDF_PAGE

class PdfNavigationAdapter(val data: List<PdfPageRecyclerItem>) :
    RecyclerView.Adapter<PdfNavigationViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfNavigationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            viewType,
            parent,
            false
        )
        return when (viewType) {
            TYPE_PDF_PAGE -> PdfNavigationViewHolder(view)
            else -> throw IllegalStateException("Unsupported viewType $viewType")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    override fun onBindViewHolder(holder: PdfNavigationViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }
}
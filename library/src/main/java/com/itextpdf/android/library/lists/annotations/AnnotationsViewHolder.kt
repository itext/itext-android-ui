package com.itextpdf.android.library.lists.annotations

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.PdfViewHolder
import com.itextpdf.kernel.pdf.annot.PdfAnnotation
import com.shockwave.pdfium.PdfDocument
import com.shockwave.pdfium.PdfiumCore


/**
 * The view holder for an item in the annotations view.
 *
 * @param view  the view
 */
internal class AnnotationsViewHolder(view: View): RecyclerView.ViewHolder(view) {

    private val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    private val tvText: TextView = view.findViewById(R.id.tvText)
    private val ivMore: ImageView = view.findViewById(R.id.ivMore)

    fun bind(item: AnnotationRecyclerItem) {
        tvTitle.text = item.title
        tvText.text = item.text

        ivMore.setOnClickListener {
            item.action(it)
        }
    }
}

/**
 * The data class holding all the data required for an annotation
 *
 * @property action the action that should happen when the item is clicked
 */
internal data class AnnotationRecyclerItem(
    val annotation: PdfAnnotation,
    val title: String?,
    val text: String?,
    val action: (View) -> (Unit)
)
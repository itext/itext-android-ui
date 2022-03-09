package com.itextpdf.android.library.lists.annotations

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.lists.PdfRecyclerItem
import com.itextpdf.android.library.lists.PdfRecyclerItem.Companion.TYPE_NAVIGATE
import com.itextpdf.android.library.lists.PdfRecyclerItem.Companion.TYPE_SPLIT
import com.itextpdf.android.library.lists.PdfViewHolder
import com.itextpdf.android.library.lists.navigation.PdfNavigationViewHolder
import com.itextpdf.android.library.lists.split.PdfSplitViewHolder

/**
 * Adapter class for the annotations
 *
 * @property data   a list of annotation items
 *
 * @param primaryColorString    the primary color that is used for highlighting selected elements. optional
 * @param secondaryColorString  the secondary color that is used for highlighting selected elements. optional
 */
class AnnotationsAdapter(
    private val data: List<AnnotationRecyclerItem>,
    primaryColorString: String?,
    secondaryColorString: String?
) :
    RecyclerView.Adapter<AnnotationsViewHolder>() {

    var selectedPosition = 0

    private val primaryColor: Int? = if (primaryColorString != null) {
        Color.parseColor(primaryColorString)
    } else {
        null
    }
    private val secondaryColor: Int? = if (secondaryColorString != null) {
        Color.parseColor(secondaryColorString)
    } else {
        null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnotationsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_item_annotation,
            parent,
            false
        )
        return AnnotationsViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: AnnotationsViewHolder, position: Int) {
        val item = data[position]

        holder.bind(item)

        if (primaryColor != null && secondaryColor != null) {
            val background = holder.itemView.background
            DrawableCompat.setTint(background, secondaryColor)
        }
    }
}
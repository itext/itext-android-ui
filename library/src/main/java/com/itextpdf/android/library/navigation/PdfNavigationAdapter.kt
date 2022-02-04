package com.itextpdf.android.library.navigation

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R

/**
 * Adapter class for the pdf thumbnail navigation
 *
 * @property data   a list of page items
 *
 * @param primaryColorString    the primary color that is used for highlighting selected elements. optional
 * @param secondaryColorString  the secondary color that is used for highlighting selected elements. optional
 */
class PdfNavigationAdapter(
    private val data: List<PdfPageRecyclerItem>,
    primaryColorString: String?,
    secondaryColorString: String?
) :
    RecyclerView.Adapter<PdfNavigationViewHolder>() {
    private var selectedPos = RecyclerView.NO_POSITION

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
    private val backgroundColorNotSelected: Int = Color.parseColor(BACKGROUND_COLOR_NOT_SELECTED)
    private val borderColorNotSelected: Int = Color.parseColor(BORDER_COLOR_NOT_SELECTED)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfNavigationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_item_navigation_pdf_page,
            parent,
            false
        )
        return PdfNavigationViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: PdfNavigationViewHolder, position: Int) {
        val item = data[position]
        val selected = selectedPos == position

        holder.bind(item)
        holder.itemView.isSelected = selected
        holder.updateTextSize(selected)

        if (primaryColor != null && secondaryColor != null) {
            val background = holder.itemView.background
            if (selected) {
                DrawableCompat.setTint(background, secondaryColor)
                holder.updateBorderColor(primaryColor)
            } else {
                DrawableCompat.setTint(background, backgroundColorNotSelected)
                holder.updateBorderColor(borderColorNotSelected)
            }
        }
    }

    /**
     * Updates the selection
     *
     * @param selectedIndex the index of the newly selected item
     */
    fun updateSelectedItem(selectedIndex: Int) {
        notifyItemChanged(selectedPos)
        selectedPos = selectedIndex
        notifyItemChanged(selectedPos)
    }

    companion object {
        private const val BACKGROUND_COLOR_NOT_SELECTED = "#FFFFFFFF"
        private const val BORDER_COLOR_NOT_SELECTED = "#61323232"
    }
}
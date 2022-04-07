package com.itextpdf.android.library.lists

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.lists.PdfRecyclerItem.Companion.TYPE_NAVIGATE
import com.itextpdf.android.library.lists.PdfRecyclerItem.Companion.TYPE_SPLIT
import com.itextpdf.android.library.lists.navigation.PdfNavigationViewHolder
import com.itextpdf.android.library.lists.split.PdfSplitViewHolder

/**
 * Adapter class for the pdf thumbnail navigation
 *
 * @property data   a list of page items
 *
 * @param primaryColorString    the primary color that is used for highlighting selected elements. optional
 * @param secondaryColorString  the secondary color that is used for highlighting selected elements. optional
 */
internal class PdfAdapter(
    private val data: List<PdfRecyclerItem>,
    private val allowMultiSelection: Boolean,
    primaryColorString: String?,
    secondaryColorString: String?
) :
    RecyclerView.Adapter<PdfViewHolder>() {
    private var selectedPositions = mutableListOf<Int>()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            viewType,
            parent,
            false
        )

        return when (viewType) {
            TYPE_NAVIGATE -> PdfNavigationViewHolder(view)
            TYPE_SPLIT -> PdfSplitViewHolder(view)
            else -> throw IllegalStateException("Unsupported viewType $viewType")
        }
    }

    override fun getItemCount(): Int = data.size

    override fun getItemViewType(position: Int): Int {
        return data[position].type
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val item = data[position]
        val selected = selectedPositions.contains(position)

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
     * Returns a sorted list of selected positions
     *
     * @return the sorted list of positions
     */
    fun getSelectedPositions(): List<Int> {
        return selectedPositions.sorted()
    }

    /**
     * Updates the selection
     *
     * @param selectedIndex the index of the newly selected item
     */
    fun updateSelectedItem(selectedIndex: Int) {
        if (allowMultiSelection) {
            if (selectedPositions.contains(selectedIndex)) {
                selectedPositions.remove(selectedIndex)
            } else {
                selectedPositions.add(selectedIndex)
            }
        } else {
            if (selectedPositions.isNotEmpty()) {
                val temp = ArrayList(selectedPositions)
                selectedPositions.clear()
                temp.forEach { notifyItemChanged(it) }
            }
            selectedPositions.add(selectedIndex)
        }
        notifyItemChanged(selectedIndex)
    }

    companion object {
        private const val BACKGROUND_COLOR_NOT_SELECTED = "#FFFFFFFF"
        private const val BORDER_COLOR_NOT_SELECTED = "#61323232"
    }
}
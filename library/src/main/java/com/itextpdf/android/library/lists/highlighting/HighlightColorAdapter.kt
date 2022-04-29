package com.itextpdf.android.library.lists.highlighting

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R

/**
 * Adapter class for the highlight colors
 *
 * @property data   a list of highlight color items
 */
internal class HighlightColorAdapter(
    private val data: List<HighlightColorRecyclerItem>,
    primaryColorString: String?
) :
    RecyclerView.Adapter<HighlightColorViewHolder>() {

    var selectedPosition = 0
        private set

    private val borderColorSelected: Int = if (primaryColorString != null) {
        Color.parseColor(primaryColorString)
    } else {
        Color.parseColor(BLACK_COLOR)
    }
    private val borderColorNotSelected: Int = Color.parseColor(TRANSPARENT_COLOR)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighlightColorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.recycler_item_highlight_color,
            parent,
            false
        )
        return HighlightColorViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: HighlightColorViewHolder, position: Int) {
        val item = data[position]
        val selected = position == selectedPosition

        holder.bind(item)
        holder.itemView.isSelected = selected

        if (selected) {
            holder.updateBorderColor(borderColorSelected)
        } else {
            holder.updateBorderColor(borderColorNotSelected)
        }
    }

    fun updateSelectedItem(position: Int) {
        val oldSelection = selectedPosition
        selectedPosition = position
        notifyItemChanged(oldSelection)
        notifyItemChanged(selectedPosition)
    }

    companion object {
        private const val TRANSPARENT_COLOR = "#00000000"
        private const val BLACK_COLOR = "#000000"
    }
}
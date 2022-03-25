package com.itextpdf.android.library.lists.highlighting

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.itextpdf.android.library.R
import com.itextpdf.android.library.extensions.getHexString
import com.itextpdf.android.library.util.DisplayUtil
import com.itextpdf.kernel.colors.DeviceRgb


/**
 * The view holder for an item in the highlighting view.
 *
 * @param view  the view
 */
class HighlightColorViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    private val circleView: View = view.findViewById(R.id.colorCircle)
    private val strokeWidth: Int = DisplayUtil.dpToPx(STROKE_WIDTH_IN_DP, itemView.context)

    fun bind(item: HighlightColorRecyclerItem) {
        val gradientDrawable = circleView.background as? GradientDrawable
        gradientDrawable?.setColor(Color.parseColor(item.color.getHexString()))

        itemView.setOnClickListener {
            item.action(item.color)
        }
    }

    /**
     * Updates the border color of the item
     *
     * @param color the color int that should be used for the border
     */
    fun updateBorderColor(color: Int) {
        val selectedDrawable = circleView.background as? GradientDrawable
        selectedDrawable?.setStroke(strokeWidth, color)
    }

    companion object {
        private const val STROKE_WIDTH_IN_DP = 2f
    }
}

/**
 * The data class holding all the data required for a highlight color
 *
 * @property action the action that should happen when the item is clicked
 */
data class HighlightColorRecyclerItem(
    val color: DeviceRgb,
    val action: (com.itextpdf.kernel.colors.Color) -> (Unit)
)
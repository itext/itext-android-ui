package com.itextpdf.android.library.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.github.barteksc.pdfviewer.util.Util
import com.itextpdf.android.library.R


class CustomScrollHandle(context: Context, private val inverted: Boolean = false) :
    RelativeLayout(context), ScrollHandle {

    lateinit var pdfView: PDFView

    private var relativeHandleMiddle = 0f
    private var textView: TextView = TextView(context)
    private var currentPos = 0f
    private val viewHandler = Handler(Looper.getMainLooper())
    private val hidePageScrollerRunnable = Runnable { hide() }

    override fun setupLayout(pdfView: PDFView) {
        val align: Int
        val width: Int
        val height: Int
        val background: Drawable?
        // determine handle position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical) {
            width = HANDLE_SHORT
            height = HANDLE_LONG
            if (inverted) { // left
                align = ALIGN_PARENT_LEFT
                background =
                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            } else { // right
                align = ALIGN_PARENT_RIGHT
                background =
                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            }
        } else {
            width = HANDLE_LONG
            height = HANDLE_SHORT
            if (inverted) { // top
                align = ALIGN_PARENT_TOP
                background =
                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            } else { // bottom
                align = ALIGN_PARENT_BOTTOM
                background =
                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            }
        }
        setBackground(background)

        val lp = LayoutParams(
            Util.getDP(
                context, width
            ), Util.getDP(context, height)
        )
        lp.setMargins(0, 0, 0, 0)
        val tvlp =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        tvlp.addRule(CENTER_IN_PARENT, TRUE)
        addView(textView, tvlp)
        lp.addRule(align)
        pdfView.addView(this, lp)
        this.pdfView = pdfView
    }

    override fun destroyLayout() {
        pdfView.removeView(this)
    }

    override fun setScroll(position: Float) {
        if (!shown()) {
            show()
        } else {
            viewHandler.removeCallbacks(hidePageScrollerRunnable)
        }
        setPosition((if (pdfView.isSwipeVertical) pdfView.height else pdfView.width) * position)
    }

    private fun setPosition(position: Float) {
        var pos = position
        if (java.lang.Float.isInfinite(pos) || java.lang.Float.isNaN(pos)) {
            return
        }
        val pdfViewSize: Float = if (pdfView.isSwipeVertical) {
            pdfView.height.toFloat()
        } else {
            pdfView.width.toFloat()
        }
        pos -= relativeHandleMiddle
        if (pos < 0) {
            pos = 0f
        } else if (pos > pdfViewSize - Util.getDP(context, HANDLE_SHORT)) {
            pos = pdfViewSize - Util.getDP(context, HANDLE_SHORT)
        }
        if (pdfView.isSwipeVertical) {
            y = pos
        } else {
            x = pos
        }
        calculateMiddle()
        invalidate()
    }

    private fun calculateMiddle() {
        val pos: Float
        val viewSize: Float
        val pdfViewSize: Float
        if (pdfView.isSwipeVertical) {
            pos = y
            viewSize = height.toFloat()
            pdfViewSize = pdfView.height.toFloat()
        } else {
            pos = x
            viewSize = width.toFloat()
            pdfViewSize = pdfView.width.toFloat()
        }
        relativeHandleMiddle = (pos + relativeHandleMiddle) / pdfViewSize * viewSize
    }

    override fun hideDelayed() {
        viewHandler.postDelayed(hidePageScrollerRunnable, 1000)
    }

    override fun setPageNum(pageNum: Int) {
        val text = pageNum.toString()
        if (textView.text != text) {
            textView.text = text
        }
    }

    override fun shown(): Boolean {
        return visibility == VISIBLE
    }

    override fun show() {
        visibility = VISIBLE
    }

    override fun hide() {
        visibility = INVISIBLE
    }

    fun setTextColor(color: Int) {
        textView.setTextColor(color)
    }

    /**
     * @param size text size in dp
     */
    fun setTextSize(size: Int) {
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size.toFloat())
    }

    private val isPDFViewReady: Boolean
        get() = pdfView.pageCount > 0 && !pdfView.documentFitsView()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isPDFViewReady) {
            return super.onTouchEvent(event)
        }
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                pdfView.stopFling()
                viewHandler.removeCallbacks(hidePageScrollerRunnable)
                currentPos = if (pdfView.isSwipeVertical) {
                    event.rawY - y
                } else {
                    event.rawX - x
                }
                if (pdfView.isSwipeVertical) {
                    setPosition(event.rawY - currentPos + relativeHandleMiddle)
                    pdfView.setPositionOffset(relativeHandleMiddle / height.toFloat(), false)
                } else {
                    setPosition(event.rawX - currentPos + relativeHandleMiddle)
                    pdfView.setPositionOffset(relativeHandleMiddle / width.toFloat(), false)
                }
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                if (pdfView.isSwipeVertical) {
                    setPosition(event.rawY - currentPos + relativeHandleMiddle)
                    pdfView.setPositionOffset(relativeHandleMiddle / height.toFloat(), false)
                } else {
                    setPosition(event.rawX - currentPos + relativeHandleMiddle)
                    pdfView.setPositionOffset(relativeHandleMiddle / width.toFloat(), false)
                }
                return true
            }
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                hideDelayed()
                pdfView.performPageSnap()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    companion object {
        private const val HANDLE_LONG = 70
        private const val HANDLE_SHORT = 20
        private const val DEFAULT_TEXT_SIZE = 16
    }

    init {
        visibility = INVISIBLE
        setTextColor(Color.BLACK)
        setTextSize(DEFAULT_TEXT_SIZE)
    }
}

package com.itextpdf.android.library.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.barteksc.pdfviewer.PDFView
import com.github.barteksc.pdfviewer.scroll.ScrollHandle
import com.itextpdf.android.library.R


class CustomScrollHandle(context: Context, private val inverted: Boolean = false) :
    RelativeLayout(context), ScrollHandle {

    lateinit var pdfView: PDFView
    private val tvCurrentPage: TextView
    private val clPageIndicator: ConstraintLayout
    private val handleViewTouched: View
    private val handleViewNotTouched: View

    private var pageCount = 0
    private var relativeHandleMiddle = 0f
    private var currentPos = 0f
    private val viewHandler = Handler(Looper.getMainLooper())
    private val hidePageScrollerRunnable = Runnable { hide() }

    init {
        val inflater = LayoutInflater.from(context)
        inflater.inflate(R.layout.custom_scroll_handle, this, true)

        tvCurrentPage = findViewById(R.id.tvCurrentPage)
        clPageIndicator = findViewById(R.id.clPageIndicator)
        handleViewTouched = findViewById(R.id.handleViewTouched)
        handleViewNotTouched = findViewById(R.id.handleViewNotTouched)

        isHandleTouched(false)
        setTextSize(DEFAULT_TEXT_SIZE)
        hide()
    }

    override fun setupLayout(pdfView: PDFView) {
        val align: Int
        val width: Int
        val height: Int
        val background: Drawable?
        // determine handle position, default is right (when scrolling vertically) or bottom (when scrolling horizontally)
        if (pdfView.isSwipeVertical) {
            width = DEFAULT_HANDLE_SHORT_TOUCHED
            height = DEFAULT_HANDLE_LONG
            if (inverted) { // left
                align = ALIGN_PARENT_LEFT
//                background =
//                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            } else { // right
                align = ALIGN_PARENT_RIGHT
//                background =
//                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            }
        } else {
            width = DEFAULT_HANDLE_LONG
            height = DEFAULT_HANDLE_SHORT_TOUCHED
            if (inverted) { // top
                align = ALIGN_PARENT_TOP
//                background =
//                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            } else { // bottom
                align = ALIGN_PARENT_BOTTOM
//                background =
//                    ContextCompat.getDrawable(context, R.drawable.scroll_bar)
            }
        }
//        setBackground(background)

//        val lp = LayoutParams(
//            Util.getDP(
//                context, width
//            ), Util.getDP(context, height)
//        )
        val lp = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

//        lp.setMargins(0, 0, 0, 0)

        lp.addRule(align)

        pdfView.addView(this, lp)
        this.pdfView = pdfView

        pageCount = pdfView.pageCount
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
        val pdfViewSize: Float
        val handleSize: Float
        if (pdfView.isSwipeVertical) {
            pdfViewSize = pdfView.height.toFloat()
            handleSize = height.toFloat()
        } else {
            pdfViewSize = pdfView.width.toFloat()
            handleSize = width.toFloat()
        }
        pos -= relativeHandleMiddle
        if (pos < 0) {
            pos = 0f
        } else if (pos > pdfViewSize - handleSize) {
            pos = pdfViewSize - handleSize
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

    @SuppressLint("SetTextI18n")
    override fun setPageNum(pageNum: Int) {
        val currentPage = pageNum.toString()
        if (tvCurrentPage.text != currentPage) {
            tvCurrentPage.text = "$currentPage/$pageCount"
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
        tvCurrentPage.setTextColor(color)
    }

    /**
     * @param size text size in dp
     */
    fun setTextSize(size: Int) {
        tvCurrentPage.setTextSize(TypedValue.COMPLEX_UNIT_DIP, size.toFloat())
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
                isHandleTouched(true)
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
                isHandleTouched(false)
                hideDelayed()
                pdfView.performPageSnap()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private fun isHandleTouched(isTouched: Boolean) {
        if (isTouched) {
            handleViewTouched.visibility = VISIBLE
            handleViewNotTouched.visibility = INVISIBLE
            clPageIndicator.visibility = VISIBLE
        } else {
            handleViewTouched.visibility = INVISIBLE
            handleViewNotTouched.visibility = VISIBLE
            clPageIndicator.visibility = GONE
        }
    }

    companion object {
        private const val DEFAULT_HANDLE_LONG = 64
        private const val DEFAULT_HANDLE_SHORT_TOUCHED = 16
        private const val DEFAULT_HANDLE_SHORT_UNTOUCHED = 8
        private const val DEFAULT_TEXT_SIZE = 16
    }
}

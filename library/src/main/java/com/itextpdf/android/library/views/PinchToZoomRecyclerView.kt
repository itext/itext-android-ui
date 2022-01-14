package com.itextpdf.android.library.views

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import androidx.recyclerview.widget.RecyclerView

class PinchToZoomRecyclerView : RecyclerView {
    private var mActivePointerId = INVALID_POINTER_ID
    private var mScaleDetector: ScaleGestureDetector? = null
    private var mScaleFactor = 1f
    private var mLastScaleFactor = 1f
    private var maxWidth = 0.0f
    private var maxHeight = 0.0f
    private var mLastTouchX = 0f
    private var mLastTouchY = 0f
    private var mPosX = 0f
    private var mPosY = 0f
    private var width = 0f
    private var height = 0f
    private var minScale = 1f
    private var maxScale = 1.5f
    private var gestureDetector: GestureDetector? = null

    constructor(context: Context) : super(context) {
        setupDetectors()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        setupDetectors()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setupDetectors()
    }

    private fun setupDetectors() {
        if (!isInEditMode) {
            mScaleDetector = ScaleGestureDetector(getContext(), ScaleListener())
            gestureDetector = GestureDetector(getContext(), GestureListener())
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        width = MeasureSpec.getSize(widthMeasureSpec).toFloat()
        height = MeasureSpec.getSize(heightMeasureSpec).toFloat()
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        try {
            return super.onInterceptTouchEvent(ev)
        } catch (ex: IllegalArgumentException) {
            ex.printStackTrace()
        }
        return false
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(ev: MotionEvent): Boolean {
        super.onTouchEvent(ev)
        val action = ev.actionMasked
        mScaleDetector?.onTouchEvent(ev)
        gestureDetector?.onTouchEvent(ev)
        when (action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                val x = ev.x
                val y = ev.y
                mLastTouchX = x
                mLastTouchY = y
                mActivePointerId = ev.getPointerId(0)
            }
            MotionEvent.ACTION_MOVE -> {
                val pointerIndex = (action and MotionEvent.ACTION_POINTER_INDEX_MASK
                        shr MotionEvent.ACTION_POINTER_INDEX_SHIFT)
                val x = ev.getX(pointerIndex)
                val y = ev.getY(pointerIndex)
                val dx = x - mLastTouchX
                val dy = y - mLastTouchY
                mPosX += dx
                mPosY += dy
                if (mPosX > 0.0f) mPosX = 0.0f else if (mPosX < maxWidth) mPosX = maxWidth
                if (mPosY > 0.0f) mPosY = 0.0f else if (mPosY < maxHeight) mPosY = maxHeight
                mLastTouchX = x
                mLastTouchY = y
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                mActivePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_CANCEL -> {
                mActivePointerId = INVALID_POINTER_ID
            }
            MotionEvent.ACTION_POINTER_UP -> {
                val pointerIndex = action and MotionEvent.ACTION_POINTER_INDEX_MASK shr MotionEvent.ACTION_POINTER_INDEX_SHIFT
                val pointerId = ev.getPointerId(pointerIndex)
                if (pointerId == mActivePointerId) {
                    val newPointerIndex = if (pointerIndex == 0) 1 else 0
                    mLastTouchX = ev.getX(newPointerIndex)
                    mLastTouchY = ev.getY(newPointerIndex)
                    mActivePointerId = ev.getPointerId(newPointerIndex)
                }
            }
        }
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.save()
        canvas.translate(mPosX, mPosY)
        canvas.scale(mScaleFactor, mScaleFactor)
        canvas.restore()
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        if (mScaleFactor == 1.0f) {
            mPosX = 0.0f
            mPosY = 0.0f
        }
        canvas.translate(mPosX, mPosY)
        canvas.scale(mScaleFactor, mScaleFactor)
        super.dispatchDraw(canvas)
        canvas.restore()
        invalidate()
    }

    private inner class ScaleListener : SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            mLastScaleFactor = mScaleFactor
            mScaleFactor *= detector.scaleFactor
            mScaleFactor = 1.0f.coerceAtLeast(mScaleFactor.coerceAtMost(3.0f))
            maxWidth = width - width * mScaleFactor
            maxHeight = height - height * mScaleFactor
            val focusX = detector.focusX
            val focusY = detector.focusY

            val adjustedScaleFactor = mScaleFactor / mLastScaleFactor
            mPosX += (mPosX - focusX) * (adjustedScaleFactor - 1)
            mPosY += (mPosY - focusY) * (adjustedScaleFactor - 1)

            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            mScaleFactor = if (mScaleFactor == maxScale) {
                minScale
            } else {
                maxScale
            }

            // TODO: This code works for double tap, but MotionEvent.ACTION_MOVE interferes with it if there is a slight move at the end of the double tap
            e?.let {
                val adjustedScaleFactor = mScaleFactor / mLastScaleFactor
                mPosX += (mPosX - e.x) * (adjustedScaleFactor - 1)
                mPosY += (mPosY - e.y) * (adjustedScaleFactor - 1)
            }

            invalidate()
            return false
        }
    }

    companion object {
        private const val INVALID_POINTER_ID = -1
    }
}
package com.itextpdf.android.library.lists.highlighting

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.itextpdf.android.library.R
import com.itextpdf.android.library.util.ImageUtil
import com.itextpdf.kernel.geom.Rectangle
import kotlin.math.sqrt


class HighlightingPreview : View {
    private var points = mutableListOf<Point>()

    // the highlight color that should be used
    var color = ColorUtils.setAlphaComponent(Color.parseColor(HANDLE_COLOR), RECT_COLOR_ALPHA)

    var rectangle: Rectangle? = null

    /**
     * point1 and point 3 are of same group and same as point 2 and point4
     */
    var groupId = -1

    // array that holds the handles
    private val handles = ArrayList<Handle>()
    private var handleId = 0

    // variable to know which handle is being dragged
    private lateinit var paint: Paint
    var canvas: Canvas? = null

    constructor(context: Context?) : super(context) {
        setup()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        setup()
    }

    private fun setup() {
        paint = Paint()
        canvas = Canvas()
        // set this to get touch events
        isFocusable = true

        points.clear()
        handles.clear()
    }

    override fun onDraw(canvas: Canvas) {
        if (points.isEmpty())
            return
        var left = points[0].x
        var top = points[0].y
        var right = points[0].x
        var bottom = points[0].y
        for (i in 1 until points.size) {
            left = if (left > points[i].x) points[i].x else left
            top = if (top > points[i].y) points[i].y else top
            right = if (right < points[i].x) points[i].x else right
            bottom = if (bottom < points[i].y) points[i].y else bottom
        }
        paint.isAntiAlias = true
        paint.isDither = true
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeWidth = 5f

        // draw border
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#AA3C3C3C")
        paint.strokeWidth = 2f
        canvas.drawRect(
            (
                    left + handles[0].widthOfHandle / 2).toFloat(), (
                    top + handles[0].widthOfHandle / 2).toFloat(), (
                    right + handles[2].widthOfHandle / 2).toFloat(), (
                    bottom + handles[2].widthOfHandle / 2).toFloat(), paint
        )
        // fill the rectangle
        paint.style = Paint.Style.FILL
        paint.color = ColorUtils.setAlphaComponent(color, RECT_COLOR_ALPHA)
        paint.strokeWidth = 0f
        canvas.drawRect(
            (
                    left + handles[0].widthOfHandle / 2).toFloat(), (
                    top + handles[0].widthOfHandle / 2).toFloat(), (
                    right + handles[2].widthOfHandle / 2).toFloat(), (
                    bottom + handles[2].widthOfHandle / 2).toFloat(), paint
        )

        // draw handles
        // reset alpha by setting any fully-opaque color
        paint.color = Color.BLUE
        paint.strokeWidth = 0f
        for (i in handles.indices) {
            val handle = handles[i]
            canvas.drawBitmap(
                handle.bitmap, handle.x.toFloat(), handle.y.toFloat(),
                paint
            )
        }
    }

    // events when touching the screen
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val eventAction = event.action
        val x = event.x.toInt()
        val y = event.y.toInt()
        when (eventAction) {
            MotionEvent.ACTION_DOWN ->
                if (points.isEmpty()) {
                    //initialize rectangle.
                    points.add(Point())
                    points[0].x = x
                    points[0].y = y
                    points.add(Point())
                    points[1].x = x
                    points[1].y = y + 30
                    points.add(Point())
                    points[2].x = x + 30
                    points[2].y = y + 30
                    points.add(Point())
                    points[3].x = x + 30
                    points[3].y = y
                    handleId = 2
                    groupId = 1
                    // declare each handle
                    for ((index, pt) in points.withIndex()) {
                        handles.add(Handle(context, R.drawable.circle_shape_with_border, pt, index))
                    }
                } else {
                    //resize rectangle
                    handleId = -1
                    groupId = -1
                    var i = handles.size - 1
                    while (i >= 0) {
                        val handle = handles[i]
                        // get the center for the handle
                        val centerX = handle.x + handle.widthOfHandle / 2
                        val centerY = handle.y + handle.heightOfBall / 2
                        // calculate the radius from the touch to the center of the handle
                        val radCircle = sqrt(
                            ((centerX - x) * (centerX - x) + (centerY - y) * (centerY - y)).toDouble()
                        )
                        // check if touch was inside of the bounds of the handle (+ buffer to easier touch the handle)
                        if (radCircle < handle.widthOfHandle + HANDLE_BUFFER) {
                            handleId = handle.id
                            groupId = if (handleId == 1 || handleId == 3) {
                                2
                            } else {
                                1
                            }
                            invalidate()
                            break
                        }
                        invalidate()
                        i--
                    }
                }
            MotionEvent.ACTION_MOVE -> if (handleId > -1) {
                // move the balls the same as the finger
                handles[handleId].x = x
                handles[handleId].y = y
                if (groupId == 1) {
                    handles[1].x = handles[0].x
                    handles[1].y = handles[2].y
                    handles[3].x = handles[2].x
                    handles[3].y = handles[0].y
                } else {
                    handles[0].x = handles[1].x
                    handles[0].y = handles[3].y
                    handles[2].x = handles[3].x
                    handles[2].y = handles[1].y
                }
                invalidate()
            }
            MotionEvent.ACTION_UP -> {}
        }
        // redraw the canvas
        invalidate()
        return true
    }

    fun reset() {
        setup()
    }

    companion object {
        private const val HANDLE_BUFFER = 80
        private const val HANDLE_SIZE = 20
        private const val HANDLE_COLOR = "#3C3C3C"
        private const val RECT_COLOR_ALPHA = 80 // value between 0 and 255
    }

    class Handle(var context: Context, resourceId: Int, var point: Point, val id: Int) {
        var bitmap: Bitmap =
            ImageUtil.getResourceAsBitmap(context, resourceId, HANDLE_SIZE, HANDLE_COLOR)
                ?: Bitmap.createBitmap(HANDLE_SIZE, HANDLE_SIZE, Bitmap.Config.ARGB_8888)
        val widthOfHandle: Int
            get() = bitmap.width
        val heightOfBall: Int
            get() = bitmap.height
        var x: Int
            get() = point.x
            set(x) {
                point.x = x
            }
        var y: Int
            get() = point.y
            set(y) {
                point.y = y
            }
    }
}
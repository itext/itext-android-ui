package com.itextpdf.android.library.lists.highlighting

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.ColorUtils
import com.itextpdf.android.library.R
import com.itextpdf.android.library.extensions.toItextRectangle
import com.itextpdf.android.library.util.ImageUtil
import com.itextpdf.kernel.geom.Rectangle
import kotlin.math.sqrt


internal class HighlightingPreview : View {
    private var points = mutableListOf<Point>()

    // the highlight color that should be used
    var color = ColorUtils.setAlphaComponent(HANDLE_COLOR, RECT_COLOR_ALPHA)

    // the drawn rect
    private var rectF = RectF()

    // the x value of the last touch point (required to calc the delta for the move event)
    private var lastX = -1

    // the y value of the last touch point (required to calc the delta for the move event)
    private var lastY = -1

    /**
     * point 1 and point 3 are in group 1, point 2 and point 4 are in group 2 and all four together are in group 3
     */
    var groupId = GROUP_NONE

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

        // update rect
        rectF.left = (left + handles[0].width / 2).toFloat()
        rectF.top = (top + handles[0].width / 2).toFloat()
        rectF.right = (right + handles[2].width / 2).toFloat()
        rectF.bottom = (bottom + handles[2].width / 2).toFloat()

        // draw border
        paint.style = Paint.Style.STROKE
        paint.color = Color.parseColor("#AA3C3C3C")
        paint.strokeWidth = 2f
        canvas.drawRect(rectF, paint)

        // fill the rectangle
        paint.style = Paint.Style.FILL
        paint.color = ColorUtils.setAlphaComponent(color, RECT_COLOR_ALPHA)
        paint.strokeWidth = 0f
        canvas.drawRect(rectF, paint)

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
                    groupId = GROUP_PT_1_AND_3
                    // declare each handle
                    for ((index, pt) in points.withIndex()) {
                        handles.add(Handle(context, R.drawable.circle_shape_with_border, pt, index))
                    }
                } else {
                    //resize rectangle
                    handleId = -1
                    groupId = GROUP_NONE
                    var i = handles.size - 1
                    while (i >= 0) {
                        val handle = handles[i]
                        // get the center for the handle
                        val centerX = handle.x + handle.width / 2
                        val centerY = handle.y + handle.height / 2
                        // calculate the radius from the touch to the center of the handle
                        val radCircle = sqrt(((centerX - x) * (centerX - x) + (centerY - y) * (centerY - y)).toDouble())
                        // check if touch was inside of the bounds of the handle (+ buffer to easier touch the handle)
                        if (radCircle < handle.width + HANDLE_BUFFER) {
                            handleId = handle.id
                            groupId = if (handleId == 0 || handleId == 2) {
                                GROUP_PT_1_AND_3
                            } else {
                                GROUP_PT_2_AND_4
                            }
                            invalidate()
                            break
                        }
                        invalidate()
                        i--
                    }

                    // check if touch was inside of rectangle, if yes, assign group containing all points
                    if (rectF.contains(event.x, event.y)) {
                        groupId = GROUP_ALL_POINTS
                        invalidate()
                    }
                    lastX = x
                    lastY = y
                }
            MotionEvent.ACTION_MOVE -> if (handleId > -1) {
                // move the handles the same as the finger
                handles[handleId].x = x
                handles[handleId].y = y
                if (groupId == GROUP_PT_1_AND_3) {
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
            } else if (groupId == GROUP_ALL_POINTS) {
                // if group is 3, the touch was within the rect, therefore move the whole rect
                val diffX = x - lastX
                val diffY = y - lastY

                for (handle in handles) {
                    handle.x = handle.x + diffX
                    handle.y = handle.y + diffY
                }
                invalidate()

                lastX = x
                lastY = y
            }
            MotionEvent.ACTION_UP -> {
                lastX = -1
                lastY = -1
            }
        }
        // redraw the canvas
        invalidate()
        return true
    }

    fun reset() {
        setup()
    }

    fun getSelectionRectangle(): Rectangle {
        return rectF.toItextRectangle()
    }

    companion object {
        private const val HANDLE_BUFFER = 80
        private const val HANDLE_SIZE = 20
        private val HANDLE_COLOR = Color.parseColor("#3C3C3C")
        private const val RECT_COLOR_ALPHA = 80 // value between 0 and 255

        private const val GROUP_NONE = -1
        private const val GROUP_PT_1_AND_3 = 1
        private const val GROUP_PT_2_AND_4 = 2
        private const val GROUP_ALL_POINTS = 3
    }

    class Handle(var context: Context, resourceId: Int, var point: Point, val id: Int) {
        var bitmap: Bitmap =
            ImageUtil.getResourceAsBitmap(context, resourceId, HANDLE_SIZE, HANDLE_COLOR)
                ?: Bitmap.createBitmap(HANDLE_SIZE, HANDLE_SIZE, Bitmap.Config.ARGB_8888)
        val width: Int
            get() = bitmap.width
        val height: Int
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
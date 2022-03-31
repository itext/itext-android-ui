package com.itextpdf.android.library.extensions

import android.graphics.PointF
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.kernel.geom.Rectangle
import com.shockwave.pdfium.util.SizeF

fun PDFView.convertMotionEventPointToPdfPagePoint(e: MotionEvent): PointF? {
    return convertScreenPointToPdfPagePoint(e.x, e.y)
}

fun PDFView.convertScreenPointToPdfPagePoint(x: Float, y: Float): PointF? {
    if (pdfFile == null) return null
    val mappedX = -currentXOffset + x
    val mappedY = -currentYOffset + y
    val page =
        pdfFile.getPageAtOffset(if (isSwipeVertical) mappedY else mappedX, zoom)
    val pageSize: SizeF = pdfFile.getScaledPageSize(page, zoom)
    val pageX: Int
    val pageY: Int
    if (isSwipeVertical) {
        pageX = pdfFile.getSecondaryPageOffset(page, zoom).toInt()
        pageY = pdfFile.getPageOffset(page, zoom).toInt()
    } else {
        pageY = pdfFile.getSecondaryPageOffset(page, zoom).toInt()
        pageX = pdfFile.getPageOffset(page, zoom).toInt()
    }
    return pdfFile.mapDeviceCoordsToPage(
        page,
        pageX,
        pageY,
        pageSize.width.toInt(),
        pageSize.height.toInt(),
        0, //TODO: use real rotation
        mappedX.toInt(),
        mappedY.toInt()
    )
}

fun PDFView.convertScreenRectToPdfPageRect(screenRect: Rectangle): Rectangle? {
    // convert lowerLeft point of screenRect to pdfPoint -> Point(x, y+height)
    val convertedLowerLeft = convertScreenPointToPdfPagePoint(screenRect.x, screenRect.y + screenRect.height)
    // convert upperRight point of screenRect to pdfPoint -> Point (x+width, y)
    val convertedUpperRight = convertScreenPointToPdfPagePoint(screenRect.x + screenRect.width, screenRect.y)
    return if (convertedLowerLeft != null && convertedUpperRight != null) {
        val convertedWidth = convertedUpperRight.x - convertedLowerLeft.x
        val convertedHeight = convertedUpperRight.y - convertedLowerLeft.y
        Rectangle(convertedLowerLeft.x, convertedLowerLeft.y, convertedWidth, convertedHeight)
    } else {
        null
    }
}
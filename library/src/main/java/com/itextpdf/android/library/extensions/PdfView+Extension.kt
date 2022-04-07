package com.itextpdf.android.library.extensions

import android.graphics.Point
import android.graphics.PointF
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.kernel.geom.Rectangle
import com.shockwave.pdfium.util.SizeF

internal fun PDFView.convertMotionEventPointToPdfPagePoint(e: MotionEvent): PointF? {
    return convertScreenPointToPdfPagePoint(e.x, e.y)
}

/**
 * Retrieves the page-index where the [motionEvent] occurred.
 *
 * @return Zero-based page-index.
 */
internal fun PDFView.getPageIndexForClickPosition(motionEvent: MotionEvent): Int? {
    return getPageIndexAtScreenPoint(motionEvent.x, motionEvent.y)
}

internal fun PDFView.getPageIndexAtScreenPoint(x: Float, y: Float): Int? {
    if (pdfFile == null) return null

    val mappedX = -currentXOffset + x
    val mappedY = -currentYOffset + y

    return pdfFile.getPageAtOffset(if (isSwipeVertical) mappedY else mappedX, zoom)
}

internal fun PDFView.convertScreenPointToPdfPagePoint(x: Float, y: Float): PointF? {
    if (pdfFile == null) return null
    val mappedX = -currentXOffset + x
    val mappedY = -currentYOffset + y
    val pageIndex =
        pdfFile.getPageAtOffset(if (isSwipeVertical) mappedY else mappedX, zoom)
    val pageSize: SizeF = pdfFile.getScaledPageSize(pageIndex, zoom)
    val pageX: Int
    val pageY: Int
    if (isSwipeVertical) {
        pageX = pdfFile.getSecondaryPageOffset(pageIndex, zoom).toInt()
        pageY = pdfFile.getPageOffset(pageIndex, zoom).toInt()
    } else {
        pageY = pdfFile.getSecondaryPageOffset(pageIndex, zoom).toInt()
        pageX = pdfFile.getPageOffset(pageIndex, zoom).toInt()
    }
    return pdfFile.mapDeviceCoordsToPage(
        pageIndex,
        pageX,
        pageY,
        pageSize.width.toInt(),
        pageSize.height.toInt(),
        0, //TODO: use real rotation
        mappedX.toInt(),
        mappedY.toInt()
    )
}

internal fun PDFView.convertScreenRectToPdfPageRect(screenRect: Rectangle): Rectangle? {
    // convert lowerLeft point of screenRect to pdfPoint -> Point(x, y+height)
    val convertedLowerLeft = convertScreenPointToPdfPagePoint(screenRect.x, screenRect.y + screenRect.height)
    // convert upperRight point of screenRect to pdfPoint -> Point (x+width, y)
    val convertedUpperRight = convertScreenPointToPdfPagePoint(screenRect.x + screenRect.width, screenRect.y)
    return if (convertedLowerLeft != null && convertedUpperRight != null) {
        // make sure both points are on the same pdf page -> if not correct the lowerLeft
        if (convertedLowerLeft.y > convertedUpperRight.y) {
            convertedLowerLeft.y = 0f
        }

        val convertedWidth = convertedUpperRight.x - convertedLowerLeft.x
        val convertedHeight = convertedUpperRight.y - convertedLowerLeft.y
        Rectangle(convertedLowerLeft.x, convertedLowerLeft.y, convertedWidth, convertedHeight)
    } else {
        null
    }
}

internal fun PDFView.convertPdfPagePointToScreenPoint(pagePoint: PointF, pageIndex: Int): Point? {
    val x = pagePoint.x
    val y = pagePoint.y

    if (pdfFile == null) return null

    val pageSize = pdfFile.getScaledPageSize(pageIndex, zoom)
    val pageSpacing = spacingPx * zoom

    val startX: Int
    val startY: Int
    if (isSwipeVertical) {
        startX = currentXOffset.toInt()
        startY = (currentYOffset + (pageSize.height + pageSpacing) * pageIndex).toInt()
    } else {
        startY = currentYOffset.toInt()
        startX = (currentXOffset + (pageSize.width + pageSpacing) * pageIndex).toInt()
    }

    return pdfFile.mapPageCoordsToDevice(
        pageIndex,
        startX,
        startY,
        pageSize.width.toInt(),
        pageSize.height.toInt(),
        0, //TODO: use real rotation
        x.toDouble(),
        y.toDouble()
    )
}
package com.itextpdf.android.library.extensions

import android.graphics.Point
import android.graphics.PointF
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView
import com.itextpdf.kernel.geom.Rectangle
import com.shockwave.pdfium.util.SizeF

/**
 * Retrieves the page-index where the [motionEvent] occurred.
 *
 * @return Zero-based page-index.
 */
internal fun PDFView.getPageIndexForClickPosition(motionEvent: MotionEvent): Int? {
    return getPageIndexAtScreenPoint(motionEvent.x, motionEvent.y)
}

/**
 * Retrieves the page-index where from the [x] and [y] point on the screen.
 *
 * @return Zero-based page-index.
 */
internal fun PDFView.getPageIndexAtScreenPoint(x: Float, y: Float): Int? {
    if (pdfFile == null) return null

    val mappedX = -currentXOffset + x
    val mappedY = -currentYOffset + y

    return pdfFile.getPageAtOffset(if (isSwipeVertical) mappedY else mappedX, zoom)
}

/**
 * Convert the screen based [motionEvent] to a point in the pdf page coordinate system.
 *
 * @return PointF in page coordinate system.
 */
internal fun PDFView.convertMotionEventPointToPdfPagePoint(motionEvent: MotionEvent): PointF? {
    return convertScreenPointToPdfPagePoint(motionEvent.x, motionEvent.y)
}

/**
 * Convert the screen based [screenRect] to a rectangle in the pdf page coordinate system. The pdf rectangle will also be corrected and cut off if the
 * [screenRect] spans over multiple pages. The point that defines on which page the rect will be is the top left of the [screenRect].
 *
 * @return The rectangle in pdf page coordinates.
 */
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

/**
 * Convert the screen based [x] and [y] to a point in the pdf page coordinate system.
 *
 * @return PointF in page coordinate system.
 */
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

/**
 * Convert the pdf page based [pagePoint] to a point on the screen.
 *
 * @return Point on the screen.
 */
internal fun PDFView.convertPdfPagePointToScreenPoint(pagePoint: PointF, pageIndex: Int): Point? {
    val x = pagePoint.x
    val y = pagePoint.y

    if (pdfFile == null) return null

    val pageSize = pdfFile.getScaledPageSize(pageIndex, zoom)
    val pageSpacing = spacingPx * zoom

    val startX: Int
    val startY: Int
    if (isSwipeVertical) {
        // delta between pdfView width and page width -> has influence on where page actually starts
        val widthDelta = (width * zoom) - pageSize.width
        // use widthDelta/2 as page is centered and therefore first half of delta is before page, second half after
        val widthXOffset = widthDelta / 2
        startX = (currentXOffset + widthXOffset).toInt()
        startY = (currentYOffset + (pageSize.height + pageSpacing) * pageIndex).toInt()
    } else {
        // delta between pdfView height and page height -> has influence on where page actually starts
        val heightDelta = (height * zoom) - pageSize.height
        // use heightDelta/2 as page is centered and therefore first half of delta is before page, second half after
        val heightYOffset = heightDelta / 2
        startY = (currentYOffset + heightYOffset).toInt()
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
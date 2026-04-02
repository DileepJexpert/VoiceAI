package com.voiceai.app.util

import android.graphics.PointF
import android.graphics.RectF

/**
 * Edge detection helper for detecting document boundaries in camera frames.
 * Currently provides a placeholder implementation that returns a rectangle
 * slightly inset from the image bounds.
 */
object EdgeDetector {

    private const val INSET_FRACTION = 0.1f

    /**
     * Detects document boundaries in the given image dimensions.
     * Returns four corner points representing the detected document edges.
     *
     * Placeholder implementation: returns a rectangle inset by 10% from each edge.
     */
    fun detectDocumentBounds(imageWidth: Int, imageHeight: Int): List<PointF> {
        val insetX = imageWidth * INSET_FRACTION
        val insetY = imageHeight * INSET_FRACTION

        return listOf(
            PointF(insetX, insetY),
            PointF(imageWidth - insetX, insetY),
            PointF(imageWidth - insetX, imageHeight - insetY),
            PointF(insetX, imageHeight - insetY)
        )
    }

    /**
     * Returns the detected document region as a [RectF].
     *
     * Placeholder implementation: returns a rectangle inset by 10% from each edge.
     */
    fun detectDocumentRect(imageWidth: Int, imageHeight: Int): RectF {
        val insetX = imageWidth * INSET_FRACTION
        val insetY = imageHeight * INSET_FRACTION
        return RectF(insetX, insetY, imageWidth - insetX, imageHeight - insetY)
    }

    /**
     * Checks whether the detected bounds form a reasonably rectangular shape
     * that is likely to be a document.
     *
     * Placeholder implementation: always returns true.
     */
    fun isDocumentDetected(corners: List<PointF>): Boolean {
        if (corners.size != 4) return false
        // TODO: Implement actual shape validation (convexity, aspect ratio, area thresholds)
        return true
    }
}

/*
 * Copyright (C) 2008 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.journeyapps.barcodescanner

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.CameraPreview.StateListener

/**
 * This view is overlaid on top of the camera preview. It adds the viewfinder rectangle and partial
 * transparency outside it, as well as the laser scanner animation and result points.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
class ViewfinderView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    // Initialize these once for performance rather than calling them every time in onDraw().
    protected val paint: Paint =
        Paint(Paint.ANTI_ALIAS_FLAG)
    protected var resultBitmap: Bitmap? = null
    protected var maskColor: Int
    protected val resultColor: Int
    protected val laserColor: Int
    protected val resultPointColor: Int
    protected var laserVisibility: Boolean
    protected var scannerAlpha: Int
    protected var possibleResultPoints: MutableList<ResultPoint>
    protected var lastPossibleResultPoints: MutableList<ResultPoint>
    protected var cameraPreview: CameraPreview? = null

    // Cache the framingRect and previewSize, so that we can still draw it after the preview
    // stopped.
    protected var framingRect: Rect? = null
    protected var previewSize: Size? = null

    // This constructor is used when the class is built from an XML resource.
    init {
        val resources = resources

        // Get set attributes on view
        val attributes = getContext().obtainStyledAttributes(attrs, R.styleable.zxing_finder)

        this.maskColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_viewfinder_mask,
            resources.getColor(R.color.zxing_viewfinder_mask)
        )
        this.resultColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_result_view,
            resources.getColor(R.color.zxing_result_view)
        )
        this.laserColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_viewfinder_laser,
            resources.getColor(R.color.zxing_viewfinder_laser)
        )
        this.resultPointColor = attributes.getColor(
            R.styleable.zxing_finder_zxing_possible_result_points,
            resources.getColor(R.color.zxing_possible_result_points)
        )
        this.laserVisibility = attributes.getBoolean(
            R.styleable.zxing_finder_zxing_viewfinder_laser_visibility,
            true
        )

        attributes.recycle()

        scannerAlpha = 0
        possibleResultPoints = ArrayList(MAX_RESULT_POINTS)
        lastPossibleResultPoints = ArrayList(MAX_RESULT_POINTS)
    }

    fun setCameraPreview(view: CameraPreview) {
        this.cameraPreview = view
        view.addStateListener(object : StateListener {
            override fun previewSized() {
                refreshSizes()
                invalidate()
            }

            override fun previewStarted() {
            }

            override fun previewStopped() {
            }

            override fun cameraError(error: Exception?) {
            }

            override fun cameraClosed() {
            }
        })
    }

    protected fun refreshSizes() {
        if (cameraPreview == null) {
            return
        }
        val framingRect = cameraPreview!!.framingRect
        val previewSize = cameraPreview!!.previewSize
        if (framingRect != null && previewSize != null) {
            this.framingRect = framingRect
            this.previewSize = previewSize
        }
    }

    public override fun onDraw(canvas: Canvas) {
        refreshSizes()
        if (framingRect == null || previewSize == null) {
            return
        }

        val frame: Rect = framingRect as Rect
        val previewSize = previewSize!!

        val width = width
        val height = height

        // Draw the exterior (i.e. outside the framing rect) darkened
        paint.color = if (resultBitmap != null) resultColor else maskColor
        canvas.drawRect(0f, 0f, width.toFloat(), frame.top.toFloat(), paint)
        canvas.drawRect(
            0f,
            frame.top.toFloat(),
            frame.left.toFloat(),
            (frame.bottom + 1).toFloat(),
            paint
        )
        canvas.drawRect(
            (frame.right + 1).toFloat(),
            frame.top.toFloat(),
            width.toFloat(),
            (frame.bottom + 1).toFloat(),
            paint
        )
        canvas.drawRect(0f, (frame.bottom + 1).toFloat(), width.toFloat(), height.toFloat(), paint)

        if (resultBitmap != null) {
            // Draw the opaque result bitmap over the scanning rectangle
            paint.alpha = CURRENT_POINT_OPACITY
            canvas.drawBitmap(resultBitmap!!, null, frame, paint)
        } else {
            // If wanted, draw a red "laser scanner" line through the middle to show decoding is active
            if (laserVisibility) {
                paint.color = laserColor

                paint.alpha = SCANNER_ALPHA[scannerAlpha]
                scannerAlpha = (scannerAlpha + 1) % SCANNER_ALPHA.size

                val middle = frame.height() / 2 + frame.top
                canvas.drawRect(
                    (frame.left + 2).toFloat(),
                    (middle - 1).toFloat(),
                    (frame.right - 1).toFloat(),
                    (middle + 2).toFloat(),
                    paint
                )
            }

            val scaleX = this.width / previewSize.width.toFloat()
            val scaleY = this.height / previewSize.height.toFloat()

            // draw the last possible result points
            if (!lastPossibleResultPoints.isEmpty()) {
                paint.alpha = CURRENT_POINT_OPACITY / 2
                paint.color = resultPointColor
                val radius = POINT_SIZE / 2.0f
                for (point in lastPossibleResultPoints) {
                    canvas.drawCircle(
                        (point.x * scaleX).toInt().toFloat(),
                        (point.y * scaleY).toInt().toFloat(),
                        radius, paint
                    )
                }
                lastPossibleResultPoints.clear()
            }

            // draw current possible result points
            if (!possibleResultPoints.isEmpty()) {
                paint.alpha = CURRENT_POINT_OPACITY
                paint.color = resultPointColor
                for (point in possibleResultPoints) {
                    canvas.drawCircle(
                        (point.x * scaleX).toInt().toFloat(),
                        (point.y * scaleY).toInt().toFloat(),
                        POINT_SIZE.toFloat(), paint
                    )
                }

                // swap and clear buffers
                val temp = possibleResultPoints
                possibleResultPoints = lastPossibleResultPoints
                lastPossibleResultPoints = temp
                possibleResultPoints.clear()
            }

            // Request another update at the animation interval, but only repaint the laser line,
            // not the entire viewfinder mask.
            postInvalidateDelayed(
                ANIMATION_DELAY,
                frame.left - POINT_SIZE,
                frame.top - POINT_SIZE,
                frame.right + POINT_SIZE,
                frame.bottom + POINT_SIZE
            )
        }
    }

    fun drawViewfinder() {
        val resultBitmap = this.resultBitmap
        this.resultBitmap = null
        resultBitmap?.recycle()
        invalidate()
    }

    /**
     * Draw a bitmap with the result points highlighted instead of the live scanning display.
     *
     * @param result An image of the result.
     */
    fun drawResultBitmap(result: Bitmap?) {
        resultBitmap = result
        invalidate()
    }

    /**
     * Only call from the UI thread.
     *
     * @param point a point to draw, relative to the preview frame
     */
    fun addPossibleResultPoint(point: ResultPoint) {
        if (possibleResultPoints.size < MAX_RESULT_POINTS) possibleResultPoints.add(point)
    }

    fun setMaskColor(maskColor: Int) {
        this.maskColor = maskColor
    }

    fun setLaserVisibility(visible: Boolean) {
        this.laserVisibility = visible
    }

    companion object {
        protected val TAG: String = ViewfinderView::class.java.simpleName

        protected val SCANNER_ALPHA: IntArray = intArrayOf(0, 64, 128, 192, 255, 192, 128, 64)
        protected const val ANIMATION_DELAY: Long = 80L
        protected const val CURRENT_POINT_OPACITY: Int = 0xA0
        protected const val MAX_RESULT_POINTS: Int = 20
        protected const val POINT_SIZE: Int = 6
    }
}
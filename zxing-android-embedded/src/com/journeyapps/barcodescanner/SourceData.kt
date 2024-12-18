package com.journeyapps.barcodescanner

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.YuvImage
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ResultPoint
import java.io.ByteArrayOutputStream

/**
 * Raw preview data from a camera.
 */
class SourceData(
    data: ByteArray, dataWidth: Int, dataHeight: Int,
    /**
     * The format of the image data. ImageFormat.NV21 and ImageFormat.YUY2 are supported.
     */
    val imageFormat: Int,
    /**
     * Rotation in degrees (0, 90, 180 or 270). This is camera rotation relative to display rotation.
     */
    private val rotation: Int
) {
    private val data = RawImageData(data, dataWidth, dataHeight)

    /**
     * Set the crop rectangle.
     *
     * @param cropRect the new crop rectangle.
     */
    /**
     * Crop rectangle, in display orientation.
     */
    var cropRect: Rect? = null

    /**
     * Factor by which to scale down before decoding.
     */
    var scalingFactor: Int = 1

    var isPreviewMirrored: Boolean = false

    /**
     * @param data        the image data
     * @param dataWidth   width of the data
     * @param dataHeight  height of the data
     * @param imageFormat ImageFormat.NV21 or ImageFormat.YUY2
     * @param rotation    camera rotation relative to display rotation, in degrees (0, 90, 180 or 270).
     */
    init {
        require(dataWidth * dataHeight <= data.size) { "Image data does not match the resolution. " + dataWidth + "x" + dataHeight + " > " + data.size }
    }

    fun getData(): ByteArray {
        return data.data
    }

    val dataWidth: Int
        /**
         * @return width of the data
         */
        get() = data.width

    val dataHeight: Int
        /**
         * @return height of the data
         */
        get() = data.height

    fun translateResultPoint(point: ResultPoint): ResultPoint {
        var x = point.x * this.scalingFactor + cropRect!!.left
        val y = point.y * this.scalingFactor + cropRect!!.top
        if (isPreviewMirrored) {
            x = data.width - x
        }
        return ResultPoint(x, y)
    }

    val isRotated: Boolean
        /**
         * @return true if the preview image is rotated orthogonal to the display
         */
        get() = rotation % 180 != 0

    fun createSource(): PlanarYUVLuminanceSource {
        val rotated = data.rotateCameraPreview(rotation)
        val scaled = rotated.cropAndScale(cropRect!!, this.scalingFactor)

        // not the preview for decoding.
        return PlanarYUVLuminanceSource(
            scaled.data,
            scaled.width,
            scaled.height,
            0,
            0,
            scaled.width,
            scaled.height,
            false
        )
    }

    val bitmap: Bitmap
        /**
         * Return the source bitmap (cropped; in display orientation).
         *
         * @return the bitmap
         */
        get() = getBitmap(1)

    /**
     * Return the source bitmap (cropped; in display orientation).
     *
     * @param scaleFactor factor to scale down by. Must be a power of 2.
     * @return the bitmap
     */
    fun getBitmap(scaleFactor: Int): Bitmap {
        return getBitmap(cropRect, scaleFactor)
    }

    fun getBitmap(cropRect: Rect?, scaleFactor: Int): Bitmap {
        var cropRect = cropRect
        if (cropRect == null) {
            cropRect = Rect(0, 0, data.width, data.height)
        } else if (isRotated) {
            cropRect = Rect(cropRect.top, cropRect.left, cropRect.bottom, cropRect.right)
        }

        // TODO: there should be a way to do this without JPEG compression / decompression cycle.
        val img = YuvImage(data.data, imageFormat, data.width, data.height, null)
        val buffer = ByteArrayOutputStream()
        img.compressToJpeg(cropRect, 90, buffer)
        val jpegData = buffer.toByteArray()

        val options = BitmapFactory.Options()
        options.inSampleSize = scaleFactor
        var bitmap = BitmapFactory.decodeByteArray(jpegData, 0, jpegData.size, options)

        // Rotate if required
        if (rotation != 0) {
            val imageMatrix = Matrix()
            imageMatrix.postRotate(rotation.toFloat())
            bitmap =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, imageMatrix, false)
        }
        return bitmap
    }
}
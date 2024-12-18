package com.journeyapps.barcodescanner

import android.graphics.Rect

class RawImageData(@JvmField val data: ByteArray, @JvmField val width: Int, @JvmField val height: Int) {
    fun cropAndScale(cropRect: Rect, scale: Int): RawImageData {
        val width = cropRect.width() / scale
        val height = cropRect.height() / scale

        val top = cropRect.top

        val area = width * height
        val matrix = ByteArray(area)

        if (scale == 1) {
            var inputOffset = top * this.width + cropRect.left

            // Copy one cropped row at a time.
            for (y in 0 until height) {
                val outputOffset = y * width
                System.arraycopy(this.data, inputOffset, matrix, outputOffset, width)
                inputOffset += this.width
            }
        } else {
            var inputOffset = top * this.width + cropRect.left

            // Copy one cropped row at a time.
            for (y in 0 until height) {
                var outputOffset = y * width
                var xOffset = inputOffset
                for (x in 0 until width) {
                    matrix[outputOffset] = data[xOffset]
                    xOffset += scale
                    outputOffset += 1
                }
                inputOffset += this.width * scale
            }
        }
        return RawImageData(matrix, width, height)
    }


    fun rotateCameraPreview(cameraRotation: Int): RawImageData {
        return when (cameraRotation) {
            90 -> RawImageData(
                rotateCW(
                    data,
                    width,
                    height
                ), this.height, this.width
            )

            180 -> RawImageData(
                rotate180(
                    data,
                    width,
                    height
                ), this.width, this.height
            )

            270 -> RawImageData(
                rotateCCW(
                    data,
                    width,
                    height
                ), this.height, this.width
            )

            0 -> this
            else -> this
        }
    }

    companion object {
        /**
         * Rotate an image by 90 degrees CW.
         *
         * @param data        the image data, in with the first width * height bytes being the luminance data.
         * @param imageWidth  the width of the image
         * @param imageHeight the height of the image
         * @return the rotated bytes
         */
        fun rotateCW(data: ByteArray, imageWidth: Int, imageHeight: Int): ByteArray {
            // Adapted from http://stackoverflow.com/a/15775173
            // data may contain more than just y (u and v), but we are only interested in the y section.

            val yuv = ByteArray(imageWidth * imageHeight)
            var i = 0
            for (x in 0 until imageWidth) {
                for (y in imageHeight - 1 downTo 0) {
                    yuv[i] = data[y * imageWidth + x]
                    i++
                }
            }
            return yuv
        }

        /**
         * Rotate an image by 180 degrees.
         *
         * @param data        the image data, in with the first width * height bytes being the luminance data.
         * @param imageWidth  the width of the image
         * @param imageHeight the height of the image
         * @return the rotated bytes
         */
        fun rotate180(data: ByteArray, imageWidth: Int, imageHeight: Int): ByteArray {
            val n = imageWidth * imageHeight
            val yuv = ByteArray(n)

            var i = n - 1
            for (j in 0 until n) {
                yuv[i] = data[j]
                i--
            }
            return yuv
        }

        /**
         * Rotate an image by 90 degrees CCW.
         *
         * @param data        the image data, in with the first width * height bytes being the luminance data.
         * @param imageWidth  the width of the image
         * @param imageHeight the height of the image
         * @return the rotated bytes
         */
        fun rotateCCW(data: ByteArray, imageWidth: Int, imageHeight: Int): ByteArray {
            val n = imageWidth * imageHeight
            val yuv = ByteArray(n)
            var i = n - 1
            for (x in 0 until imageWidth) {
                for (y in imageHeight - 1 downTo 0) {
                    yuv[i] = data[y * imageWidth + x]
                    i--
                }
            }
            return yuv
        }
    }
}
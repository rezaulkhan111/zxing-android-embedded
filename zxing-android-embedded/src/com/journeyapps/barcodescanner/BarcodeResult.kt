package com.journeyapps.barcodescanner

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.zxing.BarcodeFormat
import com.google.zxing.Result
import com.google.zxing.ResultMetadataType
import com.google.zxing.ResultPoint
import java.util.Arrays


/**
 * This contains the result of a barcode scan.
 *
 *
 * This class delegate all read-only fields of [com.google.zxing.Result],
 * and adds a bitmap with scanned barcode.
 */
class BarcodeResult(
    /**
     * @return wrapped [com.google.zxing.Result]
     */
    var result: Result, protected var sourceData: SourceData
) {
    /**
     * @return Bitmap preview scale factor
     */
    val bitmapScaleFactor: Int = 2

    val bitmap: Bitmap
        /**
         * @return [Bitmap] with barcode preview
         * @see .getBitmapWithResultPoints
         */
        get() = sourceData.getBitmap(null, bitmapScaleFactor)

    val transformedResultPoints: List<ResultPoint>
        get() {
            if (result.resultPoints == null) {
                return emptyList()
            }
            return transformResultPoints(
                Arrays.asList(
                    *result.resultPoints
                ), this.sourceData
            )
        }

    /**
     * @param color Color of result points
     * @return [Bitmap] with result points on it, or plain bitmap, if no result points
     */
    fun getBitmapWithResultPoints(color: Int): Bitmap? {
        val bitmap = bitmap
        var barcode = bitmap
        val points = transformedResultPoints

        if (!points.isEmpty() && bitmap != null) {
            barcode = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(barcode)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            val paint = Paint()
            paint.color = color
            if (points.size == 2) {
                paint.strokeWidth = PREVIEW_LINE_WIDTH
                drawLine(
                    canvas, paint, points[0], points[1],
                    bitmapScaleFactor
                )
            } else if (points.size == 4 &&
                (result.barcodeFormat == BarcodeFormat.UPC_A ||
                        result.barcodeFormat == BarcodeFormat.EAN_13)
            ) {
                // Hacky special case -- draw two lines, for the barcode and metadata
                drawLine(
                    canvas, paint, points[0], points[1],
                    bitmapScaleFactor
                )
                drawLine(
                    canvas, paint, points[2], points[3],
                    bitmapScaleFactor
                )
            } else {
                paint.strokeWidth = PREVIEW_DOT_WIDTH
                for (point in points) {
                    if (point != null) {
                        canvas.drawPoint(
                            point.x / bitmapScaleFactor,
                            point.y / bitmapScaleFactor,
                            paint
                        )
                    }
                }
            }
        }
        return barcode
    }

    val text: String
        /**
         * @return raw text encoded by the barcode
         * @see Result.getText
         */
        get() = result.text

    val rawBytes: ByteArray
        /**
         * @return raw bytes encoded by the barcode, if applicable, otherwise `null`
         * @see Result.getRawBytes
         */
        get() = result.rawBytes

    val resultPoints: Array<ResultPoint>
        /**
         * @return points related to the barcode in the image. These are typically points
         * identifying finder patterns or the corners of the barcode. The exact meaning is
         * specific to the type of barcode that was decoded.
         * @see Result.getResultPoints
         */
        get() = result.resultPoints

    val barcodeFormat: BarcodeFormat
        /**
         * @return [BarcodeFormat] representing the format of the barcode that was decoded
         * @see Result.getBarcodeFormat
         */
        get() = result.barcodeFormat

    val resultMetadata: Map<ResultMetadataType, Any>
        /**
         * @return [Map] mapping [ResultMetadataType] keys to values. May be
         * `null`. This contains optional metadata about what was detected about the barcode,
         * like orientation.
         * @see Result.getResultMetadata
         */
        get() = result.resultMetadata

    val timestamp: Long
        get() = result.timestamp

    override fun toString(): String {
        return result.text
    }

    companion object {
        private const val PREVIEW_LINE_WIDTH = 4.0f
        private const val PREVIEW_DOT_WIDTH = 10.0f

        private fun drawLine(
            canvas: Canvas,
            paint: Paint,
            a: ResultPoint?,
            b: ResultPoint?,
            scaleFactor: Int
        ) {
            if (a != null && b != null) {
                canvas.drawLine(
                    a.x / scaleFactor,
                    a.y / scaleFactor,
                    b.x / scaleFactor,
                    b.y / scaleFactor,
                    paint
                )
            }
        }

        fun transformResultPoints(
            resultPoints: List<ResultPoint>,
            sourceData: SourceData
        ): List<ResultPoint> {
            val scaledPoints: MutableList<ResultPoint> = ArrayList(resultPoints.size)
            for (point in resultPoints) {
                scaledPoints.add(sourceData.translateResultPoint(point))
            }
            return scaledPoints
        }
    }
}
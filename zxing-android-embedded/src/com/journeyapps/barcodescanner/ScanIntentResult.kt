/*
 * Based on IntentResult.
 *
 * Copyright 2009 ZXing authors
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

import android.app.Activity
import android.content.Intent
import com.google.zxing.client.android.Intents

/**
 *
 * Encapsulates the result of a barcode scan invoked through [ScanContract].
 *
 * @author Sean Owen
 */
class ScanIntentResult @JvmOverloads internal constructor(
    /**
     * @return raw content of barcode
     */
    @JvmField val contents: String? = null,
    /**
     * @return name of format, like "QR_CODE", "UPC_A". See `BarcodeFormat` for more format names.
     */
    val formatName: String? = null,
    /**
     * @return raw bytes of the barcode content, if applicable, or null otherwise
     */
    val rawBytes: ByteArray? = null,
    /**
     * @return rotation of the image, in degrees, which resulted in a successful scan. May be null.
     */
    val orientation: Int? = null,
    /**
     * @return name of the error correction level used in the barcode, if applicable
     */
    val errorCorrectionLevel: String? = null,
    /**
     * @return path to a temporary file containing the barcode image, if applicable, or null otherwise
     */
    val barcodeImagePath: String? = null,
    /**
     * @return the original intent
     */
    @JvmField val originalIntent: Intent? = null
) {
    internal constructor(intent: Intent?) : this(null, null, null, null, null, null, intent)

    override fun toString(): String {
        val rawBytesLength = rawBytes?.size ?: 0
        return """Format: $formatName
Contents: $contents
Raw bytes: ($rawBytesLength bytes)
Orientation: $orientation
EC level: $errorCorrectionLevel
Barcode image: $barcodeImagePath
Original intent: $originalIntent
"""
    }


    companion object {
        /**
         * Parse activity result, without checking the request code.
         *
         * @param resultCode result code from `onActivityResult()`
         * @param intent     [Intent] from `onActivityResult()`
         * @return an [IntentResult] containing the result of the scan. If the user cancelled scanning,
         * the fields will be null.
         */
        fun parseActivityResult(resultCode: Int, intent: Intent): ScanIntentResult {
            if (resultCode == Activity.RESULT_OK) {
                val contents = intent.getStringExtra(Intents.Scan.RESULT)
                val formatName = intent.getStringExtra(Intents.Scan.RESULT_FORMAT)
                val rawBytes = intent.getByteArrayExtra(Intents.Scan.RESULT_BYTES)
                val intentOrientation =
                    intent.getIntExtra(Intents.Scan.RESULT_ORIENTATION, Int.MIN_VALUE)
                val orientation =
                    if (intentOrientation == Int.MIN_VALUE) null else intentOrientation
                val errorCorrectionLevel =
                    intent.getStringExtra(Intents.Scan.RESULT_ERROR_CORRECTION_LEVEL)
                val barcodeImagePath = intent.getStringExtra(Intents.Scan.RESULT_BARCODE_IMAGE_PATH)
                return ScanIntentResult(
                    contents,
                    formatName,
                    rawBytes,
                    orientation,
                    errorCorrectionLevel,
                    barcodeImagePath,
                    intent
                )
            }
            return ScanIntentResult(intent)
        }
    }
}
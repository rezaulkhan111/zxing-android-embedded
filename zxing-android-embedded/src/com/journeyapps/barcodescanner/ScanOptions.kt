/*
 * Based on IntentIntegrator, Copyright 2009 ZXing authors.
 *
 */
package com.journeyapps.barcodescanner

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.google.zxing.client.android.Intents
import java.util.Arrays
import java.util.Collections

class ScanOptions {
    private val moreExtras: MutableMap<String, Any> = HashMap(3)

    private var desiredBarcodeFormats: Collection<String>? = null

    private var captureActivity: Class<*>? = null


    protected val defaultCaptureActivity: Class<*>
        get() = CaptureActivity::class.java

    fun getCaptureActivity(): Class<*> {
        if (captureActivity == null) {
            captureActivity = defaultCaptureActivity
        }
        return captureActivity
    }

    /**
     * Set the Activity class to use. It can be any activity, but should handle the intent extras
     * as used here.
     *
     * @param captureActivity the class
     */
    fun setCaptureActivity(captureActivity: Class<*>?): ScanOptions {
        this.captureActivity = captureActivity
        return this
    }

    fun getMoreExtras(): Map<String, *> {
        return moreExtras
    }

    fun addExtra(key: String, value: Any): ScanOptions {
        moreExtras[key] = value
        return this
    }

    /**
     * Set a prompt to display on the capture screen, instead of using the default.
     *
     * @param prompt the prompt to display
     */
    fun setPrompt(prompt: String?): ScanOptions {
        if (prompt != null) {
            addExtra(Intents.Scan.PROMPT_MESSAGE, prompt)
        }
        return this
    }

    /**
     * By default, the orientation is locked. Set to false to not lock.
     *
     * @param locked true to lock orientation
     */
    fun setOrientationLocked(locked: Boolean): ScanOptions {
        addExtra(Intents.Scan.ORIENTATION_LOCKED, locked)
        return this
    }

    /**
     * Use the specified camera ID.
     *
     * @param cameraId camera ID of the camera to use. A negative value means "no preference".
     * @return this
     */
    fun setCameraId(cameraId: Int): ScanOptions {
        if (cameraId >= 0) {
            addExtra(Intents.Scan.CAMERA_ID, cameraId)
        }
        return this
    }

    /**
     * Set to true to enable initial torch
     *
     * @param enabled true to enable initial torch
     * @return this
     */
    fun setTorchEnabled(enabled: Boolean): ScanOptions {
        addExtra(Intents.Scan.TORCH_ENABLED, enabled)
        return this
    }


    /**
     * Set to false to disable beep on scan.
     *
     * @param enabled false to disable beep
     * @return this
     */
    fun setBeepEnabled(enabled: Boolean): ScanOptions {
        addExtra(Intents.Scan.BEEP_ENABLED, enabled)
        return this
    }

    /**
     * Set to true to enable saving the barcode image and sending its path in the result Intent.
     *
     * @param enabled true to enable barcode image
     * @return this
     */
    fun setBarcodeImageEnabled(enabled: Boolean): ScanOptions {
        addExtra(Intents.Scan.BARCODE_IMAGE_ENABLED, enabled)
        return this
    }

    /**
     * Set the desired barcode formats to scan.
     *
     * @param desiredBarcodeFormats names of `BarcodeFormat`s to scan for
     * @return this
     */
    fun setDesiredBarcodeFormats(desiredBarcodeFormats: Collection<String>?): ScanOptions {
        this.desiredBarcodeFormats = desiredBarcodeFormats
        return this
    }

    /**
     * Set the desired barcode formats to scan.
     *
     * @param desiredBarcodeFormats names of `BarcodeFormat`s to scan for
     * @return this
     */
    fun setDesiredBarcodeFormats(vararg desiredBarcodeFormats: String): ScanOptions {
        this.desiredBarcodeFormats = Arrays.asList(*desiredBarcodeFormats)
        return this
    }

    /**
     * Initiates a scan for all known barcode types with the default camera.
     * And starts a timer to finish on timeout
     *
     * @return Activity.RESULT_CANCELED and true on parameter TIMEOUT.
     */
    fun setTimeout(timeout: Long): ScanOptions {
        addExtra(Intents.Scan.TIMEOUT, timeout)
        return this
    }

    /**
     * Create an scan intent with the specified options.
     *
     * @return the intent
     */
    fun createScanIntent(context: Context?): Intent {
        val intentScan = Intent(context, getCaptureActivity())
        intentScan.setAction(Intents.Scan.ACTION)

        // check which types of codes to scan for
        if (desiredBarcodeFormats != null) {
            // set the desired barcode types
            val joinedByComma = StringBuilder()
            for (format in desiredBarcodeFormats!!) {
                if (joinedByComma.length > 0) {
                    joinedByComma.append(',')
                }
                joinedByComma.append(format)
            }
            intentScan.putExtra(Intents.Scan.FORMATS, joinedByComma.toString())
        }

        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        intentScan.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET)
        attachMoreExtras(intentScan)
        return intentScan
    }

    private fun attachMoreExtras(intent: Intent) {
        for ((key, value) in moreExtras) {
            // Kind of hacky
            if (value is Int) {
                intent.putExtra(key, value)
            } else if (value is Long) {
                intent.putExtra(key, value)
            } else if (value is Boolean) {
                intent.putExtra(key, value)
            } else if (value is Double) {
                intent.putExtra(key, value)
            } else if (value is Float) {
                intent.putExtra(key, value)
            } else if (value is Bundle) {
                intent.putExtra(key, value)
            } else if (value is IntArray) {
                intent.putExtra(key, value)
            } else if (value is LongArray) {
                intent.putExtra(key, value)
            } else if (value is BooleanArray) {
                intent.putExtra(key, value)
            } else if (value is DoubleArray) {
                intent.putExtra(key, value)
            } else if (value is FloatArray) {
                intent.putExtra(key, value)
            } else if (value is Array<*> && value.isArrayOf<String>()) {
                intent.putExtra(key, value as Array<String?>)
            } else {
                intent.putExtra(key, value.toString())
            }
        }
    }

    companion object {
        // supported barcode formats
        // Product Codes
        const val UPC_A: String = "UPC_A"
        const val UPC_E: String = "UPC_E"
        const val EAN_8: String = "EAN_8"
        const val EAN_13: String = "EAN_13"
        const val RSS_14: String = "RSS_14"

        // Other 1D
        const val CODE_39: String = "CODE_39"
        const val CODE_93: String = "CODE_93"
        const val CODE_128: String = "CODE_128"
        const val ITF: String = "ITF"

        const val RSS_EXPANDED: String = "RSS_EXPANDED"

        // 2D
        const val QR_CODE: String = "QR_CODE"
        const val DATA_MATRIX: String = "DATA_MATRIX"
        const val PDF_417: String = "PDF_417"


        val PRODUCT_CODE_TYPES: Collection<String> = list(UPC_A, UPC_E, EAN_8, EAN_13, RSS_14)
        val ONE_D_CODE_TYPES: Collection<String> = list(
            UPC_A, UPC_E, EAN_8, EAN_13, RSS_14, CODE_39, CODE_93, CODE_128,
            ITF, RSS_14, RSS_EXPANDED
        )

        val ALL_CODE_TYPES: Collection<String>? = null

        private fun list(vararg values: String): List<String> {
            return Collections.unmodifiableList(Arrays.asList(*values))
        }
    }
}
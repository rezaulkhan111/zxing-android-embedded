package com.journeyapps.barcodescanner

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.FrameLayout
import android.widget.TextView
import com.google.zxing.MultiFormatReader
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.DecodeFormatManager
import com.google.zxing.client.android.DecodeHintManager
import com.google.zxing.client.android.Intents
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.camera.CameraParametersCallback
import com.journeyapps.barcodescanner.camera.CameraSettings

/**
 * Encapsulates BarcodeView, ViewfinderView and status text.
 *
 * To customize the UI, use BarcodeView and ViewfinderView directly.
 */
open class DecoratedBarcodeView : FrameLayout {
    var barcodeView: BarcodeView? = null
    var viewFinder: ViewfinderView? = null
        private set
    var statusView: TextView? = null
        private set

    /**
     * The instance of @link TorchListener to send events callback.
     */
    private var torchListener: TorchListener? = null

    private inner class WrappedCallback(private val delegate: BarcodeCallback) : BarcodeCallback {
        override fun barcodeResult(result: BarcodeResult?) {
            delegate.barcodeResult(result)
        }

        override fun possibleResultPoints(resultPoints: List<ResultPoint>?) {
            if (!resultPoints.isNullOrEmpty()) {
                resultPoints?.forEach { point ->
                    viewFinder!!.addPossibleResultPoint(point)
                }
                delegate.possibleResultPoints(resultPoints)
            }
        }
    }

    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize(attrs)
    }

    /**
     * Initialize the view with the xml configuration based on styleable attributes.
     *
     * @param attrs The attributes to use on view.
     */
    /**
     * Initialize with no custom attributes set.
     */
    private fun initialize(attrs: AttributeSet? = null) {
        // Get attributes set on view
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.zxing_view)

        val scannerLayout = attributes.getResourceId(
            R.styleable.zxing_view_zxing_scanner_layout, R.layout.zxing_barcode_scanner
        )

        attributes.recycle()

        inflate(context, scannerLayout, this)

        barcodeView = findViewById(R.id.zxing_barcode_surface)

        requireNotNull(barcodeView) {
            "There is no a com.journeyapps.barcodescanner.BarcodeView on provided layout " +
                    "with the id \"zxing_barcode_surface\"."
        }

        // Pass on any preview-related attributes
        barcodeView!!.initializeAttributes(attrs)


        viewFinder = findViewById(R.id.zxing_viewfinderView)

        requireNotNull(viewFinder) {
            "There is no a com.journeyapps.barcodescanner.ViewfinderView on provided layout " +
                    "with the id \"zxing_viewfinder_view\"."
        }

        if (barcodeView != null) {
            viewFinder!!.setCameraPreview(barcodeView!!)
        }

        // statusView is optional
        statusView = findViewById(R.id.zxing_status_view)
    }

    /**
     * Convenience method to initialize camera id, decode formats and prompt message from an intent.
     *
     * @param intent the intent, as generated by IntentIntegrator
     */
    fun initializeFromIntent(intent: Intent) {
        // Scan the formats the intent requested, and return the result to the calling activity.
        val decodeFormats = DecodeFormatManager.parseDecodeFormats(intent)
        val decodeHints = DecodeHintManager.parseDecodeHints(intent)

        val settings = CameraSettings()

        if (intent.hasExtra(Intents.Scan.CAMERA_ID)) {
            val cameraId = intent.getIntExtra(Intents.Scan.CAMERA_ID, -1)
            if (cameraId >= 0) {
                settings.requestedCameraId = cameraId
            }
        }

        if (intent.hasExtra(Intents.Scan.TORCH_ENABLED)) {
            if (intent.getBooleanExtra(Intents.Scan.TORCH_ENABLED, false)) {
                this.setTorchOn()
            }
        }

        val customPromptMessage = intent.getStringExtra(Intents.Scan.PROMPT_MESSAGE)
        if (customPromptMessage != null) {
            setStatusText(customPromptMessage)
        }

        // Check what type of scan. Default: normal scan
        val scanType = intent.getIntExtra(Intents.Scan.SCAN_TYPE, 0)

        val characterSet = intent.getStringExtra(Intents.Scan.CHARACTER_SET)

        val reader = MultiFormatReader()
        reader.setHints(decodeHints)

        barcodeView!!.cameraSettings = settings
        barcodeView!!.setDecoderFactory(
            DefaultDecoderFactory(
                decodeFormats,
                decodeHints,
                characterSet,
                scanType
            )
        )
    }

    var decoderFactory: DecoderFactory?
        get() = barcodeView!!.getDecoderFactory()
        set(decoderFactory) {
            barcodeView!!.setDecoderFactory(decoderFactory)
        }

    var cameraSettings: CameraSettings
        get() = barcodeView!!.cameraSettings
        set(cameraSettings) {
            barcodeView!!.cameraSettings = cameraSettings
        }

    fun setStatusText(text: String?) {
        // statusView is optional when using a custom layout
        if (statusView != null) {
            statusView!!.text = text
        }
    }

    /**
     * @see BarcodeView.pause
     */
    fun pause() {
        barcodeView!!.pause()
    }

    /**
     * @see BarcodeView.pauseAndWait
     */
    fun pauseAndWait() {
        barcodeView!!.pauseAndWait()
    }

    /**
     * @see BarcodeView.resume
     */
    fun resume() {
        barcodeView!!.resume()
    }

    fun getBarcodeView(): BarcodeView {
        return findViewById(R.id.zxing_barcode_surface)
    }

    /**
     * @see BarcodeView.decodeSingle
     */
    fun decodeSingle(callback: BarcodeCallback) {
        barcodeView!!.decodeSingle(WrappedCallback(callback))
    }

    /**
     * @see BarcodeView.decodeContinuous
     */
    fun decodeContinuous(callback: BarcodeCallback) {
        barcodeView!!.decodeContinuous(WrappedCallback(callback))
    }

    /**
     * Turn on the device's flashlight.
     */
    fun setTorchOn() {
        barcodeView!!.setTorch(true)

        if (torchListener != null) {
            torchListener!!.onTorchOn()
        }
    }

    /**
     * Turn off the device's flashlight.
     */
    fun setTorchOff() {
        barcodeView!!.setTorch(false)

        if (torchListener != null) {
            torchListener!!.onTorchOff()
        }
    }

    /**
     * Changes the settings for Camera.
     * Must be called after [.resume].
     *
     * @param callback [CameraParametersCallback]
     */
    fun changeCameraParameters(callback: CameraParametersCallback?) {
        barcodeView!!.changeCameraParameters(callback)
    }

    /**
     * Handles focus, camera, volume up and volume down keys.
     *
     * Note that this view is not usually focused, so the Activity should call this directly.
     */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_FOCUS, KeyEvent.KEYCODE_CAMERA ->                 // Handle these events so they don't launch the Camera app
                return true

            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                setTorchOff()
                return true
            }

            KeyEvent.KEYCODE_VOLUME_UP -> {
                setTorchOn()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    fun setTorchListener(listener: TorchListener?) {
        this.torchListener = listener
    }

    /**
     * The Listener to torch/fflashlight events (turn on, turn off).
     */
    interface TorchListener {
        fun onTorchOn()

        fun onTorchOff()
    }
}
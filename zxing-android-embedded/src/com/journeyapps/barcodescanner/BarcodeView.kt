package com.journeyapps.barcodescanner

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import com.google.zxing.DecodeHintType
import com.google.zxing.ResultPoint
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.Util.validateMainThread

/**
 * A view for scanning barcodes.
 *
 *
 * Two methods MUST be called to manage the state:
 * 1. resume() - initialize the camera and start the preview. Call from the Activity's onResume().
 * 2. pause() - stop the preview and release any resources. Call from the Activity's onPause().
 *
 *
 * Start decoding with decodeSingle() or decodeContinuous(). Stop decoding with stopDecoding().
 *
 * @see CameraPreview for more details on the preview lifecycle.
 */
class BarcodeView : CameraPreview {
    private enum class DecodeMode {
        NONE,
        SINGLE,
        CONTINUOUS
    }

    private var decodeMode = DecodeMode.NONE
    private var callback: BarcodeCallback? = null
    private var decoderThread: DecoderThread? = null

    private var decoderFactory: DecoderFactory? = null


    private var resultHandler: Handler? = null

    private val resultCallback = Handler.Callback { message ->
        if (message.what == R.id.zxing_decode_succeeded) {
            val result = message.obj as BarcodeResult

            if (result != null) {
                if (callback != null && decodeMode != DecodeMode.NONE) {
                    callback!!.barcodeResult(result)
                    if (decodeMode == DecodeMode.SINGLE) {
                        stopDecoding()
                    }
                }
            }
            return@Callback true
        } else if (message.what == R.id.zxing_decode_failed) {
            // Failed. Next preview is automatically tried.
            return@Callback true
        } else if (message.what == R.id.zxing_possible_result_points) {
            val resultPoints = message.obj as List<ResultPoint>
            if (callback != null && decodeMode != DecodeMode.NONE) {
                callback!!.possibleResultPoints(resultPoints)
            }
            return@Callback true
        }
        false
    }


    constructor(context: Context) : super(context) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        initialize()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        initialize()
    }

    private fun initialize() {
        decoderFactory = DefaultDecoderFactory()
        resultHandler = Handler(resultCallback)
    }

    /**
     * Set the DecoderFactory to use. Use this to specify the formats to decode.
     *
     *
     * Call this from UI thread only.
     *
     * @param decoderFactory the DecoderFactory creating Decoders.
     * @see DefaultDecoderFactory
     */
    fun setDecoderFactory(decoderFactory: DecoderFactory?) {
        validateMainThread()

        this.decoderFactory = decoderFactory
        if (this.decoderThread != null) {
            decoderThread!!.decoder = createDecoder()!!
        }
    }

    private fun createDecoder(): Decoder? {
        if (decoderFactory == null) {
            decoderFactory = createDefaultDecoderFactory()
        }
        val callback = DecoderResultPointCallback()
        val hints: MutableMap<DecodeHintType?, Any?> = HashMap()
        hints[DecodeHintType.NEED_RESULT_POINT_CALLBACK] = callback
        val decoder = decoderFactory!!.createDecoder(hints)
        callback.decoder = decoder
        return decoder
    }

    /**
     * @return the current DecoderFactory in use.
     */
    fun getDecoderFactory(): DecoderFactory? {
        return decoderFactory
    }

    /**
     * Decode a single barcode, then stop decoding.
     *
     *
     * The callback will only be called on the UI thread.
     *
     * @param callback called with the barcode result, as well as possible ResultPoints
     */
    fun decodeSingle(callback: BarcodeCallback?) {
        this.decodeMode = DecodeMode.SINGLE
        this.callback = callback
        startDecoderThread()
    }

    /**
     * Continuously decode barcodes. The same barcode may be returned multiple times per second.
     *
     *
     * The callback will only be called on the UI thread.
     *
     * @param callback called with the barcode result, as well as possible ResultPoints
     */
    fun decodeContinuous(callback: BarcodeCallback?) {
        this.decodeMode = DecodeMode.CONTINUOUS
        this.callback = callback
        startDecoderThread()
    }

    /**
     * Stop decoding, but do not stop the preview.
     */
    fun stopDecoding() {
        this.decodeMode = DecodeMode.NONE
        this.callback = null
        stopDecoderThread()
    }

    protected fun createDefaultDecoderFactory(): DecoderFactory {
        return DefaultDecoderFactory()
    }

    private fun startDecoderThread() {
        stopDecoderThread() // To be safe

        if (decodeMode != DecodeMode.NONE && isPreviewActive) {
            // We only start the thread if both:
            // 1. decoding was requested
            // 2. the preview is active
            decoderThread = DecoderThread(cameraInstance, createDecoder()!!, resultHandler)
            decoderThread!!.cropRect = previewFramingRect
            decoderThread!!.start()
        }
    }

    override fun previewStarted() {
        super.previewStarted()

        startDecoderThread()
    }

    private fun stopDecoderThread() {
        if (decoderThread != null) {
            decoderThread!!.stop()
            decoderThread = null
        }
    }

    /**
     * Stops the live preview and decoding.
     *
     *
     * Call from the Activity's onPause() method.
     */
    override fun pause() {
        stopDecoderThread()
        super.pause()
    }
}
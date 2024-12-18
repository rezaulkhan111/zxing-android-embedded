package com.journeyapps.barcodescanner.camera

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.SurfaceHolder
import com.google.zxing.client.android.R
import com.journeyapps.barcodescanner.Size
import com.journeyapps.barcodescanner.Util.validateMainThread
import com.journeyapps.barcodescanner.camera.CameraInstance

/**
 * Manage a camera instance using a background thread.
 *
 * All methods must be called from the main thread.
 */
class CameraInstance {
    /**
     *
     * @return the CameraThread used to manage the camera
     */
    protected var cameraThread: CameraThread? = null
        private set

    /**
     *
     * @return the surface om which the preview is displayed
     */
    protected var surface: CameraSurface? = null

    /**
     * Returns the CameraManager used to control the camera.
     *
     * The CameraManager is not thread-safe, and must only be used from the CameraThread.
     *
     * @return the CameraManager used
     */
    protected var cameraManager: CameraManager
        private set
    private var readyHandler: Handler? = null
    var displayConfiguration: DisplayConfiguration? = null
        set(configuration) {
            field = configuration
            cameraManager.displayConfiguration = configuration
        }
    var isOpen: Boolean = false
        private set
    var isCameraClosed: Boolean = true
        private set
    private var mainHandler: Handler? = null

    private var cameraSettings = CameraSettings()

    /**
     * Construct a new CameraInstance.
     *
     * A new CameraManager is created.
     *
     * @param context the Android Context
     */
    constructor(context: Context?) {
        validateMainThread()

        this.cameraThread = CameraThread.getInstance()
        this.cameraManager = CameraManager(context)
        cameraManager.cameraSettings = cameraSettings
        this.mainHandler = Handler()
    }

    /**
     * Construct a new CameraInstance with a specific CameraManager.
     *
     * @param cameraManager the CameraManager to use
     */
    constructor(cameraManager: CameraManager) {
        validateMainThread()

        this.cameraManager = cameraManager
    }

    fun setReadyHandler(readyHandler: Handler?) {
        this.readyHandler = readyHandler
    }

    fun setSurfaceHolder(surfaceHolder: SurfaceHolder?) {
        surface = CameraSurface(surfaceHolder)
    }

    fun getCameraSettings(): CameraSettings {
        return cameraSettings
    }

    /**
     * This only has an effect if the camera is not opened yet.
     *
     * @param cameraSettings the new camera settings
     */
    fun setCameraSettings(cameraSettings: CameraSettings) {
        if (!isOpen) {
            this.cameraSettings = cameraSettings
            cameraManager.cameraSettings = cameraSettings
        }
    }

    private val previewSize: Size?
        /**
         * Actual preview size in current rotation. null if not determined yet.
         *
         * @return preview size
         */
        get() = cameraManager.previewSize

    val cameraRotation: Int
        /**
         *
         * @return the camera rotation relative to display rotation, in degrees. Typically 0 if the
         * display is in landscape orientation.
         */
        get() = cameraManager.cameraRotation

    fun open() {
        validateMainThread()

        isOpen = true
        isCameraClosed = false

        cameraThread!!.incrementAndEnqueue(opener)
    }

    fun configureCamera() {
        validateMainThread()
        validateOpen()

        cameraThread!!.enqueue(configure)
    }

    fun startPreview() {
        validateMainThread()
        validateOpen()

        cameraThread!!.enqueue(previewStarter)
    }

    fun setTorch(on: Boolean) {
        validateMainThread()

        if (isOpen) {
            cameraThread!!.enqueue { cameraManager.setTorch(on) }
        }
    }

    /**
     * Changes the settings for Camera.
     *
     * @param callback [CameraParametersCallback]
     */
    fun changeCameraParameters(callback: CameraParametersCallback?) {
        validateMainThread()

        if (isOpen) {
            cameraThread!!.enqueue { cameraManager.changeCameraParameters(callback) }
        }
    }

    fun close() {
        validateMainThread()

        if (isOpen) {
            cameraThread!!.enqueue(closer)
        } else {
            isCameraClosed = true
        }

        isOpen = false
    }

    fun requestPreview(callback: PreviewCallback?) {
        mainHandler!!.post {
            if (!isOpen) {
                Log.d(
                    TAG,
                    "Camera is closed, not requesting preview"
                )
                return@post
            }
            cameraThread!!.enqueue { cameraManager.requestPreviewFrame(callback) }
        }
    }

    private fun validateOpen() {
        check(isOpen) { "CameraInstance is not open" }
    }

    private val opener = Runnable {
        try {
            Log.d(TAG, "Opening camera")
            cameraManager.open()
        } catch (e: Exception) {
            notifyError(e)
            Log.e(TAG, "Failed to open camera", e)
        }
    }

    private val configure: Runnable = object : Runnable {
        override fun run() {
            try {
                Log.d(TAG, "Configuring camera")
                cameraManager.configure()
                if (readyHandler != null) {
                    readyHandler!!.obtainMessage(R.id.zxing_prewiew_size_ready, this.previewSize)
                        .sendToTarget()
                }
            } catch (e: Exception) {
                notifyError(e)
                Log.e(TAG, "Failed to configure camera", e)
            }
        }
    }

    private val previewStarter = Runnable {
        try {
            Log.d(TAG, "Starting preview")
            cameraManager.setPreviewDisplay(surface)
            cameraManager.startPreview()
        } catch (e: Exception) {
            notifyError(e)
            Log.e(TAG, "Failed to start preview", e)
        }
    }

    private val closer: Runnable = object : Runnable {
        override fun run() {
            try {
                Log.d(TAG, "Closing camera")
                cameraManager.stopPreview()
                cameraManager.close()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to close camera", e)
            }

            this.isCameraClosed = true

            readyHandler!!.sendEmptyMessage(R.id.zxing_camera_closed)

            cameraThread!!.decrementInstances()
        }
    }

    private fun notifyError(error: Exception) {
        if (readyHandler != null) {
            readyHandler!!.obtainMessage(R.id.zxing_camera_error, error).sendToTarget()
        }
    }

    companion object {
        private val TAG: String = CameraInstance::class.java.simpleName
    }
}
package com.journeyapps.barcodescanner.camera

import com.google.zxing.client.android.camera.open.OpenCameraInterface

/**
 *
 */
class CameraSettings {
    /**
     * Allows third party apps to specify the camera ID, rather than determine
     * it automatically based on available cameras and their orientation.
     *
     * @param requestedCameraId camera ID of the camera to use. A negative value means "no preference".
     */
    var requestedCameraId: Int = OpenCameraInterface.NO_REQUESTED_CAMERA

    /**
     * Default to false.
     *
     * Inverted means dark & light colors are inverted.
     *
     * @return true if scan is inverted
     */
    var isScanInverted: Boolean = false

    /**
     * Default to false.
     *
     * @return true if barcode scene mode is enabled
     */
    var isBarcodeSceneModeEnabled: Boolean = false

    /**
     * Default to false.
     *
     * If enabled, metering is performed to determine focus area.
     *
     * @return true if metering is enabled
     */
    var isMeteringEnabled: Boolean = false
    private var autoFocusEnabled = true
    private var continuousFocusEnabled = false

    /**
     * Default to false.
     *
     * @return true if exposure is enabled.
     */
    var isExposureEnabled: Boolean = false

    /**
     * Default to false.
     *
     * @return true if the torch is automatically controlled based on ambient light.
     */
    var isAutoTorchEnabled: Boolean = false

    /**
     * Default to FocusMode.AUTO.
     *
     * @return value of selected focus mode
     */
    var focusMode: FocusMode? = FocusMode.AUTO

    enum class FocusMode {
        AUTO,
        CONTINUOUS,
        INFINITY,
        MACRO
    }

    /**
     * Default to true.
     *
     * @return true if auto-focus is enabled
     */
    fun isAutoFocusEnabled(): Boolean {
        return autoFocusEnabled
    }

    fun setAutoFocusEnabled(autoFocusEnabled: Boolean) {
        this.autoFocusEnabled = autoFocusEnabled

        focusMode = if (autoFocusEnabled && continuousFocusEnabled) {
            FocusMode.CONTINUOUS
        } else if (autoFocusEnabled) {
            FocusMode.AUTO
        } else {
            null
        }
    }

    /**
     * Default to false.
     *
     * @return true if continuous focus is enabled
     */
    fun isContinuousFocusEnabled(): Boolean {
        return continuousFocusEnabled
    }

    fun setContinuousFocusEnabled(continuousFocusEnabled: Boolean) {
        this.continuousFocusEnabled = continuousFocusEnabled

        focusMode = if (continuousFocusEnabled) {
            FocusMode.CONTINUOUS
        } else if (autoFocusEnabled) {
            FocusMode.AUTO
        } else {
            null
        }
    }
}
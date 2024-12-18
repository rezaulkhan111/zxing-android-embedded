package com.journeyapps.barcodescanner

import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.google.zxing.client.android.R

/**
 *
 */
open class CaptureActivity : AppCompatActivity(), BSCallBack {
    private var capture: CaptureManager? = null
    private var barcodeScannerView: DecoratedBarcodeView? = null

    private var mCustomDataModel: CustomDataModel? = null

    private var ivOrderProduct: ImageView? = null
    private var tvOrderProductName: TextView? = null
    private var tvOrderProductWeight: TextView? = null
    private var tvToolbarTitle: TextView? = null

    private var bsBarcodeScan: BarcodeScanBSF? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        barcodeScannerView = initializeContent()
        initUi()

        capture = CaptureManager(this, barcodeScannerView!!)
        capture!!.initializeFromIntent(intent, savedInstanceState)
        capture!!.decode()

        val customDataValue: String

        val mExtra = intent.extras
        if (mExtra != null) {
            customDataValue = mExtra.getString(Util.dataTransfer_Key)

            if (customDataValue != null && customDataValue !== "") {
                mCustomDataModel = Gson().fromJson(customDataValue, CustomDataModel::class.java)
                initUiFunction()
                bsBarcodeScan = BarcodeScanBSF(mCustomDataModel, this)
            }
        }
    }

    private fun initUi() {
        tvToolbarTitle = findViewById(R.id.title)
        val ivClose = findViewById<ImageView>(R.id.back)

        ivOrderProduct = findViewById(R.id.ivOrderProduct)
        tvOrderProductName = findViewById(R.id.tvOrderProductName)
        tvOrderProductWeight = findViewById(R.id.tvOrderProductWeight)
        val tvCantScanBarcode = findViewById<TextView>(R.id.tvCantScanBarcode)

        tvCantScanBarcode.setOnClickListener { v: View? ->
            bottomSheetOpenClose(
                bsBarcodeScan!!
            )
        }

        ivClose.setOnClickListener { v: View? ->
            finish()
        }
    }

    private fun initUiFunction() {
        tvToolbarTitle!!.text = mCustomDataModel!!.toolbarTitle
        ivOrderProduct!!.setImageBitmap(mCustomDataModel!!.productImage)
        tvOrderProductName!!.text = mCustomDataModel!!.productName
        tvOrderProductWeight!!.text = mCustomDataModel!!.productQuantity
    }

    private fun bottomSheetOpenClose(bottomSheetObj: BottomSheetDialogFragment) {
        if (!bottomSheetObj.isVisible) {
            bottomSheetObj.show(supportFragmentManager, "BarcodeScan")
        } else {
            bottomSheetObj.dismiss()
        }
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected open fun initializeContent(): DecoratedBarcodeView? {
        setContentView(R.layout.zxing_capture)
        return findViewById<View>(R.id.zxing_barcode_scanner) as DecoratedBarcodeView
    }

    override fun onResume() {
        super.onResume()
        capture!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        capture!!.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        capture!!.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        capture!!.onSaveInstanceState(outState)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return barcodeScannerView!!.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event)
    }

    override fun onCloseScanActivity() {
        finish()
    }
}
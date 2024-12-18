package com.journeyapps.barcodescanner;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.zxing.client.android.R;

/**
 *
 */
public class CaptureActivity extends AppCompatActivity implements BSCallBack {
    private CaptureManager capture;
    private DecoratedBarcodeView barcodeScannerView;

    private CustomDataModel mCustomDataModel;

    private ImageView ivOrderProduct;
    private TextView tvOrderProductName;
    private TextView tvOrderProductWeight;
    private TextView tvToolbarTitle;

    private BarcodeScanBSF bsBarcodeScan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        barcodeScannerView = initializeContent();
        initUi();

        capture = new CaptureManager(this, barcodeScannerView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();

        String customDataValue;

        Bundle mExtra = getIntent().getExtras();
        if (mExtra != null) {
            customDataValue = mExtra.getString(Util.dataTransfer_Key);

            if (customDataValue != null && customDataValue != "") {
                mCustomDataModel = new Gson().fromJson(customDataValue, CustomDataModel.class);
                initUiFunction();
                bsBarcodeScan = new BarcodeScanBSF(mCustomDataModel, this);
            }
        }
    }

    private void initUi() {
        tvToolbarTitle = findViewById(R.id.title);
        ImageView ivClose = findViewById(R.id.back);

        ivOrderProduct = findViewById(R.id.ivOrderProduct);
        tvOrderProductName = findViewById(R.id.tvOrderProductName);
        tvOrderProductWeight = findViewById(R.id.tvOrderProductWeight);
        TextView tvCantScanBarcode = findViewById(R.id.tvCantScanBarcode);

        tvCantScanBarcode.setOnClickListener(v -> {
            bottomSheetOpenClose(bsBarcodeScan);
        });

        ivClose.setOnClickListener(v -> {
            finish();
        });
    }

    private void initUiFunction() {
        tvToolbarTitle.setText(mCustomDataModel.toolbarTitle);
        ivOrderProduct.setImageBitmap(mCustomDataModel.productImage);
        tvOrderProductName.setText(mCustomDataModel.productName);
        tvOrderProductWeight.setText(mCustomDataModel.productQuantity);
    }

    private void bottomSheetOpenClose(BottomSheetDialogFragment bottomSheetObj) {
        if (!bottomSheetObj.isVisible()) {
            bottomSheetObj.show(getSupportFragmentManager(), "BarcodeScan");
        } else {
            bottomSheetObj.dismiss();
        }
    }

    /**
     * Override to use a different layout.
     *
     * @return the DecoratedBarcodeView
     */
    protected DecoratedBarcodeView initializeContent() {
        setContentView(R.layout.zxing_capture);
        return (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);
    }

    @Override
    protected void onResume() {
        super.onResume();
        capture.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return barcodeScannerView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onCloseScanActivity() {
        finish();
    }
}
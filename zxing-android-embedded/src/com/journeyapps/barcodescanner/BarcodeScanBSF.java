package com.journeyapps.barcodescanner;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.zxing.client.android.R;

public class BarcodeScanBSF extends BottomSheetDialogFragment {

    private TextView tvBarcodeMessage1;
    private TextView tvBarcodeMessage2;
    private Button btnItemIsCorrect;

    private CustomDataModel mCDataModel;

    public BarcodeScanBSF(CustomDataModel mCustomDataModel) {
        mCDataModel = mCustomDataModel;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View vRef = inflater.inflate(R.layout.zxing_bottom_sheet_scan_product, container, false);
        tvBarcodeMessage1 = vRef.findViewById(R.id.tvBarcodeMessage1);
        tvBarcodeMessage2 = vRef.findViewById(R.id.tvBarcodeMessage2);
        btnItemIsCorrect = vRef.findViewById(R.id.btnItemIsCorrect);

        tvBarcodeMessage1.setText(mCDataModel.bsMessage1);
        tvBarcodeMessage2.setText(mCDataModel.bsMessage2);

        btnItemIsCorrect.setText(mCDataModel.buttonText);
//        btnItemIsCorrect.setBa(mCDataModel.buttonColor);

        btnItemIsCorrect.setOnClickListener(v -> {
            dismiss();
        });

        return vRef;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initView();
    }

    void initView() {

    }
}

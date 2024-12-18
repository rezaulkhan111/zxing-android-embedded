package com.journeyapps.barcodescanner

import android.graphics.Bitmap

class CustomDataModel {
    @JvmField
    var toolbarTitle: String? = null

    @JvmField
    var productImage: Bitmap? = null

    @JvmField
    var productName: String? = null

    @JvmField
    var productQuantity: String? = null

    @JvmField
    var bsMessage1: String? = null

    @JvmField
    var bsMessage2: String? = null

    @JvmField
    var buttonText: String? = null
    @JvmField
    var buttonColor: String? = null
    @JvmField
    var buttonTextColor: String? = null
}
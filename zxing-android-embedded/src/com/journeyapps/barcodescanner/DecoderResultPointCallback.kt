package com.journeyapps.barcodescanner

import com.google.zxing.ResultPoint
import com.google.zxing.ResultPointCallback

/**
 * ResultPointCallback delegating the ResultPoints to a decoder.
 */
class DecoderResultPointCallback : ResultPointCallback {
    var decoder: Decoder? = null

    constructor(decoder: Decoder?) {
        this.decoder = decoder
    }

    constructor()

    override fun foundPossibleResultPoint(point: ResultPoint) {
        if (decoder != null) {
            decoder!!.foundPossibleResultPoint(point)
        }
    }
}
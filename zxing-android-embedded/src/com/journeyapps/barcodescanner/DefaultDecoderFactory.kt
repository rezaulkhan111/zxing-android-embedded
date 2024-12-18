package com.journeyapps.barcodescanner

import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import java.util.EnumMap

/**
 * DecoderFactory that creates a MultiFormatReader with specified hints.
 */
class DefaultDecoderFactory : DecoderFactory {
    private var decodeFormats: Collection<BarcodeFormat>? = null
    private var hints: Map<DecodeHintType, *>? = null
    private var characterSet: String? = null
    private var scanType = 0

    constructor()


    constructor(decodeFormats: Collection<BarcodeFormat>?) {
        this.decodeFormats = decodeFormats
    }

    constructor(
        decodeFormats: Collection<BarcodeFormat>?,
        hints: Map<DecodeHintType, *>?,
        characterSet: String?,
        scanType: Int
    ) {
        this.decodeFormats = decodeFormats
        this.hints = hints
        this.characterSet = characterSet
        this.scanType = scanType
    }

    override fun createDecoder(baseHints: Map<DecodeHintType, *>): Decoder? {
        val hints: MutableMap<DecodeHintType, Any?> = EnumMap(
            DecodeHintType::class.java
        )

        hints.putAll(baseHints)

        if (this.hints != null) {
            hints.putAll(this.hints!!)
        }

        if (this.decodeFormats != null) {
            hints[DecodeHintType.POSSIBLE_FORMATS] = decodeFormats
        }

        if (characterSet != null) {
            hints[DecodeHintType.CHARACTER_SET] = characterSet
        }

        val reader = MultiFormatReader()
        reader.setHints(hints)

        return when (scanType) {
            0 -> Decoder(reader)
            1 -> InvertedDecoder(reader)
            2 -> MixedDecoder(reader)
            else -> Decoder(reader)
        }
    }
}

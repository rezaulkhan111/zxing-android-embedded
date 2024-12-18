package com.journeyapps.barcodescanner

import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class ScanContract : ActivityResultContract<ScanOptions, ScanIntentResult>() {
    override fun createIntent(context: Context, input: ScanOptions): Intent {
        return input.createScanIntent(context)
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ScanIntentResult {
        return ScanIntentResult.parseActivityResult(resultCode, intent)
    }
}
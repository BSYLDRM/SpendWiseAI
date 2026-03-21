package com.example.spendwiseai.presentation.scan

import android.graphics.Bitmap
import com.example.spendwiseai.domain.model.ParsedTransaction

data class ScanReceiptUiState(
    val capturedBitmap: Bitmap? = null,
    val isParsing: Boolean = false,
    val preview: ParsedTransaction? = null,
    val errorMessage: String? = null,
    val savedTransactionId: Long? = null
)


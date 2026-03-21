package com.example.spendwiseai.domain.ai

import android.graphics.Bitmap
import com.example.spendwiseai.domain.model.ParsedTransaction

interface ReceiptVisionParser {
    suspend fun parseReceipt(bitmap: Bitmap): ParsedTransaction
}


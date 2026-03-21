package com.example.spendwiseai.ui.screens

import android.Manifest
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import com.example.spendwiseai.domain.model.TransactionType
import com.example.spendwiseai.presentation.scan.ScanReceiptViewModel
import com.example.spendwiseai.ui.theme.NeonGreen
import com.example.spendwiseai.ui.theme.SoftCoralRed
import com.example.spendwiseai.core.LocaleManager
import android.widget.Toast
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.spendwiseai.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanReceiptScreen(
    viewModel: ScanReceiptViewModel,
    onSavedNavigate: (TransactionType) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val selectedCurrency = LocaleManager.getCurrency(context)

    val preview = state.preview
    val shouldShowPreviewSheet = preview != null && !state.isParsing

    val accent = when (preview?.type) {
        TransactionType.INCOME -> NeonGreen
        else -> SoftCoralRed
    }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }

    val requestPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            hasCameraPermission = granted
        }

    val takePhotoLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            if (bitmap != null) {
                viewModel.onPhotoCaptured(bitmap)
            } else {
                // no-op: user cancelled
            }
        }

    LaunchedEffect(state.savedTransactionId) {
        if (state.savedTransactionId != null && preview != null) {
            onSavedNavigate(preview.type)
        }
    }

    LaunchedEffect(state.errorMessage) {
        val msg = state.errorMessage
        if (!msg.isNullOrBlank()) {
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RowHeader(onBack = onBack, title = stringResource(id = R.string.scan_receipt))

            state.capturedBitmap?.let { bitmap ->
                val painter = remember(bitmap) { BitmapPainter(bitmap.asImageBitmap()) }
                Image(
                    painter = painter,
                    contentDescription = stringResource(id = R.string.receipt_preview),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp)
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(18.dp))
                )
            }

            if (preview == null && !state.isParsing) {
                OutlinedButton(
                    onClick = {
                        if (!hasCameraPermission) {
                            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
                        } else {
                            takePhotoLauncher.launch(null)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CameraAlt,
                        contentDescription = stringResource(id = R.string.camera)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(stringResource(id = R.string.take_receipt_photo))
                }
            }
        }

        if (state.isParsing) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.45f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = stringResource(id = R.string.analyzing_receipt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }

    if (shouldShowPreviewSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.reset() },
            sheetState = sheetState
        ) {
            val p = preview ?: return@ModalBottomSheet

            Column(
                modifier = Modifier
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    stringResource(id = R.string.ai_parsed_preview),
                    style = MaterialTheme.typography.titleMedium
                )

                Text(
                    text = "Toplam: $selectedCurrency ${String.format("%.2f", p.amount)}",
                    color = accent,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(text = "Kategori: ${p.category}", style = MaterialTheme.typography.bodyMedium)

                Text(
                    text = "Tip: ${
                        if (p.type == TransactionType.INCOME) stringResource(id = R.string.income)
                        else stringResource(id = R.string.expense)
                    }",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = { viewModel.confirmSave(p) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.confirm_and_save))
                }
            }
        }
    }
}

@Composable
private fun RowHeader(
    onBack: () -> Unit,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
        }
        Text(title, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.width(48.dp))
    }
}


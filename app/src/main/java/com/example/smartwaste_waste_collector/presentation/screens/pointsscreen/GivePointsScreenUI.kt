package com.example.smartwaste_waste_collector.presentation.screens.pointsscreen

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.util.Size
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.smartwaste_waste_collector.presentation.viewmodels.userpointviewmodel.UserPointViewModel
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GivePointsScreenUI(
    viewModel: UserPointViewModel = hiltViewModel<UserPointViewModel>(),
    navController: NavController
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var points by remember { mutableStateOf("") }
    var showQrOptions by remember { mutableStateOf(false) }
    var showQrScanner by remember { mutableStateOf(false) }
    var scannedUserId by remember { mutableStateOf<String?>(null) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { imageUri ->
                try {
                    val inputStream = context.contentResolver.openInputStream(imageUri)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    val image = InputImage.fromBitmap(bitmap, 0)

                    val scanner = BarcodeScanning.getClient()
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            barcodes.firstOrNull()?.rawValue?.let { value ->
                                scannedUserId = value
                                viewModel.onQrScanned(value)
                                Log.d("QRScan", "Scanned from image: $value")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("QRScan", "Failed to scan image", exception)
                        }
                } catch (e: Exception) {
                    Log.e("QRScan", "Error processing image", e)
                }
            }
        }
    )

    LaunchedEffect(state) {
        if (state.success != null) {
            showSuccessDialog = true
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Award Points",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.clip(RoundedCornerShape(20.dp))
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Redeem,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Reward User",
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Scan QR code and award points to users for their contributions",
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Scan QR Code",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (scannedUserId != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "QR Code Scanned!",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "User ID: ${scannedUserId?.take(12)}...",
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        scannedUserId = null
                                        viewModel.resetState()
                                    }
                                ) {
                                    Text("Scan Again")
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showQrScanner = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.QrCodeScanner,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Scan QR",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp),
                                shape = RoundedCornerShape(16.dp),

                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.PhotoLibrary,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "From Photos",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Choose to scan QR code with camera or select an image from your gallery",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Award Points",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = points,
                        onValueChange = { points = it },
                        label = { Text("Points to Award") },
                        placeholder = { Text("Enter points (e.g., 10)") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        enabled = scannedUserId != null,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Quick Select:",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        listOf("5", "10", "15", "20").forEach { point ->
                            FilterChip(
                                onClick = { if (scannedUserId != null) points = point },
                                label = {
                                    Text(
                                        "$point pts",
                                        fontWeight = FontWeight.Medium
                                    )
                                },
                                selected = points == point,
                                enabled = scannedUserId != null,
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }

            Button(
                onClick = {
                    scannedUserId?.let { userId ->
                        viewModel.onQrScanned(userId)
                        viewModel.givePoints(points)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                shape = RoundedCornerShape(20.dp),
                enabled = scannedUserId != null && points.isNotBlank() && !state.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(28.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        "Processing...",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Icon(
                        Icons.Default.Redeem,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        if (points.isNotBlank()) "Award $points Points" else "Award Points",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (state.error.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = state.error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showQrScanner) {
        Dialog(onDismissRequest = { showQrScanner = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(650.dp),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Scan QR Code",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        IconButton(onClick = { showQrScanner = false }) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(20.dp))
                    ) {
                        QRCodeScanner { userId ->
                            scannedUserId = userId
                            viewModel.onQrScanned(userId)
                            showQrScanner = false
                        }

                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(280.dp)
                                    .border(
                                        4.dp,
                                        MaterialTheme.colorScheme.primary,
                                        RoundedCornerShape(20.dp)
                                    )
                            )
                        }
                    }

                    Text(
                        "Position the QR code within the frame to scan",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(
            points = state.success ?: points,
            onDismiss = {
                showSuccessDialog = false
                points = ""
                scannedUserId = null
                viewModel.resetState()
            }
        )
    }
}

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun QRCodeScanner(onResult: (String) -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasCameraPermission = granted
        }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }

    if (hasCameraPermission) {
        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }

                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()

                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }

                        val barcodeScanner = BarcodeScanning.getClient()

                        val imageAnalyzer = ImageAnalysis.Builder()
                            .setTargetResolution(Size(1280, 720))
                            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                            .build()
                            .also {
                                it.setAnalyzer(cameraExecutor) { imageProxy ->
                                    val mediaImage = imageProxy.image
                                    if (mediaImage != null) {
                                        val image = InputImage.fromMediaImage(
                                            mediaImage,
                                            imageProxy.imageInfo.rotationDegrees
                                        )
                                        barcodeScanner.process(image)
                                            .addOnSuccessListener { barcodes ->
                                                barcodes.firstOrNull()?.rawValue?.let { value ->
                                                    Log.d("QRScan", "Scanned: $value")
                                                    onResult(value)
                                                }
                                            }
                                            .addOnFailureListener { exception ->
                                                Log.e("QRScan", "Barcode scanning failed", exception)
                                            }
                                            .addOnCompleteListener {
                                                imageProxy.close()
                                            }
                                    } else {
                                        imageProxy.close()
                                    }
                                }
                            }

                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            cameraSelector,
                            preview,
                            imageAnalyzer
                        )
                    } catch (e: Exception) {
                        Log.e("CameraX", "Camera binding failed", e)
                    }
                }, ContextCompat.getMainExecutor(ctx))

                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Camera permission is required",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Button(
                    onClick = {
                        permissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                ) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
private fun SuccessDialog(
    points: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(
                            MaterialTheme.colorScheme.primaryContainer,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(50.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Points Awarded!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Successfully awarded $points points to the user",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
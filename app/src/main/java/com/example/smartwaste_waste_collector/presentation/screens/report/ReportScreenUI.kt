package com.example.smartwaste_waste_collector.presentation.screens.report

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.rememberAsyncImagePainter
import com.example.smartwaste_waste_collector.data.models.ReportModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.reportviewmodel.ReportViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreenUI(viewModel: ReportViewModel = hiltViewModel<ReportViewModel>()) {
    val getReportsState by viewModel.getReportsState.collectAsState()
    val updateReportState by viewModel.updateReportState.collectAsState(initial = null)
    val snackbarHostState = remember { SnackbarHostState() }
    val clearKey = remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        viewModel.getAllReports()
    }

    LaunchedEffect(updateReportState) {
        updateReportState?.let { state ->
            if (state.success != null) {
                snackbarHostState.showSnackbar("Report updated successfully.")
                viewModel.clearUpdateState()
                clearKey.value++
            } else if (state.error.isNotEmpty()) {
                snackbarHostState.showSnackbar("Update failed: ${state.error}")
                viewModel.clearUpdateState()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Waste Collection Reports",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        Text(
                            text = "Manage and update collection status",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                modifier = Modifier.background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            val reports = getReportsState.success

            when {
                getReportsState.isLoading -> LoadingState()
                getReportsState.error.isNotEmpty() -> ErrorState(
                    getReportsState.error,
                    onRetry = { viewModel.getAllReports() })
                reports != null -> {
                    if (reports.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(reports) { report ->
                                EnhancedReportCard(
                                    report = report,
                                    viewModel = viewModel,
                                    clearKey = clearKey.value
                                )
                            }
                        }
                    }
                }
            }

            if (updateReportState?.isLoading == true) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(elevation = CardDefaults.cardElevation(8.dp), shape = RoundedCornerShape(16.dp)) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Updating Report...")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EnhancedReportCard(
    report: ReportModel,
    viewModel: ReportViewModel,
    clearKey: Int
) {
    var isExpanded by remember { mutableStateOf(false) }
    var showStatusUpdateDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val newAttachments = rememberSaveable(report.reportId, clearKey, saver = listSaver(
        save = { it.toList() },
        restore = { it.toMutableStateList() }
    )) { mutableStateListOf<String>() }

    val rotationAngle by animateFloatAsState(targetValue = if (isExpanded) 180f else 0f, label = "rotation")

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris: List<Uri> ->
            uris.forEach { uri ->
                val savedUri = saveImageToInternalStorage(context, uri)
                if (savedUri != null) {
                    newAttachments.add(savedUri.toString())
                }
            }
        }
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF98756A))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Report #${report.reportId}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = " Area Name : ${report.areaName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                     Text(
                         text = "Date : ${report.reportDate}",
                         style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.onSurfaceVariant
                     )
                }
                Spacer(Modifier.width(8.dp))
                StatusChip(report.status)
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand or collapse card",
                    modifier = Modifier.padding(start = 8.dp).rotate(rotationAngle)
                )
            }

            if (!isExpanded) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column {
                    Divider(Modifier.padding(vertical = 12.dp))
                    Text(
                        text = "Description : ${report.description}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(16.dp))

                    if (report.attachments.isNotEmpty()) {
                        Text(
                            text = "Current Photos (${report.attachments.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(report.attachments) { imageUrl ->
                                AttachmentImage(imageUrl)
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    if (newAttachments.isNotEmpty()) {
                        Text(
                            text = "New Photos to Upload (${newAttachments.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(newAttachments) { uriString ->
                                AttachmentImage(
                                    imageUrl = uriString,
                                    isNew = true,
                                    onRemove = { newAttachments.remove(uriString) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Add Photos")
                        }
                        Button(
                            onClick = { showStatusUpdateDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = report.status.lowercase() != "completed"
                        ) {
                            Icon(Icons.Default.Update, null, Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Update")
                        }
                    }
                }
            }
        }
    }

    if (showStatusUpdateDialog) {
        val statuses = listOf("In Progress", "Completed")
        AlertDialog(
            onDismissRequest = { showStatusUpdateDialog = false },
            icon = { Icon(Icons.Default.Update, null) },
            title = { Text("Update Status", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select the new status for Report #${report.reportId}.")
                    Spacer(Modifier.height(16.dp))
                    statuses.forEach { statusOption ->
                        val isEnabled = report.status.lowercase() != statusOption.lowercase()
                        Button(
                            onClick = {
                                val allAttachments = report.attachments + newAttachments
                                viewModel.updateReport(report.reportId, statusOption, allAttachments)
                                showStatusUpdateDialog = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            enabled = isEnabled
                        ) {
                            Text(statusOption)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStatusUpdateDialog = false }) { Text("Cancel") }
            },
            shape = RoundedCornerShape(20.dp)
        )
    }
}


@Composable
fun StatusChip(status: String) {
    val (containerColor, contentColor) = when (status.lowercase()) {
        "pending" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
        "in progress" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        "completed" -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(50),
    ) {
        Text(
            text = status,
            color = contentColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun AttachmentImage(
    imageUrl: String,
    isNew: Boolean = false,
    onRemove: (() -> Unit)? = null
) {
    Box {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(model = imageUrl),
                contentDescription = if (isNew) "New attachment" else "Attachment",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
        }
        if (isNew && onRemove != null) {
            IconButton(
                onClick = onRemove,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 8.dp, y = (-8).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error)
                    .size(24.dp)
            ) {
                Icon(
                    Icons.Default.Close,
                    "Remove photo",
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}

private fun saveImageToInternalStorage(context: Context, sourceUri: Uri): Uri? {
    return try {
        val inputStream = context.contentResolver.openInputStream(sourceUri)
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir = File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "WasteCollector")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        val file = File(storageDir, "IMG_${timeStamp}_gallery.jpg")
        val outputStream = file.outputStream()
        inputStream?.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
        FileProvider.getUriForFile(
            context,
            context.packageName + ".provider",
            file
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(Modifier.size(48.dp), strokeWidth = 4.dp)
            Spacer(Modifier.height(16.dp))
            Text(
                "Loading Reports...",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ErrorState(error: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Filled.CloudOff,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Failed to load reports",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = onRetry) {
                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.Assignment,
                contentDescription = "No reports",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No Reports Found",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "New reports will appear here once they are available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
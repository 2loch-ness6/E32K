package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@Composable
fun FileScreen(viewModel: MarauderViewModel) {
    val fileList by viewModel.fileList.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SD CARD FILES",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.Green
            )
            IconButton(onClick = { viewModel.refreshFileList() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = Color.Green)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        
        // Download Progress Indicator
        downloadProgress?.let { progress ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF2A2A2A))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Downloading: ${progress.filename}",
                            color = Color.Cyan,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${progress.progressPercentage}%",
                            color = Color.Cyan,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LinearProgressIndicator(
                        progress = progress.progress,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = Color.Cyan
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${formatSize(progress.bytesDownloaded)} / ${formatSize(progress.totalBytes)}",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall
                    )
                    
                    progress.error?.let { error ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Error: $error",
                            color = Color.Red,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        // File List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fileList) { file ->
                FileCard(
                    filename = file.name,
                    size = file.size,
                    onDelete = { viewModel.deleteFile(file.name) },
                    onDownload = { viewModel.downloadFile(context, file.name) }
                )
            }
        }
    }
}

@Composable
fun FileCard(
    filename: String,
    size: Long,
    onDelete: () -> Unit,
    onDownload: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = filename, color = Color.White, style = MaterialTheme.typography.bodyLarge)
                Text(text = formatSize(size), color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            
            Row {
                IconButton(onClick = onDownload) {
                    Icon(Icons.Default.Download, contentDescription = "Download", tint = Color.Cyan)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

fun formatSize(size: Long): String {
    if (size <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (Math.log10(size.toDouble()) / Math.log10(1024.0)).toInt()
    return String.format("%.1f %s", size / Math.pow(1024.0, digitGroups.toDouble()), units[digitGroups])
}

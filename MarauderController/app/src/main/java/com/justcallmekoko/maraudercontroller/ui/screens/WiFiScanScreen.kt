package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcallmekoko.maraudercontroller.data.protocol.AccessPoint
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel
import com.justcallmekoko.maraudercontroller.ui.theme.NexusSuccess
import com.justcallmekoko.maraudercontroller.ui.theme.NexusWarning
import com.justcallmekoko.maraudercontroller.ui.theme.NexusError

@Composable
fun WiFiScanScreen(viewModel: MarauderViewModel) {
    val accessPoints by viewModel.accessPoints.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val isLiveScanning by viewModel.isLiveScanning.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Control Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Scan Button
            Button(
                onClick = { 
                    if (isScanning) viewModel.stopScan() 
                    else viewModel.startScanAp() 
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.Radar,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanning) "STOP" else "SCAN APs")
            }
            
            // Live Toggle
            FilledTonalButton(
                onClick = { viewModel.toggleLiveScan() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = if (isLiveScanning) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(
                    Icons.Default.Autorenew, 
                    contentDescription = null, 
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isLiveScanning) "LIVE ON" else "LIVE OFF")
            }

            // Refresh Icon Button
            IconButton(
                onClick = { viewModel.refreshAccessPoints() },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh List")
            }
        }
        
        // List Content
        if (accessPoints.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.WifiFind,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Access Points Found",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Start a scan to detect networks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(accessPoints) { index, ap ->
                    AccessPointCard(
                        index = index,
                        ap = ap,
                        onSelect = { viewModel.selectAccessPoint(index) },
                        onDeauth = { 
                            viewModel.startTargetedAttack(
                                channel = ap.channel,
                                apMac = ap.bssid,
                                stationMac = "FF:FF:FF:FF:FF:FF"
                            )
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccessPointCard(
    index: Int,
    ap: AccessPoint,
    onSelect: () -> Unit,
    onDeauth: () -> Unit
) {
    Card(
        onClick = onSelect,
        colors = CardDefaults.cardColors(
            containerColor = if (ap.selected) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        border = if (ap.selected) 
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) 
        else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Index Badge
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.size(24.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$index",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Main Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (ap.ssid.isBlank()) "<HIDDEN>" else ap.ssid,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (ap.ssid.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Tag, 
                        contentDescription = null, 
                        modifier = Modifier.size(10.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = ap.bssid,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CH: ${ap.channel}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Stats Column
            Column(horizontalAlignment = Alignment.End) {
                // RSSI Badge
                Surface(
                    color = getRssiColor(ap.rssi).copy(alpha = 0.1f),
                    contentColor = getRssiColor(ap.rssi),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${ap.rssi} dBm",
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                   // Deauth Button
                    IconButton(
                        onClick = onDeauth,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.Bolt,
                            contentDescription = "Deauth",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun getRssiColor(rssi: Int): Color {
    return when {
        rssi > -60 -> NexusSuccess
        rssi > -80 -> NexusWarning
        else -> NexusError
    }
}
package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcallmekoko.maraudercontroller.data.protocol.AccessPoint
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@Composable
fun WiFiScanScreen(viewModel: MarauderViewModel) {
    val accessPoints by viewModel.accessPoints.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    val currentScan by viewModel.currentScan.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { 
                    if (isScanning) viewModel.stopScan() 
                    else viewModel.startScanAp() 
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isScanning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = if (isScanning) Icons.Default.Stop else Icons.Default.WifiFind,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isScanning) "Stop Scan" else "Scan APs")
            }
            
            OutlinedButton(onClick = { viewModel.refreshAccessPoints() }) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Refresh List")
            }
        }
        
        // List Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "ID",
                modifier = Modifier.width(40.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "SSID",
                modifier = Modifier.weight(1f),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "RSSI",
                modifier = Modifier.width(50.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "CH",
                modifier = Modifier.width(40.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
            Text(
                text = "SEC",
                modifier = Modifier.width(50.dp),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
        
        if (accessPoints.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No Access Points Found",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(accessPoints) { index, ap ->
                    AccessPointItem(
                        index = index,
                        ap = ap,
                        onSelect = { viewModel.selectAccessPoint(index) }
                    )
                    Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                }
            }
        }
    }
}

@Composable
fun AccessPointItem(
    index: Int,
    ap: AccessPoint,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .background(if (ap.selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = ap.selected,
            onCheckedChange = { onSelect() },
            modifier = Modifier.size(32.dp)
        )
        
        Text(
            text = "$index",
            modifier = Modifier.width(32.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (ap.ssid.isBlank()) "<HIDDEN>" else ap.ssid,
                fontWeight = FontWeight.SemiBold,
                color = if (ap.ssid.isBlank()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = ap.bssid,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${ap.rssi}",
            modifier = Modifier.width(50.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = getRssiColor(ap.rssi)
        )
        
        Text(
            text = "${ap.channel}",
            modifier = Modifier.width(40.dp),
            fontSize = 12.sp
        )
        
        Text(
            text = ap.encryption.take(4),
            modifier = Modifier.width(50.dp),
            fontSize = 11.sp,
            color = if (ap.encryption.contains("OPEN", true)) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun getRssiColor(rssi: Int): Color {
    return when {
        rssi > -60 -> Color(0xFF4CAF50) // Green
        rssi > -80 -> Color(0xFFFFC107) // Amber
        else -> Color(0xFFF44336)       // Red
    }
}
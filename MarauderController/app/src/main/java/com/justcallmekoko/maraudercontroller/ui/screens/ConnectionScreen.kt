package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@Composable
fun ConnectionScreen(
    viewModel: MarauderViewModel
) {
    val connectionState by viewModel.connectionState.collectAsState()
    var devices by remember { mutableStateOf(viewModel.findDevices()) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Usb,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "ESP32 Marauder",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Connect your device via USB OTG",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (devices.isEmpty()) {
            Text(
                text = "No devices found",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(onClick = { devices = viewModel.findDevices() }) {
                Text("Scan for Devices")
            }
        } else {
            Text(
                text = "Found ${devices.size} device(s)",
                style = MaterialTheme.typography.titleMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            devices.forEachIndexed { index, driver ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = driver.device.deviceName,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Vendor ID: ${driver.device.vendorId}, Product ID: ${driver.device.productId}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Button(onClick = { viewModel.connect(index) }) {
                            Text("Connect")
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            TextButton(onClick = { devices = viewModel.findDevices() }) {
                Text("Refresh")
            }
        }
    }
}

@Composable
fun ConnectionDialog(
    viewModel: MarauderViewModel,
    onDismiss: () -> Unit
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connection") },
        text = {
            Column {
                when (connectionState) {
                    is com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager.ConnectionState.Connected -> {
                        Text("Device connected successfully")
                        Text(
                            text = (connectionState as com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager.ConnectionState.Connected).deviceName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    is com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager.ConnectionState.Connecting -> {
                        Text("Connecting to device...")
                        CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                    }
                    is com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager.ConnectionState.Disconnected -> {
                        Text("Not connected to any device")
                    }
                    is com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager.ConnectionState.Error -> {
                        Text(
                            text = "Connection error: ${(connectionState as com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager.ConnectionState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (isConnected) {
                TextButton(onClick = {
                    viewModel.disconnect()
                    onDismiss()
                }) {
                    Text("Disconnect")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

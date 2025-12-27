package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcallmekoko.maraudercontroller.data.preferences.PreferencesManager
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@Composable
fun SettingsScreen(viewModel: MarauderViewModel) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val themeMode by viewModel.themeModeFlow.collectAsState(initial = PreferencesManager.ThemeMode.SYSTEM)
    val autoConnect by viewModel.autoConnectFlow.collectAsState(initial = false)
    val gpsEnabled by viewModel.gpsEnabledFlow.collectAsState(initial = false)
    val bluetoothEnabled by viewModel.bluetoothEnabledFlow.collectAsState(initial = false)
    
    var showThemeDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // App Settings Section
        Text("App Settings", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Theme Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = when (themeMode) {
                                PreferencesManager.ThemeMode.LIGHT -> "Light"
                                PreferencesManager.ThemeMode.DARK -> "Dark"
                                PreferencesManager.ThemeMode.SYSTEM -> "System Default"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { showThemeDialog = true }) {
                        Text("Change")
                    }
                }
                
                Divider()
                
                // Auto Connect Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto Connect", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Automatically connect to last device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = autoConnect,
                        onCheckedChange = { viewModel.setAutoConnect(it) }
                    )
                }
                
                Divider()
                
                // GPS Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("GPS Integration", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Enable wardriving and location tracking",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = gpsEnabled,
                        onCheckedChange = { viewModel.setGpsEnabled(it) }
                    )
                }
                
                Divider()
                
                // Bluetooth Setting
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Bluetooth Support", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = "Enable Bluetooth scanning and attacks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = bluetoothEnabled,
                        onCheckedChange = { viewModel.setBluetoothEnabled(it) }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Device Information Section
        Text("Device Information", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Version", deviceInfo?.version ?: "Unknown")
                InfoRow("Hardware", deviceInfo?.hardware ?: "Unknown")
                InfoRow("Free Heap", "${deviceInfo?.freeHeap ?: 0} bytes")
                InfoRow("Uptime", "${deviceInfo?.uptime ?: 0} ms")
                deviceInfo?.batteryLevel?.let {
                    InfoRow("Battery", "$it%")
                }
                deviceInfo?.temperature?.let {
                    InfoRow("Temperature", "${it}°C")
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.refreshDeviceInfo() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Refresh Info")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Device Control Section
        Text("Device Control", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        
        Button(
            onClick = { viewModel.reboot() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.PowerSettingsNew, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reboot Marauder")
        }
        
        Spacer(modifier = Modifier.height(48.dp))
    }
    
    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Select Theme") },
            text = {
                Column {
                    RadioButtonRow(
                        text = "Light",
                        selected = themeMode == PreferencesManager.ThemeMode.LIGHT,
                        onClick = {
                            viewModel.setThemeMode(PreferencesManager.ThemeMode.LIGHT)
                            showThemeDialog = false
                        }
                    )
                    RadioButtonRow(
                        text = "Dark",
                        selected = themeMode == PreferencesManager.ThemeMode.DARK,
                        onClick = {
                            viewModel.setThemeMode(PreferencesManager.ThemeMode.DARK)
                            showThemeDialog = false
                        }
                    )
                    RadioButtonRow(
                        text = "System Default",
                        selected = themeMode == PreferencesManager.ThemeMode.SYSTEM,
                        onClick = {
                            viewModel.setThemeMode(PreferencesManager.ThemeMode.SYSTEM)
                            showThemeDialog = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun RadioButtonRow(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, modifier = Modifier.weight(1f))
    }
}
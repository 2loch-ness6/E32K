package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcallmekoko.maraudercontroller.data.protocol.SSID
import com.justcallmekoko.maraudercontroller.data.protocol.Station
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@Composable
fun ListsScreen(viewModel: MarauderViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val stations by viewModel.stations.collectAsState()
    val ssids by viewModel.ssids.collectAsState()
    
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Stations") })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("SSIDs") })
        }
        
        when (selectedTab) {
            0 -> StationList(
                stations = stations, 
                onRefresh = { viewModel.refreshStations() }, 
                onClear = { viewModel.clearStations() },
                onDeauth = { station -> 
                    viewModel.startTargetedAttack(station.channel, station.bssid, station.mac)
                }
            )
            1 -> SsidList(
                ssids = ssids, 
                onRefresh = { viewModel.refreshSsids() }, 
                onAdd = { viewModel.addSsid(it) }, 
                onClear = { viewModel.clearSsids() }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationList(
    stations: List<Station>,
    onRefresh: () -> Unit,
    onClear: () -> Unit,
    onDeauth: (Station) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            OutlinedButton(onClick = onClear) { Text("Clear") }
        }
        
        if (stations.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Stations Found", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(stations) { station ->
                    StationItem(station = station, onDeauth = onDeauth)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun StationItem(
    station: Station,
    onDeauth: (Station) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {},
                onLongClick = { showMenu = true }
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = station.mac,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (station.rssi != 0) {
                     Surface(
                        color = getRssiColor(station.rssi).copy(alpha = 0.1f),
                        contentColor = getRssiColor(station.rssi),
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "${station.rssi} dBm",
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(text = "Vendor: ${station.vendor.ifBlank { "Unknown" }}", style = MaterialTheme.typography.bodySmall)
            
            if (station.bssid.isNotBlank()) {
                 Text(
                    text = "AP: ${station.bssid}", 
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Ch: ${station.channel}", fontSize = 12.sp)
                Text(text = "Pkts: ${station.packets}", fontSize = 12.sp)
            }
            
            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Deauth Station") },
                    onClick = {
                        onDeauth(station)
                        showMenu = false
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    }
                )
            }
        }
    }
}

@Composable
fun SsidList(
    ssids: List<SSID>,
    onRefresh: () -> Unit,
    onAdd: (String) -> Unit,
    onClear: () -> Unit
) {
    var newSsid by remember { mutableStateOf("") }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newSsid,
                onValueChange = { newSsid = it },
                label = { Text("New SSID") },
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = { 
                if (newSsid.isNotBlank()) {
                    onAdd(newSsid)
                    newSsid = ""
                }
            }) { Text("Add") }
        }
        
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            OutlinedButton(onClick = onClear) { Text("Clear All") }
        }

        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            items(ssids) { ssid ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.padding(12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = ssid.name, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}
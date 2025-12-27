package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcallmekoko.maraudercontroller.data.protocol.AccessPoint
import com.justcallmekoko.maraudercontroller.data.protocol.Station
import com.justcallmekoko.maraudercontroller.data.protocol.SSID
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

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
            0 -> StationList(stations = stations, onRefresh = { viewModel.refreshStations() }, onClear = { viewModel.clearAccessPoints() }) // Note: clearStations is what we want but API might be mapped differently, using what's avail in Repo
            1 -> SsidList(ssids = ssids, onRefresh = { viewModel.refreshSsids() }, onAdd = { viewModel.addSsid(it) }, onClear = { viewModel.clearSsids() })
        }
    }
}

@Composable
fun StationList(
    stations: List<Station>,
    onRefresh: () -> Unit,
    onClear: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            OutlinedButton(onClick = onRefresh) { Text("Refresh") }
            // OutlinedButton(onClick = onClear) { Text("Clear") }
        }
        
        LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
            items(stations) { station ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = station.mac, fontWeight = FontWeight.Bold)
                        Text(text = "Vendor: ${station.vendor}", fontSize = 12.sp)
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(text = "RSSI: ${station.rssi}", fontSize = 12.sp)
                            Text(text = "Pkts: ${station.packets}", fontSize = 12.sp)
                        }
                    }
                }
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
                        // Could add remove button here if supported by API easily per item
                    }
                }
            }
        }
    }
}
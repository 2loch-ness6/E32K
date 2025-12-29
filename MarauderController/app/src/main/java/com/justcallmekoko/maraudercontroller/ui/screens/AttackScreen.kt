package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.justcallmekoko.maraudercontroller.data.protocol.AttackType
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@Composable
fun AttackScreen(viewModel: MarauderViewModel) {
    val isAttacking by viewModel.isAttacking.collectAsState()
    val currentAttack by viewModel.currentAttack.collectAsState()
    val selectedCount by viewModel.selectedAccessPointsCount.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isAttacking) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "ATTACK IN PROGRESS",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Type: ${currentAttack?.name ?: "Unknown"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.stopAttack() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("STOP ATTACK")
                    }
                }
            }
        } else {
            Text(
                text = "Targeting: $selectedCount Access Points",
                style = MaterialTheme.typography.titleMedium,
                color = if (selectedCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("WiFi Attacks", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            
            AttackButton("Deauth Flood", "Disconnects clients from selected APs") {
                viewModel.startAttack(AttackType.DEAUTH)
            }
            
            AttackButton("Probe Flood", "Floods selected APs with probe requests") {
                viewModel.startAttack(AttackType.PROBE)
            }
            
            AttackButton("Beacon Spam", "Creates fake APs (needs SSIDs list)") {
                viewModel.startAttack(AttackType.BEACON_SPAM)
            }
            
            AttackButton("Rick Roll", "Creates APs with Rick Roll lyrics") {
                viewModel.startAttack(AttackType.RICK_ROLL)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Other Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            
            Button(
                onClick = { viewModel.deselectAll() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Deselect All Targets")
            }
        }
    }
}

@Composable
fun AttackButton(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(text = title, style = MaterialTheme.typography.titleMedium)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
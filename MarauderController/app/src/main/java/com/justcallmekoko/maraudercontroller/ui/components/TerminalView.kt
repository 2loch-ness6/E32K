package com.justcallmekoko.maraudercontroller.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@Composable
fun TerminalView(
    viewModel: MarauderViewModel,
    modifier: Modifier = Modifier
) {
    val terminalOutput by viewModel.terminalOutput.collectAsState()
    
    Column(modifier = modifier.background(Color.Black)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Terminal", color = Color.Green, fontSize = 12.sp)
            TextButton(onClick = { viewModel.clearTerminal() }) {
                Text("Clear", color = Color.Green, fontSize = 12.sp)
            }
        }
        
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            reverseLayout = true
        ) {
            items(terminalOutput.reversed()) { line ->
                Text(
                    text = line,
                    color = Color.Green,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp
                )
            }
        }
    }
}

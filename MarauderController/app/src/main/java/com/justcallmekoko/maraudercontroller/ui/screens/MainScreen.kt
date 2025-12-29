package com.justcallmekoko.maraudercontroller.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager
import com.justcallmekoko.maraudercontroller.ui.components.*
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MarauderViewModel
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val showTerminal by viewModel.showTerminal.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    
    var showConnectionDialog by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(connectionState) {
        if (connectionState is SerialConnectionManager.ConnectionState.Error) {
            snackbarHostState.showSnackbar(
                message = (connectionState as SerialConnectionManager.ConnectionState.Error).message,
                withDismissAction = true
            )
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "GEMINI NEXUS", 
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                actions = {
                    // Connection status indicator
                    ConnectionStatusChip(
                        connectionState = connectionState,
                        onClick = { showConnectionDialog = true }
                    )
                    
                    IconButton(onClick = { viewModel.toggleTerminal() }) {
                        Icon(Icons.Default.Terminal, "Terminal", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    
                    if (isConnected) {
                        IconButton(onClick = { viewModel.refreshDeviceInfo() }) {
                            Icon(Icons.Default.Info, "Device Info", tint = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (isConnected) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ) {
                    NavigationBarItem(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        icon = { Icon(Icons.Default.Dashboard, "Dashboard") },
                        label = { Text("Home") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        icon = { Icon(Icons.Default.Wifi, "WiFi") },
                        label = { Text("WiFi") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        icon = { Icon(Icons.Default.Security, "Attacks") },
                        label = { Text("Attacks") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 3,
                        onClick = { viewModel.selectTab(3) },
                        icon = { Icon(Icons.Default.Settings, "Settings") },
                        label = { Text("Config") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                    NavigationBarItem(
                        selected = selectedTab == 4,
                        onClick = { viewModel.selectTab(4) },
                        icon = { Icon(Icons.Default.Folder, "Files") },
                        label = { Text("Files") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Main content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                when {
                    !isConnected -> {
                        ConnectionScreen(viewModel = viewModel)
                    }
                    selectedTab == 0 -> {
                        DashboardScreen(viewModel = viewModel)
                    }
                    selectedTab == 1 -> {
                        WiFiScanScreen(viewModel = viewModel)
                    }
                    selectedTab == 2 -> {
                        AttackScreen(viewModel = viewModel)
                    }
                    selectedTab == 3 -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                    selectedTab == 4 -> {
                        FileScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
    
    // Terminal Bottom Sheet
    if (showTerminal) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.toggleTerminal() },
            sheetState = rememberModalBottomSheetState(),
            containerColor = androidx.compose.ui.graphics.Color.Black
        ) {
            TerminalView(
                viewModel = viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.5f)
            )
        }
    }
    
    // Connection dialog
    if (showConnectionDialog) {
        ConnectionDialog(
            viewModel = viewModel,
            onDismiss = { showConnectionDialog = false }
        )
    }
}

@Composable
fun ConnectionStatusChip(
    connectionState: SerialConnectionManager.ConnectionState,
    onClick: () -> Unit
) {
    val (text, color) = when (connectionState) {
        is SerialConnectionManager.ConnectionState.Connected -> 
            "Connected" to MaterialTheme.colorScheme.primary
        is SerialConnectionManager.ConnectionState.Connecting -> 
            "Connecting..." to MaterialTheme.colorScheme.secondary
        is SerialConnectionManager.ConnectionState.Disconnected -> 
            "Disconnected" to MaterialTheme.colorScheme.error
        is SerialConnectionManager.ConnectionState.Error -> 
            "Error" to MaterialTheme.colorScheme.error
    }
    
    FilterChip(
        selected = connectionState is SerialConnectionManager.ConnectionState.Connected,
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = {
            Icon(
                imageVector = when (connectionState) {
                    is SerialConnectionManager.ConnectionState.Connected -> Icons.Default.CheckCircle
                    is SerialConnectionManager.ConnectionState.Connecting -> Icons.Default.Refresh
                    else -> Icons.Default.Error
                },
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = color.copy(alpha = 0.2f),
            selectedLabelColor = color
        )
    )
}

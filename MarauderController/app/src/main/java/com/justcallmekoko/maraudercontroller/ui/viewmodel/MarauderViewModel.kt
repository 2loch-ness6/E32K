package com.justcallmekoko.maraudercontroller.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.justcallmekoko.maraudercontroller.data.preferences.PreferencesManager
import com.justcallmekoko.maraudercontroller.data.protocol.*
import com.justcallmekoko.maraudercontroller.data.repository.MarauderRepository
import com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Main ViewModel for Marauder controller
 */
class MarauderViewModel(
    private val repository: MarauderRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {
    
    // Connection state
    val connectionState: StateFlow<SerialConnectionManager.ConnectionState> = 
        repository.connectionState
    
    // Device data
    val accessPoints: StateFlow<List<AccessPoint>> = repository.accessPoints
    val stations: StateFlow<List<Station>> = repository.stations
    val ssids: StateFlow<List<SSID>> = repository.ssids
    val deviceInfo: StateFlow<DeviceInfo?> = repository.deviceInfo
    val gpsData: StateFlow<GpsData?> = repository.gpsData
    val packetStats: StateFlow<PacketStats> = repository.packetStats
    val currentScan: StateFlow<ScanType?> = repository.currentScan
    val currentAttack: StateFlow<AttackType?> = repository.currentAttack
    val terminalOutput: StateFlow<List<String>> = repository.terminalOutput
    val currentChannel: StateFlow<Int> = repository.currentChannel
    
    // UI state
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()
    
    private val _showTerminal = MutableStateFlow(false)
    val showTerminal: StateFlow<Boolean> = _showTerminal.asStateFlow()

    private val _isLiveScanning = MutableStateFlow(false)
    val isLiveScanning: StateFlow<Boolean> = _isLiveScanning.asStateFlow()
    
    // Derived state
    val isConnected: StateFlow<Boolean> = connectionState.map { 
        it is SerialConnectionManager.ConnectionState.Connected 
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val isScanning: StateFlow<Boolean> = currentScan.map { 
        it != null 
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val isAttacking: StateFlow<Boolean> = currentAttack.map { 
        it != null 
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    
    val selectedAccessPointsCount: StateFlow<Int> = accessPoints.map { 
        it.count { ap -> ap.selected } 
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0)
    
    // Connection management
    fun findDevices() = repository.findDevices()
    
    fun connect(deviceIndex: Int) {
        viewModelScope.launch {
            repository.connect(deviceIndex)
        }
    }
    
    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }
    
    // Scanning operations
    fun startScanAp(continuous: Boolean = false, timeout: Int? = null) {
        repository.scanAp(continuous, timeout)
    }
    
    fun startScanStation(continuous: Boolean = false) {
        repository.scanStation(continuous)
    }
    
    fun stopScan() {
        if (_isLiveScanning.value) {
            repository.stopLiveScan()
            _isLiveScanning.value = false
        }
        repository.stopScan()
    }

    fun toggleLiveScan() {
        if (_isLiveScanning.value) {
            repository.stopLiveScan()
            _isLiveScanning.value = false
        } else {
            repository.startLiveScan()
            _isLiveScanning.value = true
        }
    }
    
    fun refreshAccessPoints() {
        repository.listAccessPoints()
    }
    
    fun refreshStations() {
        repository.listStations()
    }
    
    fun refreshSsids() {
        repository.listSsids()
    }
    
    // Access point management
    fun selectAccessPoint(index: Int) {
        repository.selectAccessPoint(index)
        // Refresh list to see selection
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listAccessPoints()
        }
    }
    
    fun selectAllAccessPoints() {
        repository.selectAllAccessPoints()
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listAccessPoints()
        }
    }
    
    fun deselectAll() {
        repository.deselectAll()
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listAccessPoints()
        }
    }
    
    fun clearAccessPoints() {
        repository.clearAccessPoints()
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listAccessPoints()
        }
    }
    
    // SSID management
    fun addSsid(ssid: String) {
        repository.addSsid(ssid)
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listSsids()
        }
    }
    
    fun removeSsid(ssid: String) {
        repository.removeSsid(ssid)
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listSsids()
        }
    }
    
    fun generateRandomSsids(count: Int) {
        repository.generateRandomSsids(count)
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listSsids()
        }
    }
    
    fun clearSsids() {
        repository.clearSsids()
        viewModelScope.launch {
            kotlinx.coroutines.delay(100)
            repository.listSsids()
        }
    }
    
    // Attack operations
    fun startAttack(type: AttackType, targetMac: String? = null, timeout: Int? = null) {
        repository.attack(type, targetMac, timeout)
    }
    
    fun stopAttack() {
        repository.stopScan() // Stop scan stops attacks too
    }
    
    // Channel management
    fun setChannel(channel: Int) {
        repository.setChannel(channel)
    }
    
    // Device info
    fun refreshDeviceInfo() {
        repository.getDeviceInfo()
    }
    
    fun refreshGpsData() {
        repository.getGpsData()
    }
    
    fun reboot() {
        repository.reboot()
    }
    
    // Terminal
    fun sendCommand(command: String) {
        repository.sendCommand(command)
    }
    
    fun clearTerminal() {
        repository.clearTerminal()
    }
    
    fun toggleTerminal() {
        _showTerminal.value = !_showTerminal.value
    }
    
    // UI navigation
    fun selectTab(index: Int) {
        _selectedTab.value = index
    }
    
    // Theme management
    fun setThemeMode(mode: PreferencesManager.ThemeMode) {
        viewModelScope.launch {
            preferencesManager.setThemeMode(mode)
        }
    }
    
    val themeModeFlow = preferencesManager.themeModeFlow
    
    // Preferences
    fun setAutoConnect(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setAutoConnect(enabled)
        }
    }
    
    val autoConnectFlow = preferencesManager.autoConnectFlow
    
    fun setGpsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setGpsEnabled(enabled)
        }
    }
    
    val gpsEnabledFlow = preferencesManager.gpsEnabledFlow
    
    fun setBluetoothEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setBluetoothEnabled(enabled)
        }
    }
    
    val bluetoothEnabledFlow = preferencesManager.bluetoothEnabledFlow
    
    override fun onCleared() {
        super.onCleared()
        repository.release()
    }
    
    class Factory(
        private val repository: MarauderRepository,
        private val preferencesManager: PreferencesManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MarauderViewModel::class.java)) {
                return MarauderViewModel(repository, preferencesManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}

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
    val fileList: StateFlow<List<MarauderRepository.FileEntry>> = repository.fileList
    
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

    fun startTargetedAttack(channel: Int, apMac: String, stationMac: String) {
        viewModelScope.launch {
            repository.sendBinaryAttack(0x01, channel, apMac, stationMac)
        }
    }
    
    fun refreshFileList() {
        viewModelScope.launch {
            repository.refreshFileList()
        }
    }
    
    fun deleteFile(filename: String) {
        viewModelScope.launch {
            repository.deleteFile(filename)
        }
    }
    
    // Simple download to local cache for now
    fun downloadFile(context: android.content.Context, filename: String) {
        viewModelScope.launch {
            val file = java.io.File(context.getExternalFilesDir(null), filename)
            val outputStream = java.io.FileOutputStream(file)
            try {
                repository.downloadFile(filename).collect { chunk ->
                    outputStream.write(chunk)
                }
                android.widget.Toast.makeText(context, "Downloaded to ${file.absolutePath}", android.widget.Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                android.widget.Toast.makeText(context, "Download failed", android.widget.Toast.LENGTH_SHORT).show()
            } finally {
                outputStream.close()
            }
        }
    }

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

package com.justcallmekoko.maraudercontroller.data.repository

import android.content.Context
import com.justcallmekoko.maraudercontroller.data.protocol.*
import com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Repository managing Marauder device state and communication
 */
class MarauderRepository(context: Context) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val serialManager = SerialConnectionManager(context)
    private val parser = MarauderProtocolParser()
    
    // Connection state
    val connectionState: StateFlow<SerialConnectionManager.ConnectionState> = 
        serialManager.connectionState
    
    // Device state
    private val _accessPoints = MutableStateFlow<List<AccessPoint>>(emptyList())
    val accessPoints: StateFlow<List<AccessPoint>> = _accessPoints.asStateFlow()
    
    private val _stations = MutableStateFlow<List<Station>>(emptyList())
    val stations: StateFlow<List<Station>> = _stations.asStateFlow()
    
    private val _ssids = MutableStateFlow<List<SSID>>(emptyList())
    val ssids: StateFlow<List<SSID>> = _ssids.asStateFlow()
    
    private val _deviceInfo = MutableStateFlow<DeviceInfo?>(null)
    val deviceInfo: StateFlow<DeviceInfo?> = _deviceInfo.asStateFlow()
    
    private val _gpsData = MutableStateFlow<GpsData?>(null)
    val gpsData: StateFlow<GpsData?> = _gpsData.asStateFlow()
    
    private val _packetStats = MutableStateFlow(PacketStats())
    val packetStats: StateFlow<PacketStats> = _packetStats.asStateFlow()
    
    private val _currentScan = MutableStateFlow<ScanType?>(null)
    val currentScan: StateFlow<ScanType?> = _currentScan.asStateFlow()
    
    private val _currentAttack = MutableStateFlow<AttackType?>(null)
    val currentAttack: StateFlow<AttackType?> = _currentAttack.asStateFlow()
    
    private val _terminalOutput = MutableStateFlow<List<String>>(emptyList())
    val terminalOutput: StateFlow<List<String>> = _terminalOutput.asStateFlow()
    
    private val _currentChannel = MutableStateFlow(1)
    val currentChannel: StateFlow<Int> = _currentChannel.asStateFlow()
    
    private val responseBuffer = mutableListOf<String>()
    private var listMode: ListMode? = null
    
    enum class ListMode {
        ACCESS_POINTS,
        STATIONS,
        SSIDS
    }
    
    init {
        // Monitor serial data
        scope.launch {
            serialManager.receivedData.collect { line ->
                if (line.isNotEmpty()) {
                    processSerialLine(line)
                }
            }
        }
    }
    
    /**
     * Find available devices
     */
    fun findDevices() = serialManager.findDevices()
    
    /**
     * Connect to device
     */
    fun connect(driverIndex: Int) {
        val devices = serialManager.findDevices()
        if (driverIndex in devices.indices) {
            serialManager.requestConnection(devices[driverIndex])
        }
    }
    
    /**
     * Disconnect from device
     */
    fun disconnect() {
        serialManager.disconnect()
    }
    
    /**
     * Send command to device
     */
    fun sendCommand(command: String) {
        addToTerminal("> $command")
        serialManager.sendCommand(command)
    }
    
    /**
     * Process received serial line
     */
    private fun processSerialLine(line: String) {
        // Add to terminal output
        addToTerminal(line)
        
        // Parse response
        val response = parser.parseLine(line)
        
        when (response) {
            is MarauderResponse.Prompt -> {
                // Process buffered list if in list mode
                processBufferedList()
                listMode = null
                responseBuffer.clear()
            }
            
            is MarauderResponse.ScanStarted -> {
                _currentScan.value = mapScanType(response.scanType)
            }
            
            is MarauderResponse.ScanStopped -> {
                _currentScan.value = null
            }
            
            is MarauderResponse.AttackStarted -> {
                _currentAttack.value = mapAttackType(response.attackType)
            }
            
            is MarauderResponse.AttackStopped -> {
                _currentAttack.value = null
            }
            
            is MarauderResponse.Info -> {
                _deviceInfo.value = response.info
            }
            
            is MarauderResponse.GpsInfo -> {
                _gpsData.value = response.gps
            }
            
            is MarauderResponse.RawOutput -> {
                // Check if this is part of a list
                if (line.startsWith("[") && line.contains("]")) {
                    responseBuffer.add(line)
                } else {
                    // Check for list headers
                    when {
                        line.contains("Access Points", ignoreCase = true) -> {
                            listMode = ListMode.ACCESS_POINTS
                            responseBuffer.clear()
                        }
                        line.contains("Stations", ignoreCase = true) -> {
                            listMode = ListMode.STATIONS
                            responseBuffer.clear()
                        }
                        line.contains("SSIDs", ignoreCase = true) -> {
                            listMode = ListMode.SSIDS
                            responseBuffer.clear()
                        }
                    }
                }
            }
            
            else -> {}
        }
    }
    
    /**
     * Process buffered list data
     */
    private fun processBufferedList() {
        if (responseBuffer.isEmpty()) return
        
        when (listMode) {
            ListMode.ACCESS_POINTS -> {
                _accessPoints.value = parser.parseAccessPointList(responseBuffer)
            }
            ListMode.STATIONS -> {
                _stations.value = parser.parseStationList(responseBuffer)
            }
            ListMode.SSIDS -> {
                _ssids.value = parser.parseSsidList(responseBuffer)
            }
            null -> {}
        }
    }
    
    /**
     * Add line to terminal output
     */
    private fun addToTerminal(line: String) {
        val current = _terminalOutput.value.toMutableList()
        current.add(line)
        
        // Keep only last 500 lines
        if (current.size > 500) {
            _terminalOutput.value = current.takeLast(500)
        } else {
            _terminalOutput.value = current
        }
    }
    
    /**
     * Clear terminal output
     */
    fun clearTerminal() {
        _terminalOutput.value = emptyList()
    }
    
    // Convenience methods for common commands
    
    fun scanAp(continuous: Boolean = false, timeout: Int? = null) {
        sendCommand(MarauderCommands.buildScanApCommand(continuous, timeout))
    }
    
    fun scanStation(continuous: Boolean = false) {
        sendCommand(if (continuous) "${MarauderCommands.SCAN_STA} -c" else MarauderCommands.SCAN_STA)
    }
    
    fun stopScan() {
        scope.launch {
            addToTerminal("> ${MarauderCommands.STOP_SCAN}")
            // Wait for "Stopping" or "Stopped" to ensure scan is done before allowing new commands
            val pattern = Regex("Stopping|Stopped", RegexOption.IGNORE_CASE)
            serialManager.sendCommandAndWait(MarauderCommands.STOP_SCAN, pattern)
        }
    }
    
    fun listAccessPoints() {
        sendCommand(MarauderCommands.buildListCommand(apList = true))
    }
    
    fun listStations() {
        sendCommand(MarauderCommands.buildListCommand(staList = true))
    }
    
    fun listSsids() {
        sendCommand(MarauderCommands.buildListCommand(ssidList = true))
    }
    
    fun selectAccessPoint(index: Int) {
        sendCommand(MarauderCommands.buildSelectCommand(apIndex = index))
    }
    
    fun selectAllAccessPoints() {
        sendCommand(MarauderCommands.buildSelectCommand(all = true))
    }
    
    fun deselectAll() {
        sendCommand(MarauderCommands.buildSelectCommand(all = true, deselect = true))
    }
    
    fun addSsid(ssid: String) {
        sendCommand(MarauderCommands.buildSsidCommand(ssid = ssid, add = true))
    }
    
    fun removeSsid(ssid: String) {
        sendCommand(MarauderCommands.buildSsidCommand(ssid = ssid, remove = true))
    }
    
    fun generateRandomSsids(count: Int) {
        sendCommand(MarauderCommands.buildSsidCommand(random = true, count = count))
    }
    
    fun attack(type: AttackType, targetMac: String? = null, timeout: Int? = null) {
        val typeStr = when (type) {
            AttackType.DEAUTH -> "deauth"
            AttackType.BEACON_SPAM -> "beacon"
            AttackType.PROBE -> "probe"
            AttackType.RICK_ROLL -> "rickroll"
            AttackType.AP_SPAM -> "spam"
            AttackType.MIMIC -> "mimic"
            else -> return
        }
        sendCommand(MarauderCommands.buildAttackCommand(typeStr, targetMac, timeout = timeout))
    }
    
    fun clearAccessPoints() {
        sendCommand(MarauderCommands.buildClearCommand(clearAps = true))
    }
    
    fun clearSsids() {
        sendCommand(MarauderCommands.buildClearCommand(clearSsids = true))
    }
    
    fun clearStations() {
        sendCommand(MarauderCommands.buildClearCommand(clearStations = true))
    }
    
    fun setChannel(channel: Int) {
        _currentChannel.value = channel
        sendCommand(MarauderCommands.buildChannelCommand(channel))
    }
    
    fun getDeviceInfo() {
        sendCommand(MarauderCommands.INFO)
    }
    
    fun getGpsData() {
        sendCommand(MarauderCommands.GPS_DATA)
    }
    
    fun reboot() {
        sendCommand(MarauderCommands.REBOOT)
    }
    
    /**
     * Map scan type string to enum
     */
    private fun mapScanType(type: String): ScanType? {
        return when {
            type.contains("AP", ignoreCase = true) -> ScanType.AP_SCAN
            type.contains("station", ignoreCase = true) -> ScanType.STATION_SCAN
            type.contains("beacon", ignoreCase = true) -> ScanType.BEACON_SNIFF
            type.contains("probe", ignoreCase = true) -> ScanType.PROBE_SNIFF
            type.contains("deauth", ignoreCase = true) -> ScanType.DEAUTH_SNIFF
            type.contains("pmkid", ignoreCase = true) -> ScanType.PMKID_SNIFF
            type.contains("esp", ignoreCase = true) -> ScanType.ESP_SNIFF
            type.contains("raw", ignoreCase = true) -> ScanType.RAW_CAPTURE
            type.contains("wardrive", ignoreCase = true) -> ScanType.WARDRIVE
            else -> null
        }
    }
    
    /**
     * Map attack type string to enum
     */
    private fun mapAttackType(type: String): AttackType? {
        return when {
            type.contains("deauth", ignoreCase = true) -> AttackType.DEAUTH
            type.contains("beacon", ignoreCase = true) -> AttackType.BEACON_SPAM
            type.contains("probe", ignoreCase = true) -> AttackType.PROBE
            type.contains("rick", ignoreCase = true) -> AttackType.RICK_ROLL
            type.contains("spam", ignoreCase = true) -> AttackType.AP_SPAM
            type.contains("mimic", ignoreCase = true) -> AttackType.MIMIC
            else -> null
        }
    }
    
    fun release() {
        serialManager.release()
    }
}

package com.justcallmekoko.maraudercontroller.data.repository

import android.content.Context
import com.justcallmekoko.maraudercontroller.data.protocol.*
import com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager
import com.justcallmekoko.maraudercontroller.data.serial.ConnectionRecoveryManager
import com.justcallmekoko.maraudercontroller.data.serial.CommandRetryManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Repository managing Marauder device state and communication
 */
class MarauderRepository(context: Context) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val serialManager = SerialConnectionManager(context)
    private val parser = MarauderProtocolParser()
    private val recoveryManager = ConnectionRecoveryManager()
    private val retryManager = CommandRetryManager()
    
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

    private val _bluetoothDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val bluetoothDevices: StateFlow<List<BluetoothDevice>> = _bluetoothDevices.asStateFlow()
    
    private val _currentChannel = MutableStateFlow(1)
    val currentChannel: StateFlow<Int> = _currentChannel.asStateFlow()

    private val _fileList = MutableStateFlow<List<FileEntry>>(emptyList())
    val fileList: StateFlow<List<FileEntry>> = _fileList.asStateFlow()
    
    private val _downloadProgress = MutableStateFlow<DownloadProgress?>(null)
    val downloadProgress: StateFlow<DownloadProgress?> = _downloadProgress.asStateFlow()
    
    // Recovery state
    val recoveryState: StateFlow<ConnectionRecoveryManager.RecoveryState> = 
        recoveryManager.recoveryState
    
    private val responseBuffer = mutableListOf<String>()
    private var listMode: ListMode? = null
    private var liveScanJob: Job? = null
    
    data class FileEntry(
        val name: String,
        val size: Long
    )
    
    data class FileChunk(val seq: Int, val data: ByteArray)
    
    private val _fileDownloadChunks = MutableSharedFlow<FileChunk>(extraBufferCapacity = 100)

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

        // Monitor binary data
        scope.launch {
            serialManager.binaryEvents.collect { packet ->
                processBinaryPacket(packet)
            }
        }
    }
    
    /**
     * Process received binary packet
     */
    private fun processBinaryPacket(packet: MarauderBinaryProtocol.BinaryPacket) {
        if (packet.cmd == MarauderBinaryProtocol.RESP_SCAN_DATA) {
            parseScanData(packet.payload)
        }
    }

    private fun parseScanData(payload: ByteArray) {
        if (payload.isEmpty()) return
        
        val type = payload[0].toInt()
        
        // Type 0x01: Access Point
        if (type == 0x01 && payload.size >= 11) {
            try {
                // [Type:1][RSSI:1][Ch:1][MAC:6][Auth:1][SSID_Len:1][SSID:Var]
                val rssi = payload[1].toByte().toInt()
                val channel = payload[2].toInt() and 0xFF
                
                // Extract MAC
                val macBytes = payload.copyOfRange(3, 9)
                val mac = macBytes.joinToString(":") { "%02X".format(it) }
                
                val auth = payload[9].toInt() and 0xFF
                
                val ssidLen = payload[10].toInt() and 0xFF
                var ssid = ""
                if (ssidLen > 0 && payload.size >= 11 + ssidLen) {
                    ssid = String(payload, 11, ssidLen)
                }
                
                // Create AP Object
                val newAp = AccessPoint(
                    ssid = ssid,
                    bssid = mac,
                    channel = channel,
                    rssi = rssi,
                    encryption = if (auth == 1) "WPA" else "OPEN", // Simplified
                    lastSeen = System.currentTimeMillis()
                )
                
                updateAccessPointList(newAp)
                
            } catch (e: Exception) {
                // Log parsing error
            }
        }
        // Type 0x02: Station
        else if (type == 0x02 && payload.size >= 15) {
            try {
                // [Type:1][RSSI:1][MAC:6][BSSID:6][Ch:1]
                val rssi = payload[1].toByte().toInt()
                val mac = payload.copyOfRange(2, 8).joinToString(":") { "%02X".format(it) }
                val bssid = payload.copyOfRange(8, 14).joinToString(":") { "%02X".format(it) }
                val channel = payload[14].toInt() and 0xFF

                val newSta = Station(
                    mac = mac,
                    bssid = bssid,
                    rssi = rssi,
                    channel = channel,
                    lastSeen = System.currentTimeMillis()
                )
                updateStationList(newSta)
            } catch (e: Exception) { }
        }
        // Type 0x03: BLE Device
        else if (type == 0x03 && payload.size >= 9) {
            try {
                // [Type:1][RSSI:1][MAC:6][Name_Len:1][Name:Var]
                val rssi = payload[1].toByte().toInt()
                val mac = payload.copyOfRange(2, 8).joinToString(":") { "%02X".format(it) }
                val nameLen = payload[8].toInt() and 0xFF
                var name = ""
                if (nameLen > 0 && payload.size >= 9 + nameLen) {
                    name = String(payload, 9, nameLen)
                }

                val newBle = BluetoothDevice(
                    address = mac,
                    name = name,
                    rssi = rssi,
                    lastSeen = System.currentTimeMillis()
                )
                updateBluetoothDeviceList(newBle)
            } catch (e: Exception) { }
        }
        // Type 0x04: GPS
        else if (type == 0x04 && payload.size >= 27) {
            try {
                // [Type:1][Lat:8][Lon:8][Alt:8][Sats:1][Fix:1]
                val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
                buffer.get() // Skip type
                val lat = buffer.getDouble()
                val lon = buffer.getDouble()
                val alt = buffer.getDouble()
                val sats = buffer.get().toInt() and 0xFF
                val fix = buffer.get().toInt() != 0
                
                _gpsData.value = GpsData(
                    latitude = lat,
                    longitude = lon,
                    altitude = alt,
                    satellites = sats,
                    fix = fix,
                    timestamp = System.currentTimeMillis().toString()
                )
            } catch (e: Exception) { }
        }
        // Type 0x05: File Entry
        else if (type == 0x05 && payload.size > 5) {
            try {
                // [Type:1][Size:4][Name_Len:1][Name:Var]
                val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
                buffer.get() // Skip type
                val size = buffer.getInt().toLong() and 0xFFFFFFFFL
                val nameLen = buffer.get().toInt() and 0xFF
                var name = ""
                if (nameLen > 0 && payload.size >= 6 + nameLen) {
                    name = String(payload, 6, nameLen)
                }
                
                val newFile = FileEntry(name, size)
                updateFileList(newFile)
            } catch (e: Exception) { }
        }
        // Type 0x06: File Data
        else if (type == 0x06 && payload.size > 3) {
            try {
                // [Type:1][Seq:2][Len:1][Data:Var]
                val buffer = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN)
                buffer.get() // Skip type
                val seq = buffer.getShort().toInt() and 0xFFFF
                val dataLen = buffer.get().toInt() and 0xFF
                
                if (dataLen > 0 && payload.size >= 4 + dataLen) {
                    val chunk = payload.copyOfRange(4, 4 + dataLen)
                    scope.launch {
                        _fileDownloadChunks.emit(FileChunk(seq, chunk))
                    }
                } else if (dataLen == 0) {
                     // EOF Marker
                     scope.launch {
                        _fileDownloadChunks.emit(FileChunk(-1, ByteArray(0)))
                    }
                }
            } catch (e: Exception) { }
        }
    }

    private fun updateFileList(newFile: FileEntry) {
        val currentList = _fileList.value.toMutableList()
        if (currentList.none { it.name == newFile.name }) {
            currentList.add(newFile)
            _fileList.value = currentList
        }
    }

    private fun updateStationList(newSta: Station) {
        val currentList = _stations.value.toMutableList()
        val index = currentList.indexOfFirst { it.mac == newSta.mac }
        
        if (index != -1) {
            currentList[index] = newSta
        } else {
            currentList.add(newSta)
        }
        _stations.value = currentList
    }

    private fun updateBluetoothDeviceList(newBle: BluetoothDevice) {
        val currentList = _bluetoothDevices.value.toMutableList()
        val index = currentList.indexOfFirst { it.address == newBle.address }
        
        if (index != -1) {
            currentList[index] = newBle
        } else {
            currentList.add(newBle)
        }
        _bluetoothDevices.value = currentList
    }

    private fun updateAccessPointList(newAp: AccessPoint) {
        val currentList = _accessPoints.value.toMutableList()
        val index = currentList.indexOfFirst { it.bssid == newAp.bssid }
        
        if (index != -1) {
            // Update existing
            val existing = currentList[index]
            // Keep existing selection and encryption if unknown
            currentList[index] = newAp.copy(
                selected = existing.selected,
                encryption = if (newAp.encryption == "UNK") existing.encryption else newAp.encryption
            )
        } else {
            // Add new
            currentList.add(newAp)
        }
        _accessPoints.value = currentList
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
     * Connect to specific UsbDevice (Auto-Connect)
     */
    fun connectToDevice(device: android.hardware.usb.UsbDevice) {
        serialManager.connectToDevice(device)
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
            
            is MarauderResponse.VersionInfo -> {
                // Update device info with version
                val current = _deviceInfo.value ?: DeviceInfo()
                _deviceInfo.value = current.copy(version = response.version)
            }
            
            is MarauderResponse.HardwareInfo -> {
                // Update device info with hardware
                val current = _deviceInfo.value ?: DeviceInfo()
                _deviceInfo.value = current.copy(hardware = response.hardware)
            }
            
            is MarauderResponse.HeapInfo -> {
                // Update device info with free heap
                val current = _deviceInfo.value ?: DeviceInfo()
                _deviceInfo.value = current.copy(freeHeap = response.freeHeap.toInt())
            }
            
            is MarauderResponse.DeviceVersionInfo -> {
                // Update device info with all version data
                val current = _deviceInfo.value ?: DeviceInfo()
                _deviceInfo.value = current.copy(
                    version = response.version,
                    hardware = response.hardware,
                    freeHeap = response.freeHeap.toInt()
                )
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
        addToTerminal("> Binary: SCAN_AP")
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_SCAN_AP, 0, ByteArray(0)
        )
        serialManager.sendBinaryCommand(packet)
    }
    
    fun scanStation(continuous: Boolean = false) {
        addToTerminal("> Binary: SCAN_STA")
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_SCAN_STA, 0, ByteArray(0)
        )
        serialManager.sendBinaryCommand(packet)
    }
    
    private suspend fun stopScanSuspend() {
        addToTerminal("> Binary: STOP_SCAN")
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_STOP_SCAN, 0, ByteArray(0)
        )
        // For stop, we might want to wait for ACK
        serialManager.sendBinaryCommandAndWait(packet)
    }

    fun stopScan() {
        scope.launch { stopScanSuspend() }
    }

    fun stopAttack() {
        stopScan()
    }
    
    private suspend fun listAccessPointsSuspend() {
        // List is still text-based in firmware for now, or we rely on binary stream updates?
        // The prompt implies strict binary. But if firmware doesn't support binary LIST yet?
        // Schema says CMD_FS_LIST is for files. There is no CMD_LIST_AP in binary schema yet.
        // We rely on the streaming data from SCAN_AP.
        // However, if we need to re-request the list, we might still need legacy text or add CMD_LIST_AP.
        // For now, let's keep list text-based as it's not in the binary schema explicitly as a request-response 
        // other than streaming.
        val cmd = MarauderCommands.buildListCommand(apList = true)
        addToTerminal("> $cmd")
        // Wait for prompt > which indicates list is done
        val promptPattern = Regex("^>\\s*$")
        serialManager.sendCommandAndWait(cmd, promptPattern, timeoutMs = 10000)
    }

    fun startLiveScan(intervalMs: Long = 5000) {
        liveScanJob?.cancel()
        liveScanJob = scope.launch {
            try {
                while (isActive) {
                    scanAp(continuous = true)
                    delay(intervalMs)
                    stopScanSuspend()
                    listAccessPointsSuspend()
                    delay(500) // Short delay to read
                }
            } catch (e: CancellationException) {
                stopScanSuspend()
            }
        }
    }

    fun stopLiveScan() {
        liveScanJob?.cancel()
        liveScanJob = null
        stopScan()
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

    /**
     * Send targeted attack command via binary protocol (Phase 3)
     */
    fun sendBinaryAttack(type: Int, channel: Int, apMac: String, stationMac: String) {
        // Payload: [Type(1)][Channel(1)][AP_MAC(6)][Station_MAC(6)] = 14 Bytes
        val payload = ByteArray(14)

        payload[0] = type.toByte()
        payload[1] = channel.toByte()

        try {
            // Parse AP MAC
            val apParts = apMac.replace(":", "").chunked(2)
            for (i in 0 until 6.coerceAtMost(apParts.size)) {
                payload[2 + i] = apParts[i].toInt(16).toByte()
            }

            // Parse Station MAC
            val staParts = stationMac.replace(":", "").chunked(2)
            for (i in 0 until 6.coerceAtMost(staParts.size)) {
                payload[8 + i] = staParts[i].toInt(16).toByte()
            }
        } catch (e: Exception) {
            // Log error or ignore
        }

        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_ATTACK,
            payload.size,
            payload
        )
        serialManager.sendBinaryCommand(packet)
    }

    fun refreshFileList() {
        // Clear list first
        _fileList.value = emptyList()
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_FS_LIST, 0, ByteArray(0)
        )
        serialManager.sendBinaryCommand(packet)
    }

    fun deleteFile(filename: String) {
        val nameBytes = filename.toByteArray()
        val payload = ByteArray(1 + nameBytes.size)
        payload[0] = nameBytes.size.toByte()
        System.arraycopy(nameBytes, 0, payload, 1, nameBytes.size)
        
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_FS_DELETE, payload.size, payload
        )
        serialManager.sendBinaryCommand(packet)
        // Refresh after delay
        scope.launch {
            delay(500)
            refreshFileList()
        }
    }

    fun downloadFile(filename: String): Flow<ByteArray> = flow {
        // Get file size from the list
        val fileEntry = _fileList.value.find { it.name == filename }
        val totalSize = fileEntry?.size ?: 0L
        var downloadedSize = 0L
        
        // Initialize progress
        _downloadProgress.value = DownloadProgress(
            filename = filename,
            bytesDownloaded = 0,
            totalBytes = totalSize,
            progress = 0f
        )
        
        // Request download
        val nameBytes = filename.toByteArray()
        val payload = ByteArray(1 + nameBytes.size)
        payload[0] = nameBytes.size.toByte()
        System.arraycopy(nameBytes, 0, payload, 1, nameBytes.size)
        
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_FS_READ, payload.size, payload
        )
        serialManager.sendBinaryCommand(packet)
        
        try {
            // Collect until EOF (-1) or error
            // We use a longer timeout or infinite since we have an explicit EOF
            withTimeoutOrNull(60000) { // 60s timeout just in case
                _fileDownloadChunks.collect { chunk ->
                    if (chunk.seq == -1) {
                        // EOF reached - mark as complete
                        _downloadProgress.value = DownloadProgress(
                            filename = filename,
                            bytesDownloaded = downloadedSize,
                            totalBytes = totalSize,
                            progress = 1.0f,
                            isComplete = true
                        )
                        throw CancellationException("EOF")
                    }
                    downloadedSize += chunk.data.size
                    val progress = if (totalSize > 0) downloadedSize.toFloat() / totalSize else 0f
                    
                    // Update progress
                    _downloadProgress.value = DownloadProgress(
                        filename = filename,
                        bytesDownloaded = downloadedSize,
                        totalBytes = totalSize,
                        progress = progress
                    )
                    
                    emit(chunk.data)
                }
            }
        } catch (e: CancellationException) {
            // Normal end of stream - already marked complete above
        } catch (e: Exception) {
            // Error occurred
            _downloadProgress.value = DownloadProgress(
                filename = filename,
                bytesDownloaded = downloadedSize,
                totalBytes = totalSize,
                progress = if (totalSize > 0) downloadedSize.toFloat() / totalSize else 0f,
                isComplete = false,
                error = e.message ?: "Download failed"
            )
            throw e
        } finally {
            // Clear progress state after download completes or fails
            scope.launch {
                delay(2000) // Keep visible for 2 seconds
                _downloadProgress.value = null
            }
        }
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
        addToTerminal("> Binary: REBOOT")
        val packet = MarauderBinaryProtocol.BinaryPacket(
            MarauderBinaryProtocol.CMD_REBOOT, 0, ByteArray(0)
        )
        serialManager.sendBinaryCommand(packet)
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
    
    /**
     * Query device version from firmware
     */
    fun getDeviceVersion() {
        sendCommand("version")
    }
    
    /**
     * Query device hardware type from firmware
     */
    fun getDeviceHardware() {
        sendCommand("hardware")
    }
    
    /**
     * Query free heap from firmware
     */
    fun getDeviceHeap() {
        sendCommand("heap")
    }
    
    /**
     * Query all device information (version, hardware, heap)
     */
    fun queryDeviceInfo() {
        getDeviceVersion()
        getDeviceHardware()
        getDeviceHeap()
    }
    
    fun release() {
        // Stop any ongoing operations
        liveScanJob?.cancel()
        
        // Release recovery manager
        recoveryManager.release()
        
        // Release serial manager
        serialManager.release()
        
        // Cancel scope
        scope.cancel()
    }
}

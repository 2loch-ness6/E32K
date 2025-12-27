package com.justcallmekoko.maraudercontroller.data.firmware

import android.util.Log
import com.justcallmekoko.maraudercontroller.data.protocol.*
import com.justcallmekoko.maraudercontroller.data.serial.SerialConnectionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.InputStream

/**
 * Firmware Update Manager
 * Handles firmware version checking and OTA updates over serial
 */
class FirmwareUpdateManager(
    private val serialManager: SerialConnectionManager,
    private val firmwareManager: FirmwareManager
) {
    
    private val _updateStatus = MutableStateFlow<UpdateStatus>(UpdateStatus.Idle)
    val updateStatus: StateFlow<UpdateStatus> = _updateStatus.asStateFlow()
    
    private val _deviceVersion = MutableStateFlow<DeviceVersion?>(null)
    val deviceVersion: StateFlow<DeviceVersion?> = _deviceVersion.asStateFlow()
    
    companion object {
        private const val TAG = "FirmwareUpdate"
        private const val CHUNK_SIZE = 1024 // Send 1KB chunks
    }
    
    /**
     * Check device version and hardware
     */
    suspend fun checkDeviceVersion(): DeviceVersion? {
        _updateStatus.value = UpdateStatus.CheckingVersion
        
        // Send version query command
        serialManager.sendCommand(ProtocolCommands.GET_VERSION)
        serialManager.sendCommand(ProtocolCommands.GET_HARDWARE)
        serialManager.sendCommand(ProtocolCommands.GET_HEAP)
        
        // Wait for response (simplified - real implementation needs proper parsing)
        kotlinx.coroutines.delay(500)
        
        // Parse responses from serial data
        // This would be integrated with the protocol parser
        
        _updateStatus.value = UpdateStatus.Idle
        return _deviceVersion.value
    }
    
    /**
     * Check if update is available
     */
    suspend fun isUpdateAvailable(deviceVersion: DeviceVersion): Boolean {
        val hardwareType = HardwareType.fromIdentifier(deviceVersion.hardware)
        if (!firmwareManager.isFirmwareAvailable(hardwareType)) {
            return false
        }
        
        val availableVersion = firmwareManager.getFirmwareVersion()
        return VersionComparator.isUpdateAvailable(deviceVersion.version, availableVersion)
    }
    
    /**
     * Perform firmware update
     */
    suspend fun performUpdate(deviceVersion: DeviceVersion): Boolean {
        return try {
            val hardwareType = HardwareType.fromIdentifier(deviceVersion.hardware)
            
            // Get firmware stream
            val firmwareStream = firmwareManager.getFirmwareStream(hardwareType)
                ?: return false
            
            _updateStatus.value = UpdateStatus.Preparing
            
            // Notify device of incoming update
            serialManager.sendCommand(ProtocolCommands.START_UPDATE)
            kotlinx.coroutines.delay(1000) // Wait for device to prepare
            
            // Upload firmware
            uploadFirmware(firmwareStream)
            
            // Verify
            _updateStatus.value = UpdateStatus.Verifying
            serialManager.sendCommand(ProtocolCommands.UPDATE_VERIFY)
            kotlinx.coroutines.delay(2000)
            
            _updateStatus.value = UpdateStatus.Success
            true
        } catch (e: Exception) {
            Log.e(TAG, "Update failed", e)
            _updateStatus.value = UpdateStatus.Error(e.message ?: "Unknown error")
            false
        }
    }
    
    /**
     * Upload firmware in chunks
     */
    private suspend fun uploadFirmware(inputStream: InputStream) {
        inputStream.use { stream ->
            val buffer = ByteArray(CHUNK_SIZE)
            var totalSent = 0
            val totalSize = stream.available()
            
            while (true) {
                val read = stream.read(buffer)
                if (read <= 0) break
                
                // Send chunk
                serialManager.sendRawData(buffer, read)
                totalSent += read
                
                // Update progress
                _updateStatus.value = UpdateStatus.Uploading(totalSent, totalSize)
                
                // Small delay to avoid overwhelming serial buffer
                kotlinx.coroutines.delay(10)
            }
        }
        
        // Signal upload complete
        serialManager.sendCommand(ProtocolCommands.UPDATE_COMPLETE)
    }
    
    /**
     * Handle version response from device
     */
    fun handleVersionResponse(version: String, hardware: String, heap: Long = 0) {
        _deviceVersion.value = DeviceVersion(
            version = version,
            hardware = hardware,
            freeHeap = heap
        )
    }
}

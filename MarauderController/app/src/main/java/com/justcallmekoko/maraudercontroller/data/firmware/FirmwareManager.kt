package com.justcallmekoko.maraudercontroller.data.firmware

import android.content.Context
import com.justcallmekoko.maraudercontroller.data.protocol.FirmwareMetadata
import com.justcallmekoko.maraudercontroller.data.protocol.HardwareType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.security.MessageDigest

/**
 * Firmware Manager
 * Manages bundled firmware binaries and metadata
 */
class FirmwareManager(private val context: Context) {
    
    companion object {
        private const val FIRMWARE_DIR = "firmware"
        private const val FIRMWARE_VERSION = "v1.9.1" // Updated version with list headers fix
        
        /**
         * Firmware file mappings for each hardware type
         */
        private val FIRMWARE_MAPPINGS = mapOf(
            HardwareType.FLIPPER to "flipper/esp32_marauder.bin",
            HardwareType.M5STICKC to "m5stickc/esp32_marauder.bin",
            HardwareType.M5STICKCP2 to "m5stickcp2/esp32_marauder.bin",
            HardwareType.CARDPUTER to "cardputer/esp32_marauder.bin",
            HardwareType.MARAUDER_V4 to "marauder_v4/esp32_marauder.bin",
            HardwareType.MARAUDER_V6 to "marauder_v6/esp32_marauder.bin",
            HardwareType.MARAUDER_V7 to "marauder_v7/esp32_marauder.bin",
            HardwareType.MULTIBOARD_S3 to "multiboard_s3/esp32_marauder.bin",
            HardwareType.CYD_MICRO to "cyd_micro/esp32_marauder.bin",
            HardwareType.ESP32_C5 to "esp32c5/esp32_marauder.bin"
        )
    }
    
    /**
     * Get firmware metadata for hardware type
     */
    suspend fun getFirmwareMetadata(hardwareType: HardwareType): FirmwareMetadata? {
        return withContext(Dispatchers.IO) {
            val firmwarePath = FIRMWARE_MAPPINGS[hardwareType] ?: return@withContext null
            
            try {
                val inputStream = context.assets.open("$FIRMWARE_DIR/$firmwarePath")
                val size = inputStream.available().toLong()
                val checksum = calculateChecksum(inputStream)
                
                FirmwareMetadata(
                    version = FIRMWARE_VERSION,
                    hardwareTarget = hardwareType.identifier,
                    buildDate = "2024-12-27",
                    fileSize = size,
                    checksum = checksum
                )
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Get firmware input stream for hardware type
     */
    suspend fun getFirmwareStream(hardwareType: HardwareType): InputStream? {
        return withContext(Dispatchers.IO) {
            val firmwarePath = FIRMWARE_MAPPINGS[hardwareType] ?: return@withContext null
            
            try {
                context.assets.open("$FIRMWARE_DIR/$firmwarePath")
            } catch (e: Exception) {
                null
            }
        }
    }
    
    /**
     * Check if firmware is available for hardware type
     */
    fun isFirmwareAvailable(hardwareType: HardwareType): Boolean {
        return FIRMWARE_MAPPINGS.containsKey(hardwareType)
    }
    
    /**
     * Get firmware version
     */
    fun getFirmwareVersion(): String = FIRMWARE_VERSION
    
    /**
     * Calculate MD5 checksum of firmware
     */
    private fun calculateChecksum(inputStream: InputStream): String {
        val md = MessageDigest.getInstance("MD5")
        val buffer = ByteArray(8192)
        var read: Int
        
        inputStream.use { stream ->
            while (stream.read(buffer).also { read = it } > 0) {
                md.update(buffer, 0, read)
            }
        }
        
        return md.digest().joinToString("") { "%02x".format(it) }
    }
    
    /**
     * Get all supported hardware types with available firmware
     */
    fun getSupportedHardware(): List<HardwareType> {
        return FIRMWARE_MAPPINGS.keys.toList()
    }
}

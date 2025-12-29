package com.justcallmekoko.maraudercontroller.data.protocol

/**
 * Marauder Serial Protocol Layer
 * Provides seamless communication between Android app and ESP32 firmware
 */

/**
 * Protocol version for compatibility checking
 */
const val PROTOCOL_VERSION = "1.0.0"

/**
 * Special command prefixes for protocol control
 */
object ProtocolCommands {
    const val GET_VERSION = "#version"
    const val GET_HARDWARE = "#hardware"
    const val GET_HEAP = "#heap"
    const val START_UPDATE = "#update start"
    const val UPDATE_READY = "#update ready"
    const val UPDATE_DATA = "#update data"
    const val UPDATE_COMPLETE = "#update complete"
    const val UPDATE_VERIFY = "#update verify"
}

/**
 * Protocol responses from ESP32
 */
object ProtocolResponses {
    const val VERSION_PREFIX = "#VERSION:"
    const val HARDWARE_PREFIX = "#HARDWARE:"
    const val HEAP_PREFIX = "#HEAP:"
    const val UPDATE_ACK = "#UPDATE:ACK"
    const val UPDATE_READY_ACK = "#UPDATE:READY"
    const val UPDATE_PROGRESS = "#UPDATE:PROGRESS"
    const val UPDATE_SUCCESS = "#UPDATE:SUCCESS"
    const val UPDATE_ERROR = "#UPDATE:ERROR"
}

/**
 * Device information from ESP32
 */
data class DeviceVersion(
    val version: String,
    val hardware: String,
    val freeHeap: Long = 0,
    val uptime: Long = 0
)

/**
 * Firmware package metadata
 */
data class FirmwareMetadata(
    val version: String,
    val hardwareTarget: String,
    val buildDate: String,
    val fileSize: Long,
    val checksum: String
)

/**
 * Update status
 */
sealed class UpdateStatus {
    object Idle : UpdateStatus()
    object CheckingVersion : UpdateStatus()
    data class UpdateAvailable(val currentVersion: String, val newVersion: String) : UpdateStatus()
    object Preparing : UpdateStatus()
    data class Uploading(val progress: Int, val total: Int) : UpdateStatus()
    object Verifying : UpdateStatus()
    object Success : UpdateStatus()
    data class Error(val message: String) : UpdateStatus()
}

/**
 * Hardware types supported
 */
enum class HardwareType(val identifier: String, val displayName: String) {
    FLIPPER("MARAUDER_FLIPPER", "Flipper Zero WiFi Dev Board"),
    M5STICKC("MARAUDER_M5STICKC", "M5Stick-C Plus"),
    M5STICKCP2("MARAUDER_M5STICKCP2", "M5Stick-C Plus2"),
    CARDPUTER("MARAUDER_CARDPUTER", "M5 Cardputer"),
    MARAUDER_V4("MARAUDER_V4", "Marauder V4"),
    MARAUDER_V6("MARAUDER_V6", "Marauder V6"),
    MARAUDER_V7("MARAUDER_V7", "Marauder V7"),
    MULTIBOARD_S3("MARAUDER_MULTIBOARD_S3", "Flipper Multi-Board S3"),
    CYD_MICRO("MARAUDER_CYD_MICRO", "CYD 2.8\" Micro"),
    CYD_2USB("MARAUDER_CYD_2USB", "CYD 2.8\" 2-USB"),
    CYD_GUITION("MARAUDER_CYD_GUITION", "CYD 2.4\" Guition"),
    CYD_3_5_INCH("MARAUDER_CYD_3_5_INCH", "CYD 3.5\""),
    ESP32_C5("MARAUDER_C5", "ESP32-C5 DevKit"),
    GENERIC("GENERIC_ESP32", "Generic ESP32"),
    UNKNOWN("UNKNOWN", "Unknown Hardware");
    
    companion object {
        fun fromIdentifier(id: String): HardwareType {
            return values().find { it.identifier == id } ?: UNKNOWN
        }
    }
}

/**
 * Version comparison utility
 */
object VersionComparator {
    /**
     * Compare two version strings (e.g., "v1.9.0" vs "v1.10.0")
     * Returns: -1 if v1 < v2, 0 if equal, 1 if v1 > v2
     */
    fun compare(v1: String, v2: String): Int {
        val v1Parts = v1.removePrefix("v").split(".")
        val v2Parts = v2.removePrefix("v").split(".")
        
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        
        for (i in 0 until maxLength) {
            val v1Part = v1Parts.getOrNull(i)?.toIntOrNull() ?: 0
            val v2Part = v2Parts.getOrNull(i)?.toIntOrNull() ?: 0
            
            when {
                v1Part < v2Part -> return -1
                v1Part > v2Part -> return 1
            }
        }
        
        return 0
    }
    
    /**
     * Check if update is available
     */
    fun isUpdateAvailable(currentVersion: String, availableVersion: String): Boolean {
        return compare(currentVersion, availableVersion) < 0
    }
}

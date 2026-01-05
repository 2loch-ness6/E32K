package com.justcallmekoko.maraudercontroller.data.protocol

import kotlinx.serialization.Serializable

/**
 * Data models for ESP32 Marauder data structures
 */

@Serializable
data class AccessPoint(
    val ssid: String = "",
    val bssid: String = "",
    val channel: Int = 0,
    val rssi: Int = 0,
    val encryption: String = "",
    val selected: Boolean = false,
    val vendor: String = "",
    val beacon: Int = 0,
    val lastSeen: Long = 0
)

@Serializable
data class Station(
    val mac: String = "",
    val bssid: String = "",
    val rssi: Int = 0,
    val channel: Int = 0,
    val lastSeen: Long = 0,
    val packets: Int = 0,
    val vendor: String = ""
)

@Serializable
data class SSID(
    val name: String = "",
    val selected: Boolean = false,
    val channel: Int = 0
)

@Serializable
data class ScanResult(
    val type: ScanType,
    val aps: List<AccessPoint> = emptyList(),
    val stations: List<Station> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class ScanType {
    AP_SCAN,
    STATION_SCAN,
    ALL_SCAN,
    BEACON_SNIFF,
    PROBE_SNIFF,
    DEAUTH_SNIFF,
    PWN_SNIFF,
    PMKID_SNIFF,
    ESP_SNIFF,
    RAW_CAPTURE,
    WARDRIVE,
    BLUETOOTH
}

@Serializable
data class AttackConfig(
    val type: AttackType,
    val targetMac: String? = null,
    val targetSsid: String? = null,
    val random: Boolean = false,
    val timeout: Int? = null,
    val channel: Int? = null
)

enum class AttackType {
    DEAUTH,
    BEACON_SPAM,
    BEACON_LIST,
    PROBE,
    RICK_ROLL,
    AP_SPAM,
    MIMIC,
    EVIL_PORTAL,
    KARMA
}

@Serializable
data class DeviceInfo(
    val version: String = "",
    val hardware: String = "",
    val freeHeap: Int = 0,
    val uptime: Long = 0,
    val batteryLevel: Int? = null,
    val temperature: Float? = null
)

@Serializable
data class GpsData(
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val altitude: Double = 0.0,
    val speed: Double = 0.0,
    val satellites: Int = 0,
    val fix: Boolean = false,
    val accuracy: Double = 0.0,
    val timestamp: String = ""
)

@Serializable
data class PacketStats(
    val beacon: Int = 0,
    val probe: Int = 0,
    val deauth: Int = 0,
    val eapol: Int = 0,
    val data: Int = 0,
    val total: Int = 0
)

@Serializable
data class BluetoothDevice(
    val address: String = "",
    val name: String = "",
    val rssi: Int = 0,
    val type: String = "",
    val services: List<String> = emptyList(),
    val manufacturer: String = "",
    val lastSeen: Long = 0
)

@Serializable
data class ScriptConfig(
    val name: String = "",
    val commands: List<String> = emptyList(),
    val delay: Int = 1000,
    val repeat: Boolean = false,
    val repeatCount: Int = 1
)

sealed class MarauderResponse {
    data class Success(val message: String) : MarauderResponse()
    data class Error(val message: String) : MarauderResponse()
    data class AccessPointList(val aps: List<AccessPoint>) : MarauderResponse()
    data class StationList(val stations: List<Station>) : MarauderResponse()
    data class SsidList(val ssids: List<SSID>) : MarauderResponse()
    data class BluetoothDeviceList(val devices: List<BluetoothDevice>) : MarauderResponse()
    data class Info(val info: DeviceInfo) : MarauderResponse()
    data class GpsInfo(val gps: GpsData) : MarauderResponse()
    data class PacketCount(val stats: PacketStats) : MarauderResponse()
    data class ScanStarted(val scanType: String) : MarauderResponse()
    data class ScanStopped(val scanType: String) : MarauderResponse()
    data class AttackStarted(val attackType: String) : MarauderResponse()
    data class AttackStopped(val attackType: String) : MarauderResponse()
    data class RawOutput(val line: String) : MarauderResponse()
    // Protocol responses for seamless communication
    data class VersionInfo(val version: String) : MarauderResponse()
    data class HardwareInfo(val hardware: String) : MarauderResponse()
    data class HeapInfo(val freeHeap: Long) : MarauderResponse()
    data class DeviceVersionInfo(val version: String, val hardware: String, val freeHeap: Long) : MarauderResponse()
    object Prompt : MarauderResponse()
}

/**
 * Download progress tracking for file transfers
 */
data class DownloadProgress(
    val filename: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val progress: Float, // 0.0 to 1.0
    val isComplete: Boolean = false,
    val error: String? = null
) {
    val progressPercentage: Int
        get() = (progress * 100).toInt()
}

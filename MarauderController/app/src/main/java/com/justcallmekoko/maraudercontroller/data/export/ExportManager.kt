package com.justcallmekoko.maraudercontroller.data.export

import android.content.Context
import com.justcallmekoko.maraudercontroller.data.protocol.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages data export functionality
 */
class ExportManager(private val context: Context) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    enum class ExportFormat {
        JSON, CSV, TXT, WIGLE
    }
    
    /**
     * Export access points to file
     */
    suspend fun exportAccessPoints(
        accessPoints: List<AccessPoint>,
        format: ExportFormat = ExportFormat.JSON,
        exportPath: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(exportPath)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "access_points_$timestamp.${format.name.lowercase()}"
            val file = File(exportDir, fileName)
            
            val content = when (format) {
                ExportFormat.JSON -> json.encodeToString(accessPoints)
                ExportFormat.CSV -> convertAccessPointsToCsv(accessPoints)
                ExportFormat.TXT -> convertAccessPointsToText(accessPoints)
                ExportFormat.WIGLE -> convertAccessPointsToWigle(accessPoints)
            }
            
            file.writeText(content)
            file
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export stations to file
     */
    suspend fun exportStations(
        stations: List<Station>,
        format: ExportFormat = ExportFormat.JSON,
        exportPath: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(exportPath)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "stations_$timestamp.${format.name.lowercase()}"
            val file = File(exportDir, fileName)
            
            val content = when (format) {
                ExportFormat.JSON -> json.encodeToString(stations)
                ExportFormat.CSV -> convertStationsToCsv(stations)
                ExportFormat.TXT -> convertStationsToText(stations)
                else -> json.encodeToString(stations)
            }
            
            file.writeText(content)
            file
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export terminal logs to file
     */
    suspend fun exportTerminalLog(
        logs: List<String>,
        exportPath: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(exportPath)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "terminal_log_$timestamp.txt"
            val file = File(exportDir, fileName)
            
            file.writeText(logs.joinToString("\n"))
            file
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export packet statistics to file
     */
    suspend fun exportPacketStats(
        stats: PacketStats,
        exportPath: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(exportPath)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "packet_stats_$timestamp.json"
            val file = File(exportDir, fileName)
            
            file.writeText(json.encodeToString(stats))
            file
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Export wardriving data in WiGLE format
     */
    suspend fun exportWardrivingData(
        accessPoints: List<AccessPoint>,
        gpsData: GpsData?,
        exportPath: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val exportDir = File(exportPath)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "wardriving_$timestamp.csv"
            val file = File(exportDir, fileName)
            
            val content = convertToWigleFormat(accessPoints, gpsData)
            file.writeText(content)
            file
        } catch (e: Exception) {
            null
        }
    }
    
    private fun convertAccessPointsToCsv(aps: List<AccessPoint>): String {
        val header = "SSID,BSSID,Channel,RSSI,Encryption,Vendor,Beacons\n"
        val rows = aps.joinToString("\n") { ap ->
            "${ap.ssid},${ap.bssid},${ap.channel},${ap.rssi},${ap.encryption},${ap.vendor},${ap.beacon}"
        }
        return header + rows
    }
    
    private fun convertAccessPointsToText(aps: List<AccessPoint>): String {
        return aps.joinToString("\n\n") { ap ->
            """
            SSID: ${ap.ssid}
            BSSID: ${ap.bssid}
            Channel: ${ap.channel}
            Signal: ${ap.rssi} dBm
            Encryption: ${ap.encryption}
            Vendor: ${ap.vendor}
            Beacons: ${ap.beacon}
            """.trimIndent()
        }
    }
    
    private fun convertStationsToCsv(stations: List<Station>): String {
        val header = "MAC,RSSI,Channel,Packets,Vendor\n"
        val rows = stations.joinToString("\n") { station ->
            "${station.mac},${station.rssi},${station.channel},${station.packets},${station.vendor}"
        }
        return header + rows
    }
    
    private fun convertStationsToText(stations: List<Station>): String {
        return stations.joinToString("\n\n") { station ->
            """
            MAC: ${station.mac}
            Signal: ${station.rssi} dBm
            Channel: ${station.channel}
            Packets: ${station.packets}
            Vendor: ${station.vendor}
            """.trimIndent()
        }
    }
    
    private fun convertAccessPointsToWigle(aps: List<AccessPoint>): String {
        // Basic WiGLE CSV format (simplified)
        val header = "MAC,SSID,AuthMode,FirstSeen,Channel,RSSI,CurrentLatitude,CurrentLongitude,AltitudeMeters,AccuracyMeters,Type\n"
        val rows = aps.joinToString("\n") { ap ->
            "${ap.bssid},${ap.ssid},${ap.encryption},${Date()},${ap.channel},${ap.rssi},0.0,0.0,0.0,0.0,WIFI"
        }
        return header + rows
    }
    
    private fun convertToWigleFormat(aps: List<AccessPoint>, gps: GpsData?): String {
        val header = "MAC,SSID,AuthMode,FirstSeen,Channel,RSSI,CurrentLatitude,CurrentLongitude,AltitudeMeters,AccuracyMeters,Type\n"
        val lat = gps?.latitude ?: 0.0
        val lon = gps?.longitude ?: 0.0
        val alt = gps?.altitude ?: 0.0
        
        val rows = aps.joinToString("\n") { ap ->
            "${ap.bssid},${ap.ssid},${ap.encryption},${Date()},${ap.channel},${ap.rssi},$lat,$lon,$alt,10.0,WIFI"
        }
        return header + rows
    }
}

package com.justcallmekoko.maraudercontroller.data.protocol

import com.justcallmekoko.maraudercontroller.utils.ParserLogger
import com.justcallmekoko.maraudercontroller.utils.AndroidParserLogger

/**
 * Parses serial output from ESP32 Marauder device
 */
class MarauderProtocolParser(
    private val logger: ParserLogger = AndroidParserLogger()
) {
    
    companion object {
        private const val TAG = "MarauderParser"
        
        // Response patterns
        private val ANSI_PATTERN = Regex("""\u001B\[[;\d]*[mK]""")
        private val PROMPT_PATTERN = Regex("^>\\s*$")
        private val COMMAND_ECHO_PATTERN = Regex("^#(.+)$")
        private val AP_LINE_PATTERN = Regex("""\[(\d+)\]\s*(.+?)\s*\((.+?)\)\s*Ch:\s*(\d+)\s*RSSI:\s*(-?\d+)\s*(.+)""")
        private val STATION_LINE_PATTERN = Regex("""\[(\d+)\]\s*(.+?)\s*RSSI:\s*(-?\d+)\s*Ch:\s*(\d+)\s*Pkts:\s*(\d+)""")
        private val SSID_LINE_PATTERN = Regex("""\[(\d+)\]\s*(.+?)(?:\s*Ch:\s*(\d+))?""")
        private val SCAN_START_PATTERN = Regex("""Starting\s+(.+?)\s+scan""", RegexOption.IGNORE_CASE)
        private val SCAN_STOP_PATTERN = Regex("""Stopping\s+(.+?)""", RegexOption.IGNORE_CASE)
        private val ATTACK_START_PATTERN = Regex("""Starting\s+(.+?)\s+attack""", RegexOption.IGNORE_CASE)
        private val ATTACK_STOP_PATTERN = Regex("""Stopped\s+(.+?)\s+attack""", RegexOption.IGNORE_CASE)
        private val GPS_FIX_PATTERN = Regex("""Fix:\s*(\w+)""")
        private val GPS_SATS_PATTERN = Regex("""Sats:\s*(\d+)""")
        private val GPS_LAT_PATTERN = Regex("""Lat:\s*([-\d.]+)""")
        private val GPS_LON_PATTERN = Regex("""Lon:\s*([-\d.]+)""")
        private val GPS_ALT_PATTERN = Regex("""Alt:\s*([-\d.]+)""")
        private val GPS_ACCURACY_PATTERN = Regex("""Accuracy:\s*([-\d.]+)""")
        private val CHANNEL_PATTERN = Regex("""(?:Current|Set)\s+channel:\s*(\d+)""")
        private val VERSION_PATTERN = Regex("""ESP32\s+Marauder\s+(v[\d.]+)""")
        private val PACKET_COUNT_PATTERN = Regex("""(\w+):\s*(\d+)""")
    }
    
    private val currentGpsData = GpsData()

    private fun stripAnsi(text: String): String {
        return text.replace(ANSI_PATTERN, "").replace(Regex("""\x1B(?:[@-Z\\-_]|\[[0-?]*[ -/]*[@-~])"""), "")
    }
    
    /**
     * Parse a line of serial output
     */
    fun parseLine(line: String): MarauderResponse {
        val trimmed = stripAnsi(line).trim()
        
        if (trimmed.isEmpty()) {
            return MarauderResponse.RawOutput(line)
        }
        
        // Check for prompt
        if (PROMPT_PATTERN.matches(trimmed)) {
            return MarauderResponse.Prompt
        }
        
        // Check for command echo (lines starting with #)
        COMMAND_ECHO_PATTERN.find(trimmed)?.let {
            return MarauderResponse.RawOutput(it.groupValues[1])
        }
        
        // Check for scan start
        SCAN_START_PATTERN.find(trimmed)?.let {
            return MarauderResponse.ScanStarted(it.groupValues[1])
        }
        
        // Check for scan stop
        SCAN_STOP_PATTERN.find(trimmed)?.let {
            return MarauderResponse.ScanStopped(it.groupValues[1])
        }
        
        // Check for attack start
        ATTACK_START_PATTERN.find(trimmed)?.let {
            return MarauderResponse.AttackStarted(it.groupValues[1])
        }
        
        // Check for attack stop
        ATTACK_STOP_PATTERN.find(trimmed)?.let {
            return MarauderResponse.AttackStopped(it.groupValues[1])
        }
        
        // Check for channel info
        CHANNEL_PATTERN.find(trimmed)?.let {
            return MarauderResponse.Success("Channel: ${it.groupValues[1]}")
        }
        
        // Check for version info
        VERSION_PATTERN.find(trimmed)?.let {
            val version = it.groupValues[1]
            return MarauderResponse.Info(DeviceInfo(version = version))
        }
        
        // Check for GPS data
        if (parseGpsLine(trimmed)) {
            return MarauderResponse.GpsInfo(currentGpsData.copy())
        }
        
        // Default: raw output
        return MarauderResponse.RawOutput(trimmed)
    }
    
    /**
     * Parse access point list
     */
    fun parseAccessPointList(lines: List<String>): List<AccessPoint> {
        val aps = mutableListOf<AccessPoint>()
        
        for (line in lines) {
            val cleanLine = stripAnsi(line).trim()
            val match = AP_LINE_PATTERN.find(cleanLine) ?: continue
            
            try {
                val index = match.groupValues[1].toInt()
                val ssid = match.groupValues[2].trim()
                val bssid = match.groupValues[3].trim()
                val channel = match.groupValues[4].toInt()
                val rssi = match.groupValues[5].toInt()
                var encryption = match.groupValues[6].trim()
                
                // Check if selected and remove the marker from encryption string
                val selected = cleanLine.contains("*") || cleanLine.contains("(*)") 
                encryption = encryption.replace("(*)", "").trim()
                
                aps.add(
                    AccessPoint(
                        ssid = ssid,
                        bssid = bssid,
                        channel = channel,
                        rssi = rssi,
                        encryption = encryption,
                        selected = selected
                    )
                )
            } catch (e: Exception) {
                logger.w(TAG, "Failed to parse AP line: $line", e)
            }
        }
        
        return aps
    }
    
    /**
     * Parse station list
     */
    fun parseStationList(lines: List<String>): List<Station> {
        val stations = mutableListOf<Station>()
        
        for (line in lines) {
            val cleanLine = stripAnsi(line).trim()
            val match = STATION_LINE_PATTERN.find(cleanLine) ?: continue
            
            try {
                val mac = match.groupValues[2].trim()
                val rssi = match.groupValues[3].toInt()
                val channel = match.groupValues[4].toInt()
                val packets = match.groupValues[5].toInt()
                
                stations.add(
                    Station(
                        mac = mac,
                        rssi = rssi,
                        channel = channel,
                        packets = packets
                    )
                )
            } catch (e: Exception) {
                logger.w(TAG, "Failed to parse station line: $line", e)
            }
        }
        
        return stations
    }
    
    /**
     * Parse SSID list
     */
    fun parseSsidList(lines: List<String>): List<SSID> {
        val ssids = mutableListOf<SSID>()
        
        for (line in lines) {
            val cleanLine = stripAnsi(line).trim()
            val match = SSID_LINE_PATTERN.find(cleanLine) ?: continue
            
            try {
                val name = match.groupValues[2].trim()
                val channel = match.groupValues[3].toIntOrNull() ?: 0
                val selected = cleanLine.contains("*") || cleanLine.contains("(*)")
                
                ssids.add(
                    SSID(
                        name = name,
                        channel = channel,
                        selected = selected
                    )
                )
            } catch (e: Exception) {
                logger.w(TAG, "Failed to parse SSID line: $line", e)
            }
        }
        
        return ssids
    }
    
    /**
     * Parse GPS data line
     */
    private fun parseGpsLine(line: String): Boolean {
        var parsed = false
        
        GPS_FIX_PATTERN.find(line)?.let {
            // currentGpsData.fix = it.groupValues[1].equals("true", ignoreCase = true)
            parsed = true
        }
        
        GPS_SATS_PATTERN.find(line)?.let {
            // currentGpsData.satellites = it.groupValues[1].toIntOrNull() ?: 0
            parsed = true
        }
        
        GPS_LAT_PATTERN.find(line)?.let {
            // currentGpsData.latitude = it.groupValues[1].toDoubleOrNull() ?: 0.0
            parsed = true
        }
        
        GPS_LON_PATTERN.find(line)?.let {
            // currentGpsData.longitude = it.groupValues[1].toDoubleOrNull() ?: 0.0
            parsed = true
        }
        
        GPS_ALT_PATTERN.find(line)?.let {
            // currentGpsData.altitude = it.groupValues[1].toDoubleOrNull() ?: 0.0
            parsed = true
        }
        
        GPS_ACCURACY_PATTERN.find(line)?.let {
            // currentGpsData.accuracy = it.groupValues[1].toDoubleOrNull() ?: 0.0
            parsed = true
        }
        
        return parsed
    }
    
    /**
     * Parse packet count statistics
     */
    fun parsePacketCount(lines: List<String>): PacketStats {
        var beacon = 0
        var probe = 0
        var deauth = 0
        var eapol = 0
        var data = 0
        
        for (line in lines) {
            val matches = PACKET_COUNT_PATTERN.findAll(line)
            for (match in matches) {
                val type = match.groupValues[1].lowercase()
                val count = match.groupValues[2].toIntOrNull() ?: 0
                
                when {
                    type.contains("beacon") -> beacon = count
                    type.contains("probe") -> probe = count
                    type.contains("deauth") -> deauth = count
                    type.contains("eapol") -> eapol = count
                    type.contains("data") -> data = count
                }
            }
        }
        
        return PacketStats(
            beacon = beacon,
            probe = probe,
            deauth = deauth,
            eapol = eapol,
            data = data,
            total = beacon + probe + deauth + eapol + data
        )
    }
}

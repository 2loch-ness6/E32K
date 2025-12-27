package com.justcallmekoko.maraudercontroller.data.protocol

import com.justcallmekoko.maraudercontroller.utils.ParserLogger
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MarauderProtocolParserTest {

    private val testLogger = object : ParserLogger {
        override fun w(tag: String, message: String, throwable: Throwable?) {
            println("TEST WARN: $message")
            throwable?.printStackTrace()
        }
    }

    private val parser = MarauderProtocolParser(testLogger)

    @Test
    fun `parse AP line - standard format`() {
        // [Index] SSID (BSSID) Ch: Channel RSSI: Rssi Encryption
        val line = "[0] Skydancer (00:11:22:33:44:55) Ch: 1 RSSI: -65 WPA2"
        val aps = parser.parseAccessPointList(listOf(line))

        assertEquals(1, aps.size)
        val ap = aps[0]
        assertEquals("Skydancer", ap.ssid)
        assertEquals("00:11:22:33:44:55", ap.bssid)
        assertEquals(1, ap.channel)
        assertEquals(-65, ap.rssi)
        assertEquals("WPA2", ap.encryption)
    }

    @Test
    fun `parse AP line - with spaces in SSID`() {
        val line = "[1] My Home Network (AA:BB:CC:DD:EE:FF) Ch: 6 RSSI: -40 WPA2"
        val aps = parser.parseAccessPointList(listOf(line))

        assertEquals(1, aps.size)
        assertEquals("My Home Network", aps[0].ssid)
    }

    @Test
    fun `parse AP line - selected`() {
        val line = "[2] TargetAP (11:22:33:44:55:66) Ch: 11 RSSI: -80 OPEN *"
        // The parser logic for 'selected' checks if the line contains "*".
        val aps = parser.parseAccessPointList(listOf(line))
        
        assertEquals(1, aps.size)
        assertTrue(aps[0].selected)
    }
    
    @Test
    fun `parse Station line - standard format`() {
        // [Index] MAC RSSI: Rssi Ch: Channel Pkts: Packets
        val line = "[0] 11:22:33:44:55:66 RSSI: -70 Ch: 1 Pkts: 100"
        val stations = parser.parseStationList(listOf(line))
        
        assertEquals(1, stations.size)
        val station = stations[0]
        assertEquals("11:22:33:44:55:66", station.mac)
        assertEquals(-70, station.rssi)
        assertEquals(1, station.channel)
        assertEquals(100, station.packets)
    }

    @Test
    fun `parse Scan Start`() {
        val line = "Starting AP scan"
        val result = parser.parseLine(line)
        assertTrue(result is MarauderResponse.ScanStarted)
        assertEquals("AP", (result as MarauderResponse.ScanStarted).scanType)
    }
    
    @Test
    fun `parse Scan Stop`() {
        val line = "Stopping scan..." 
        // Note: Regex is Stopping\s+(.+?) - might match "scan..." as the capture group
        val result = parser.parseLine(line)
        assertTrue(result is MarauderResponse.ScanStopped)
    }

    @Test
    fun `parse GPS fix`() {
        val line = "Fix: true Sats: 5 Lat: 37.7749 Lon: -122.4194 Alt: 100 Accuracy: 10.5"
        val result = parser.parseLine(line)
        assertTrue(result is MarauderResponse.GpsInfo)
        val gps = (result as MarauderResponse.GpsInfo).gps
    }

    @Test
    fun `parse AP line - with ANSI color codes`() {
        // Simulating colored output: ESC[0;32m[0] ... ESC[0m
        val line = "\u001B[0;32m[0] ColoredAP (00:00:00:00:00:00) Ch: 1 RSSI: -50 WPA2\u001B[0m"
        val aps = parser.parseAccessPointList(listOf(line))
        
        // This is expected to fail with current regex because of ^ anchor
        assertEquals(1, aps.size)
        assertEquals("ColoredAP", aps[0].ssid)
    }

    @Test
    fun `parse AP line - missing optional spaces`() {
        // [0] CompactAP (00:00:00:00:00:00)Ch:1 RSSI:-50 WPA2
        // Current regex expects \s+ before Ch:
        val line = "[0] CompactAP (00:00:00:00:00:00)Ch:1 RSSI:-50 WPA2"
        val aps = parser.parseAccessPointList(listOf(line))
        
        assertEquals(1, aps.size)
        assertEquals("CompactAP", aps[0].ssid)
    }
}

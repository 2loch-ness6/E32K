package com.justcallmekoko.maraudercontroller.data.session

import android.content.Context
import com.justcallmekoko.maraudercontroller.data.protocol.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

/**
 * Manages session data persistence
 */
class SessionManager(private val context: Context) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val sessionDir = File(context.filesDir, "sessions")
    
    init {
        if (!sessionDir.exists()) {
            sessionDir.mkdirs()
        }
    }
    
    @Serializable
    data class SessionData(
        val timestamp: Long = System.currentTimeMillis(),
        val deviceInfo: DeviceInfo? = null,
        val accessPoints: List<AccessPoint> = emptyList(),
        val stations: List<Station> = emptyList(),
        val ssids: List<SSID> = emptyList(),
        val gpsData: GpsData? = null,
        val packetStats: PacketStats? = null,
        val attackConfigs: List<AttackConfig> = emptyList(),
        val terminalLog: List<String> = emptyList(),
        val notes: String = ""
    )
    
    /**
     * Save current session data
     */
    suspend fun saveSession(
        sessionName: String? = null,
        deviceInfo: DeviceInfo? = null,
        accessPoints: List<AccessPoint> = emptyList(),
        stations: List<Station> = emptyList(),
        ssids: List<SSID> = emptyList(),
        gpsData: GpsData? = null,
        packetStats: PacketStats? = null,
        attackConfigs: List<AttackConfig> = emptyList(),
        terminalLog: List<String> = emptyList(),
        notes: String = ""
    ): String = withContext(Dispatchers.IO) {
        val timestamp = System.currentTimeMillis()
        val name = sessionName ?: generateSessionName(timestamp)
        
        val sessionData = SessionData(
            timestamp = timestamp,
            deviceInfo = deviceInfo,
            accessPoints = accessPoints,
            stations = stations,
            ssids = ssids,
            gpsData = gpsData,
            packetStats = packetStats,
            attackConfigs = attackConfigs,
            terminalLog = terminalLog,
            notes = notes
        )
        
        val file = File(sessionDir, "$name.json")
        file.writeText(json.encodeToString(sessionData))
        
        name
    }
    
    /**
     * Load session data by name
     */
    suspend fun loadSession(sessionName: String): SessionData? = withContext(Dispatchers.IO) {
        try {
            val file = File(sessionDir, "$sessionName.json")
            if (file.exists()) {
                val content = file.readText()
                json.decodeFromString<SessionData>(content)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * List all saved sessions
     */
    suspend fun listSessions(): List<Pair<String, Long>> = withContext(Dispatchers.IO) {
        sessionDir.listFiles { file -> file.extension == "json" }
            ?.map { file ->
                val name = file.nameWithoutExtension
                val timestamp = file.lastModified()
                name to timestamp
            }
            ?.sortedByDescending { it.second }
            ?: emptyList()
    }
    
    /**
     * Delete session by name
     */
    suspend fun deleteSession(sessionName: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(sessionDir, "$sessionName.json")
        file.exists() && file.delete()
    }
    
    /**
     * Export session to external storage
     */
    suspend fun exportSession(
        sessionName: String,
        exportPath: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val sourceFile = File(sessionDir, "$sessionName.json")
            if (!sourceFile.exists()) return@withContext false
            
            val exportDir = File(exportPath)
            if (!exportDir.exists()) {
                exportDir.mkdirs()
            }
            
            val destFile = File(exportDir, "$sessionName.json")
            sourceFile.copyTo(destFile, overwrite = true)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Get most recent session
     */
    suspend fun getMostRecentSession(): SessionData? = withContext(Dispatchers.IO) {
        val sessions = listSessions()
        if (sessions.isEmpty()) return@withContext null
        
        val mostRecent = sessions.first()
        loadSession(mostRecent.first)
    }
    
    private fun generateSessionName(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        return "session_${dateFormat.format(Date(timestamp))}"
    }
}

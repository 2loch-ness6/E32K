package com.justcallmekoko.maraudercontroller.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Manages app preferences using DataStore
 */
class PreferencesManager(private val context: Context) {
    
    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "marauder_preferences")
    
    companion object {
        private val THEME_MODE = stringPreferencesKey("theme_mode")
        private val LANGUAGE = stringPreferencesKey("language")
        private val AUTO_CONNECT = booleanPreferencesKey("auto_connect")
        private val LAST_DEVICE_NAME = stringPreferencesKey("last_device_name")
        private val SAVE_SESSION_DATA = booleanPreferencesKey("save_session_data")
        private val EXPORT_PATH = stringPreferencesKey("export_path")
        private val GPS_ENABLED = booleanPreferencesKey("gps_enabled")
        private val BLUETOOTH_ENABLED = booleanPreferencesKey("bluetooth_enabled")
        private val AUTO_EXPORT = booleanPreferencesKey("auto_export")
        private val PACKET_VISUALIZATION = booleanPreferencesKey("packet_visualization")
        private val SCRIPTING_ENABLED = booleanPreferencesKey("scripting_enabled")
    }
    
    enum class ThemeMode {
        LIGHT, DARK, SYSTEM
    }
    
    // Theme mode
    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data.map { preferences ->
        when (preferences[THEME_MODE]) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }
    
    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[THEME_MODE] = when (mode) {
                ThemeMode.LIGHT -> "light"
                ThemeMode.DARK -> "dark"
                ThemeMode.SYSTEM -> "system"
            }
        }
    }
    
    // Language
    val languageFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[LANGUAGE] ?: "en"
    }
    
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[LANGUAGE] = language
        }
    }
    
    // Auto connect
    val autoConnectFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_CONNECT] ?: false
    }
    
    suspend fun setAutoConnect(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_CONNECT] = enabled
        }
    }
    
    // Last device
    val lastDeviceNameFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[LAST_DEVICE_NAME]
    }
    
    suspend fun setLastDeviceName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[LAST_DEVICE_NAME] = name
        }
    }
    
    // Session data
    val saveSessionDataFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SAVE_SESSION_DATA] ?: true
    }
    
    suspend fun setSaveSessionData(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SAVE_SESSION_DATA] = enabled
        }
    }
    
    // Export path
    val exportPathFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[EXPORT_PATH] ?: "/sdcard/MarauderController"
    }
    
    suspend fun setExportPath(path: String) {
        context.dataStore.edit { preferences ->
            preferences[EXPORT_PATH] = path
        }
    }
    
    // GPS enabled
    val gpsEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[GPS_ENABLED] ?: false
    }
    
    suspend fun setGpsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[GPS_ENABLED] = enabled
        }
    }
    
    // Bluetooth enabled
    val bluetoothEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[BLUETOOTH_ENABLED] ?: false
    }
    
    suspend fun setBluetoothEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[BLUETOOTH_ENABLED] = enabled
        }
    }
    
    // Auto export
    val autoExportFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[AUTO_EXPORT] ?: false
    }
    
    suspend fun setAutoExport(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[AUTO_EXPORT] = enabled
        }
    }
    
    // Packet visualization
    val packetVisualizationFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[PACKET_VISUALIZATION] ?: true
    }
    
    suspend fun setPacketVisualization(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PACKET_VISUALIZATION] = enabled
        }
    }
    
    // Scripting enabled
    val scriptingEnabledFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SCRIPTING_ENABLED] ?: false
    }
    
    suspend fun setScriptingEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SCRIPTING_ENABLED] = enabled
        }
    }
}

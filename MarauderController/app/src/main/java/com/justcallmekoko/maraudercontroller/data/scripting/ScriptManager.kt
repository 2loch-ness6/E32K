package com.justcallmekoko.maraudercontroller.data.scripting

import android.content.Context
import com.justcallmekoko.maraudercontroller.data.protocol.ScriptConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Manages automation scripts for Marauder commands
 */
class ScriptManager(private val context: Context) {
    
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    private val scriptsDir = File(context.filesDir, "scripts")
    
    init {
        if (!scriptsDir.exists()) {
            scriptsDir.mkdirs()
        }
    }
    
    /**
     * Save a script configuration
     */
    suspend fun saveScript(script: ScriptConfig): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(scriptsDir, "${script.name}.json")
            file.writeText(json.encodeToString(script))
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Load a script by name
     */
    suspend fun loadScript(name: String): ScriptConfig? = withContext(Dispatchers.IO) {
        try {
            val file = File(scriptsDir, "$name.json")
            if (file.exists()) {
                json.decodeFromString<ScriptConfig>(file.readText())
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * List all available scripts
     */
    suspend fun listScripts(): List<String> = withContext(Dispatchers.IO) {
        scriptsDir.listFiles { file -> file.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()
    }
    
    /**
     * Delete a script
     */
    suspend fun deleteScript(name: String): Boolean = withContext(Dispatchers.IO) {
        val file = File(scriptsDir, "$name.json")
        file.exists() && file.delete()
    }
    
    /**
     * Execute a script
     */
    suspend fun executeScript(
        script: ScriptConfig,
        onCommand: suspend (String) -> Unit,
        onProgress: (Int, Int) -> Unit = { _, _ -> }
    ) = withContext(Dispatchers.Default) {
        val repeatCount = if (script.repeat) script.repeatCount else 1
        
        for (iteration in 1..repeatCount) {
            for ((index, command) in script.commands.withIndex()) {
                onCommand(command)
                onProgress(index + 1, script.commands.size)
                
                if (index < script.commands.size - 1) {
                    delay(script.delay.toLong())
                }
            }
            
            if (iteration < repeatCount) {
                delay(script.delay.toLong())
            }
        }
    }
    
    /**
     * Create example scripts
     */
    suspend fun createExampleScripts() {
        val examples = listOf(
            ScriptConfig(
                name = "Quick WiFi Scan",
                commands = listOf(
                    "scanap",
                    "list -a"
                ),
                delay = 3000,
                repeat = false
            ),
            ScriptConfig(
                name = "Wardrive Mode",
                commands = listOf(
                    "scanap -c",
                    "gps"
                ),
                delay = 5000,
                repeat = true,
                repeatCount = 10
            ),
            ScriptConfig(
                name = "Deauth All Nearby",
                commands = listOf(
                    "scanap",
                    "select -a",
                    "attack -t deauth"
                ),
                delay = 2000,
                repeat = false
            ),
            ScriptConfig(
                name = "Beacon Spam Random",
                commands = listOf(
                    "ssid -g 20",
                    "attack -t beacon"
                ),
                delay = 1000,
                repeat = false
            )
        )
        
        examples.forEach { saveScript(it) }
    }
}

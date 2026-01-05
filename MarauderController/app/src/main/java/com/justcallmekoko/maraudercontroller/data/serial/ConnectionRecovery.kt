package com.justcallmekoko.maraudercontroller.data.serial

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Manages automatic reconnection and error recovery for serial connections
 */
class ConnectionRecoveryManager(
    private val reconnectAttempts: Int = 3,
    private val reconnectDelayMs: Long = 2000,
    private val healthCheckIntervalMs: Long = 10000
) {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    
    private val _recoveryState = MutableStateFlow<RecoveryState>(RecoveryState.Idle)
    val recoveryState: StateFlow<RecoveryState> = _recoveryState.asStateFlow()
    
    private var healthCheckJob: Job? = null
    private var reconnectJob: Job? = null
    
    sealed class RecoveryState {
        object Idle : RecoveryState()
        data class Reconnecting(val attempt: Int, val maxAttempts: Int) : RecoveryState()
        data class HealthCheck(val lastCheckTime: Long) : RecoveryState()
        data class Failed(val reason: String) : RecoveryState()
    }
    
    /**
     * Start periodic health checks
     */
    fun startHealthCheck(checkConnection: suspend () -> Boolean) {
        healthCheckJob?.cancel()
        healthCheckJob = scope.launch {
            while (isActive) {
                delay(healthCheckIntervalMs)
                
                _recoveryState.value = RecoveryState.HealthCheck(System.currentTimeMillis())
                
                val isHealthy = try {
                    withTimeout(5000) {
                        checkConnection()
                    }
                } catch (e: Exception) {
                    false
                }
                
                if (!isHealthy) {
                    // Connection lost, trigger recovery
                    _recoveryState.value = RecoveryState.Failed("Health check failed")
                }
            }
        }
    }
    
    /**
     * Stop health checks
     */
    fun stopHealthCheck() {
        healthCheckJob?.cancel()
        healthCheckJob = null
        _recoveryState.value = RecoveryState.Idle
    }
    
    /**
     * Attempt to reconnect with exponential backoff
     */
    suspend fun attemptReconnect(reconnectAction: suspend () -> Boolean): Boolean {
        reconnectJob?.cancel()
        
        return withContext(scope.coroutineContext) {
            for (attempt in 1..reconnectAttempts) {
                _recoveryState.value = RecoveryState.Reconnecting(attempt, reconnectAttempts)
                
                try {
                    val success = reconnectAction()
                    if (success) {
                        _recoveryState.value = RecoveryState.Idle
                        return@withContext true
                    }
                } catch (e: Exception) {
                    // Log error but continue trying
                }
                
                // Exponential backoff: 2s, 4s, 8s, etc.
                if (attempt < reconnectAttempts) {
                    val delayTime = reconnectDelayMs * (1 shl (attempt - 1))
                    delay(delayTime)
                }
            }
            
            _recoveryState.value = RecoveryState.Failed("Reconnection failed after $reconnectAttempts attempts")
            false
        }
    }
    
    /**
     * Cancel any ongoing recovery operations
     */
    fun cancelRecovery() {
        reconnectJob?.cancel()
        healthCheckJob?.cancel()
        _recoveryState.value = RecoveryState.Idle
    }
    
    /**
     * Release resources
     */
    fun release() {
        cancelRecovery()
        scope.cancel()
    }
}

/**
 * Error recovery strategies for command failures
 */
class CommandRetryManager(
    private val maxRetries: Int = 3,
    private val retryDelayMs: Long = 500
) {
    
    /**
     * Execute a command with automatic retry on failure
     */
    suspend fun <T> executeWithRetry(
        command: suspend () -> T,
        shouldRetry: (Exception) -> Boolean = { true }
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val result = command()
                return Result.success(result)
            } catch (e: Exception) {
                lastException = e
                
                if (!shouldRetry(e) || attempt == maxRetries - 1) {
                    return Result.failure(e)
                }
                
                // Exponential backoff
                val delayTime = retryDelayMs * (1 shl attempt)
                delay(delayTime)
            }
        }
        
        return Result.failure(lastException ?: Exception("Unknown error"))
    }
    
    /**
     * Execute a command with timeout and retry
     */
    suspend fun <T> executeWithTimeoutAndRetry(
        timeoutMs: Long,
        command: suspend () -> T,
        shouldRetry: (Exception) -> Boolean = { true }
    ): Result<T> {
        return executeWithRetry({
            withTimeout(timeoutMs) {
                command()
            }
        }, shouldRetry)
    }
}

/**
 * Manages command queue and prevents concurrent command execution
 */
class CommandQueueManager {
    
    private val commandMutex = kotlinx.coroutines.sync.Mutex()
    private val _queueSize = MutableStateFlow(0)
    val queueSize: StateFlow<Int> = _queueSize.asStateFlow()
    
    /**
     * Execute command exclusively (no other commands will run concurrently)
     */
    suspend fun <T> executeExclusive(command: suspend () -> T): T {
        commandMutex.lock()
        _queueSize.value++
        
        try {
            return command()
        } finally {
            _queueSize.value--
            commandMutex.unlock()
        }
    }
    
    /**
     * Check if queue is empty
     */
    fun isEmpty(): Boolean = _queueSize.value == 0
}

/**
 * Exception types for better error handling
 */
sealed class SerialException(message: String) : Exception(message) {
    class DeviceNotConnected : SerialException("Device not connected")
    class PermissionDenied : SerialException("USB permission denied")
    class DeviceNotFound : SerialException("No compatible device found")
    class CommandTimeout : SerialException("Command timed out")
    class InvalidResponse : SerialException("Invalid response received")
    class IOError(cause: Throwable) : SerialException("I/O error: ${cause.message}")
}

/**
 * Protocol-specific exceptions
 */
sealed class ProtocolException(message: String) : Exception(message) {
    class MalformedPacket : ProtocolException("Malformed binary packet")
    class UnsupportedCommand : ProtocolException("Unsupported command")
    class PayloadTooLarge : ProtocolException("Payload exceeds maximum size")
    class NACKReceived : ProtocolException("Device sent NACK")
}

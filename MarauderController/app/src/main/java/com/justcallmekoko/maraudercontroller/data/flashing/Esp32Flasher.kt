package com.justcallmekoko.maraudercontroller.data.flashing

import android.util.Log
import com.hoho.android.usbserial.driver.UsbSerialPort
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.IOException

/**
 * Main ESP32 flasher implementation
 */
class Esp32Flasher(
    private val serialPort: UsbSerialPort,
    private val bootController: BootModeController
) {
    
    private val _progress = MutableStateFlow(FlashProgress())
    val progress: StateFlow<FlashProgress> = _progress
    
    companion object {
        private const val TAG = "Esp32Flasher"
    }
    
    data class FlashProgress(
        val stage: FlashStage = FlashStage.IDLE,
        val bytesWritten: Int = 0,
        val totalBytes: Int = 0,
        val currentBlock: Int = 0,
        val totalBlocks: Int = 0,
        val message: String = ""
    ) {
        val percentage: Float
            get() = if (totalBytes > 0) (bytesWritten.toFloat() / totalBytes) * 100f else 0f
    }
    
    enum class FlashStage {
        IDLE,
        ENTERING_BOOTLOADER,
        SYNCING,
        PREPARING_FLASH,
        WRITING_FLASH,
        FINISHING,
        COMPLETE,
        ERROR
    }
    
    /**
     * Flash firmware to ESP32
     * 
     * @param firmware Firmware binary data
     * @param address Flash address (default 0x10000 for app)
     */
    suspend fun flashFirmware(
        firmware: ByteArray,
        address: Int = 0x10000
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Enter bootloader
            updateProgress(FlashStage.ENTERING_BOOTLOADER, "Entering bootloader mode...")
            bootController.enterBootloaderMode().getOrThrow()
            delay(500) // Wait for bootloader to initialize
            
            // Step 2: Sync with bootloader
            updateProgress(FlashStage.SYNCING, "Syncing with ESP32...")
            syncWithBootloader().getOrThrow()
            
            // Step 3: Prepare flash
            updateProgress(FlashStage.PREPARING_FLASH, "Preparing flash operation...")
            val blockSize = Esp32Commands.ESP_FLASH_BLOCK_SIZE
            val blocks = (firmware.size + blockSize - 1) / blockSize
            
            prepareFlash(firmware.size, blocks, blockSize, address).getOrThrow()
            
            // Step 4: Write flash data
            updateProgress(FlashStage.WRITING_FLASH, "Writing flash data...")
            writeFlashData(firmware, blockSize).getOrThrow()
            
            // Step 5: Finish flash operation
            updateProgress(FlashStage.FINISHING, "Finishing flash operation...")
            finishFlash(reboot = true).getOrThrow()
            
            updateProgress(FlashStage.COMPLETE, "Flash complete!")
            Result.success(Unit)
        } catch (e: Exception) {
            updateProgress(FlashStage.ERROR, "Flash failed: ${e.message}")
            Result.failure(e)
        }
    }
    
    /**
     * Sync with ESP32 bootloader
     */
    private suspend fun syncWithBootloader(retries: Int = 7): Result<Unit> {
        repeat(retries) {
            try {
                val syncCommand = Esp32Commands.createSyncCommand()
                val slipEncoded = SlipProtocol.encode(syncCommand)
                
                serialPort.write(slipEncoded, 1000)
                delay(100)
                
                // Read response
                val response = readResponse(1000)
                if (response != null) {
                    try {
                        val decoded = SlipProtocol.decode(response)
                        val parsed = Esp32Commands.parseResponse(decoded)
                        
                        if (parsed?.status == Esp32Commands.ResponseStatus.SUCCESS) {
                            return Result.success(Unit)
                        }
                    } catch (e: SlipDecodeException) {
                        Log.w(TAG, "Failed to decode SLIP response during sync", e)
                    }
                } else {
                    Log.d(TAG, "No response received during sync attempt")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Sync attempt failed", e)
                delay(100)
            }
        }
        
        return Result.failure(IOException("Failed to sync with ESP32 after $retries attempts"))
    }
    
    /**
     * Prepare flash operation
     */
    private suspend fun prepareFlash(
        size: Int,
        blocks: Int,
        blockSize: Int,
        offset: Int
    ): Result<Unit> {
        try {
            val command = Esp32Commands.createFlashBeginCommand(size, blocks, blockSize, offset)
            val slipEncoded = SlipProtocol.encode(command)
            
            serialPort.write(slipEncoded, 1000)
            delay(100)
            
            val response = readResponse(3000)
            if (response != null) {
                try {
                    val decoded = SlipProtocol.decode(response)
                    val parsed = Esp32Commands.parseResponse(decoded)
                    
                    if (parsed?.status == Esp32Commands.ResponseStatus.SUCCESS) {
                        _progress.value = _progress.value.copy(
                            totalBytes = size,
                            totalBlocks = blocks
                        )
                        return Result.success(Unit)
                    }
                } catch (e: SlipDecodeException) {
                    Log.e(TAG, "Failed to decode SLIP response in prepareFlash", e)
                    return Result.failure(e)
                }
            } else {
                Log.e(TAG, "No response received in prepareFlash")
            }
            
            return Result.failure(IOException("Failed to prepare flash"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception in prepareFlash", e)
            return Result.failure(e)
        }
    }
    
    /**
     * Write flash data in blocks
     */
    private suspend fun writeFlashData(firmware: ByteArray, blockSize: Int): Result<Unit> {
        try {
            val blocks = (firmware.size + blockSize - 1) / blockSize
            
            for (i in 0 until blocks) {
                val offset = i * blockSize
                val remaining = firmware.size - offset
                val currentBlockSize = minOf(remaining, blockSize)
                
                // Create block data (pad with 0xFF if needed)
                val blockData = ByteArray(blockSize) { 0xFF.toByte() }
                System.arraycopy(firmware, offset, blockData, 0, currentBlockSize)
                
                // Send flash data command
                val command = Esp32Commands.createFlashDataCommand(blockData, i)
                val slipEncoded = SlipProtocol.encode(command)
                
                serialPort.write(slipEncoded, 5000)
                
                // Read response
                val response = readResponse(5000)
                if (response != null) {
                    try {
                        val decoded = SlipProtocol.decode(response)
                        val parsed = Esp32Commands.parseResponse(decoded)
                        
                        if (parsed?.status != Esp32Commands.ResponseStatus.SUCCESS) {
                            Log.e(TAG, "Flash write failed at block $i with status: ${parsed?.status}")
                            return Result.failure(IOException("Flash write failed at block $i"))
                        }
                    } catch (e: SlipDecodeException) {
                        Log.e(TAG, "Failed to decode SLIP response at block $i", e)
                        return Result.failure(IOException("Failed to decode response at block $i", e))
                    }
                } else {
                    Log.e(TAG, "No response received for block $i")
                    return Result.failure(IOException("No response received for block $i"))
                }
                
                // Update progress
                _progress.value = _progress.value.copy(
                    bytesWritten = offset + currentBlockSize,
                    currentBlock = i + 1
                )
                
                // Small delay between blocks
                delay(10)
            }
            
            return Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Exception in writeFlashData", e)
            return Result.failure(e)
        }
    }
    
    /**
     * Finish flash operation
     */
    private suspend fun finishFlash(reboot: Boolean): Result<Unit> {
        try {
            val command = Esp32Commands.createFlashEndCommand(reboot)
            val slipEncoded = SlipProtocol.encode(command)
            
            serialPort.write(slipEncoded, 1000)
            delay(100)
            
            val response = readResponse(1000)
            if (response != null) {
                try {
                    val decoded = SlipProtocol.decode(response)
                    val parsed = Esp32Commands.parseResponse(decoded)
                    
                    if (parsed?.status == Esp32Commands.ResponseStatus.SUCCESS) {
                        return Result.success(Unit)
                    }
                } catch (e: SlipDecodeException) {
                    Log.e(TAG, "Failed to decode SLIP response in finishFlash", e)
                    return Result.failure(e)
                }
            } else {
                Log.e(TAG, "No response received in finishFlash")
            }
            
            return Result.failure(IOException("Failed to finish flash"))
        } catch (e: Exception) {
            Log.e(TAG, "Exception in finishFlash", e)
            return Result.failure(e)
        }
    }
    
    /**
     * Read response from serial port
     */
    private suspend fun readResponse(timeout: Int): ByteArray? {
        return withTimeoutOrNull(timeout.toLong()) {
            val buffer = ByteArray(4096)
            try {
                val len = serialPort.read(buffer, timeout)
                if (len > 0) {
                    buffer.copyOf(len)
                } else {
                    Log.d(TAG, "No data read from serial port")
                    null
                }
            } catch (e: Exception) {
                Log.w(TAG, "Exception reading from serial port: ${e.message}", e)
                null
            }
        }
    }
    
    /**
     * Update progress state
     */
    private fun updateProgress(stage: FlashStage, message: String) {
        _progress.value = _progress.value.copy(
            stage = stage,
            message = message
        )
    }
}

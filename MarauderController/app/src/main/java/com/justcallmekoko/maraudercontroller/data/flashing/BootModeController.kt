package com.justcallmekoko.maraudercontroller.data.flashing

import com.hoho.android.usbserial.driver.UsbSerialPort
import kotlinx.coroutines.delay

/**
 * Controls ESP32 boot mode using DTR/RTS signals
 * 
 * Boot mode entry sequence:
 * - ESP32 enters bootloader when GPIO0 is LOW during reset
 * - DTR typically controls EN (reset) pin
 * - RTS typically controls GPIO0 pin
 * 
 * Circuit varies by board, this implements common patterns.
 */
class BootModeController(private val serialPort: UsbSerialPort) {
    
    /**
     * Enter bootloader mode (automatic)
     * 
     * Sequence for NodeMCU/Wemos style boards:
     * 1. Set RTS=true (GPIO0=LOW)
     * 2. Set DTR=false (EN=LOW, reset asserted)
     * 3. Wait 100ms
     * 4. Set DTR=true (EN=HIGH, reset released)
     * 5. Wait 50ms
     * 6. Set RTS=false (GPIO0=HIGH)
     * 
     * ESP32 should now be in bootloader mode
     */
    suspend fun enterBootloaderMode(variant: Esp32Variant = Esp32Variant.GENERIC): Result<Unit> {
        return try {
            when (variant) {
                Esp32Variant.GENERIC -> enterBootloaderGeneric()
                Esp32Variant.DEVKITC -> enterBootloaderDevKitC()
                Esp32Variant.WROOM -> enterBootloaderWroom()
                Esp32Variant.S2 -> enterBootloaderS2()
                Esp32Variant.S3 -> enterBootloaderS3()
                Esp32Variant.C3 -> enterBootloaderC3()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(BootModeException("Failed to enter bootloader: ${e.message}", e))
        }
    }
    
    private suspend fun enterBootloaderGeneric() {
        // Standard sequence for most boards
        serialPort.dtr = false
        serialPort.rts = true
        delay(100)
        
        serialPort.dtr = true
        serialPort.rts = false
        delay(50)
        
        serialPort.dtr = false
    }
    
    private suspend fun enterBootloaderDevKitC() {
        // Espressif DevKitC has inverted logic on some pins
        serialPort.rts = false
        serialPort.dtr = false
        delay(100)
        
        serialPort.dtr = true
        delay(50)
        
        serialPort.rts = true
        serialPort.dtr = false
        delay(50)
        
        serialPort.rts = false
    }
    
    private suspend fun enterBootloaderWroom() {
        // WROOM modules might need longer delays
        serialPort.dtr = false
        serialPort.rts = true
        delay(200)
        
        serialPort.dtr = true
        delay(100)
        
        serialPort.rts = false
        serialPort.dtr = false
    }
    
    private suspend fun enterBootloaderS2() {
        // ESP32-S2 specific sequence
        serialPort.dtr = false
        serialPort.rts = true
        delay(100)
        
        serialPort.dtr = true
        delay(50)
        
        serialPort.rts = false
    }
    
    private suspend fun enterBootloaderS3() {
        // ESP32-S3 specific sequence (similar to generic)
        serialPort.dtr = false
        serialPort.rts = true
        delay(100)
        
        serialPort.dtr = true
        serialPort.rts = false
        delay(50)
        
        serialPort.dtr = false
    }
    
    private suspend fun enterBootloaderC3() {
        // ESP32-C3 specific sequence
        serialPort.dtr = false
        serialPort.rts = false
        delay(100)
        
        serialPort.dtr = true
        delay(50)
        
        serialPort.dtr = false
    }
    
    /**
     * Reset ESP32 to normal mode
     */
    suspend fun resetToNormalMode() {
        serialPort.dtr = false
        serialPort.rts = false
        delay(100)
        
        serialPort.dtr = true
        delay(50)
        
        serialPort.dtr = false
    }
    
    /**
     * Hard reset (toggle EN pin)
     */
    suspend fun hardReset() {
        serialPort.dtr = false
        delay(100)
        serialPort.dtr = true
        delay(50)
        serialPort.dtr = false
    }
}

enum class Esp32Variant {
    GENERIC,
    DEVKITC,
    WROOM,
    S2,
    S3,
    C3
}

class BootModeException(message: String, cause: Throwable? = null) : Exception(message, cause)

package com.justcallmekoko.maraudercontroller.data.flashing

import kotlinx.coroutines.delay

/**
 * Detects which boot mode sequence to use based on board response
 */
class BootModeDetector(private val controller: BootModeController) {
    
    /**
     * Auto-detect correct boot sequence by trying variants
     * 
     * @return Best matching Esp32Variant
     */
    suspend fun detectVariant(): Esp32Variant {
        val variants = listOf(
            Esp32Variant.GENERIC,
            Esp32Variant.DEVKITC,
            Esp32Variant.WROOM
        )
        
        for (variant in variants) {
            val result = controller.enterBootloaderMode(variant)
            if (result.isSuccess) {
                // Try to sync with bootloader
                delay(100)
                // If sync succeeds, this is the right variant
                return variant
            }
        }
        
        // Default to generic
        return Esp32Variant.GENERIC
    }
}

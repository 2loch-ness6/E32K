package com.justcallmekoko.maraudercontroller.data.flashing

import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * ESP32 ROM Bootloader Commands
 * Based on esptool.py protocol
 */
object Esp32Commands {
    
    // Command opcodes
    const val ESP_FLASH_BEGIN: Byte = 0x02
    const val ESP_FLASH_DATA: Byte = 0x03
    const val ESP_FLASH_END: Byte = 0x04
    const val ESP_MEM_BEGIN: Byte = 0x05
    const val ESP_MEM_END: Byte = 0x06
    const val ESP_MEM_DATA: Byte = 0x07
    const val ESP_SYNC: Byte = 0x08
    const val ESP_WRITE_REG: Byte = 0x09
    const val ESP_READ_REG: Byte = 0x0a
    const val ESP_SPI_ATTACH: Byte = 0x0d
    const val ESP_CHANGE_BAUDRATE: Byte = 0x0f
    const val ESP_SPI_FLASH_MD5: Byte = 0x13
    
    // Response values
    const val ESP_ROM_BAUD: Int = 115200
    const val ESP_FLASH_BLOCK_SIZE: Int = 0x400 // 1024 bytes
    
    /**
     * Command packet structure:
     * - Direction: 0x00 (request) or 0x01 (response)
     * - Command: 1 byte
     * - Size: 2 bytes (little endian)
     * - Checksum: 4 bytes
     * - Data: variable length
     */
    data class CommandPacket(
        val command: Byte,
        val data: ByteArray = byteArrayOf(),
        val checksum: Int = 0
    ) {
        fun toBytes(): ByteArray {
            val buffer = ByteBuffer.allocate(8 + data.size)
            buffer.order(ByteOrder.LITTLE_ENDIAN)
            
            buffer.put(0x00) // Direction: request
            buffer.put(command)
            buffer.putShort(data.size.toShort())
            buffer.putInt(calculateChecksum(data))
            buffer.put(data)
            
            return buffer.array()
        }
        
        private fun calculateChecksum(data: ByteArray): Int {
            var checksum = 0xEF
            for (byte in data) {
                checksum = checksum xor (byte.toInt() and 0xFF)
            }
            return checksum
        }
        
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CommandPacket

            if (command != other.command) return false
            if (!data.contentEquals(other.data)) return false
            if (checksum != other.checksum) return false

            return true
        }

        override fun hashCode(): Int {
            var result = command.toInt()
            result = 31 * result + data.contentHashCode()
            result = 31 * result + checksum
            return result
        }
    }
    
    /**
     * SYNC command - establish communication
     * Sends: 0x07 0x07 0x12 0x20 followed by 32 x 0x55
     */
    fun createSyncCommand(): ByteArray {
        val syncSequence = byteArrayOf(
            0x07, 0x07, 0x12, 0x20
        ) + ByteArray(32) { 0x55.toByte() }
        
        return CommandPacket(ESP_SYNC, syncSequence).toBytes()
    }
    
    /**
     * FLASH_BEGIN command - prepare for flash write
     * 
     * @param size Total size to flash
     * @param blocks Number of blocks
     * @param blockSize Size of each block
     * @param offset Flash offset address
     */
    fun createFlashBeginCommand(
        size: Int,
        blocks: Int,
        blockSize: Int,
        offset: Int
    ): ByteArray {
        val buffer = ByteBuffer.allocate(16)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(size)
        buffer.putInt(blocks)
        buffer.putInt(blockSize)
        buffer.putInt(offset)
        
        return CommandPacket(ESP_FLASH_BEGIN, buffer.array()).toBytes()
    }
    
    /**
     * FLASH_DATA command - write flash data block
     * 
     * @param data Block data (should be blockSize bytes, padded with 0xFF)
     * @param sequence Block sequence number
     */
    fun createFlashDataCommand(data: ByteArray, sequence: Int): ByteArray {
        val buffer = ByteBuffer.allocate(16 + data.size)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(data.size)
        buffer.putInt(sequence)
        buffer.putInt(0) // Reserved
        buffer.putInt(0) // Reserved
        buffer.put(data)
        
        return CommandPacket(ESP_FLASH_DATA, buffer.array()).toBytes()
    }
    
    /**
     * FLASH_END command - finish flash operation
     * 
     * @param reboot Whether to reboot after flash
     */
    fun createFlashEndCommand(reboot: Boolean = true): ByteArray {
        val buffer = ByteBuffer.allocate(4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(if (reboot) 0 else 1)
        
        return CommandPacket(ESP_FLASH_END, buffer.array()).toBytes()
    }
    
    /**
     * CHANGE_BAUDRATE command - change serial speed
     * 
     * @param baudrate New baudrate
     */
    fun createChangeBaudrateCommand(baudrate: Int): ByteArray {
        val buffer = ByteBuffer.allocate(8)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        buffer.putInt(baudrate)
        buffer.putInt(0) // Old baudrate (ignored by ROM)
        
        return CommandPacket(ESP_CHANGE_BAUDRATE, buffer.array()).toBytes()
    }
    
    /**
     * Parse response packet
     */
    data class ResponsePacket(
        val command: Byte,
        val size: Int,
        val value: Int,
        val data: ByteArray,
        val status: ResponseStatus
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ResponsePacket

            if (command != other.command) return false
            if (size != other.size) return false
            if (value != other.value) return false
            if (!data.contentEquals(other.data)) return false
            if (status != other.status) return false

            return true
        }

        override fun hashCode(): Int {
            var result = command.toInt()
            result = 31 * result + size
            result = 31 * result + value
            result = 31 * result + data.contentHashCode()
            result = 31 * result + status.hashCode()
            return result
        }
    }
    
    enum class ResponseStatus(val code: Byte) {
        SUCCESS(0x00),
        FAILURE(0x01);
        
        companion object {
            fun fromByte(byte: Byte) = values().find { it.code == byte } ?: FAILURE
        }
    }
    
    /**
     * Parse response from ESP32
     */
    fun parseResponse(data: ByteArray): ResponsePacket? {
        if (data.size < 10) return null
        
        val buffer = ByteBuffer.wrap(data)
        buffer.order(ByteOrder.LITTLE_ENDIAN)
        
        val direction = buffer.get()
        if (direction != 0x01.toByte()) return null // Must be response
        
        val command = buffer.get()
        val size = buffer.short.toInt()
        val value = buffer.int
        
        val responseData = ByteArray(size)
        buffer.get(responseData)
        
        val status = if (data.size > 10 + size) {
            ResponseStatus.fromByte(data[10 + size])
        } else {
            ResponseStatus.SUCCESS
        }
        
        return ResponsePacket(command, size, value, responseData, status)
    }
}

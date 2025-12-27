package com.justcallmekoko.maraudercontroller.data.protocol

/**
 * Binary Protocol Definitions for ESP32 Marauder
 * Must match MarauderProtocol.h in firmware
 */
object MarauderBinaryProtocol {
    const val START_BYTE: Byte = 0xA5.toByte()
    const val END_BYTE: Byte = 0x5A.toByte()
    
    // Command IDs
    const val CMD_PING: Byte = 0x00
    const val CMD_SCAN_AP: Byte = 0x01
    const val CMD_SCAN_STA: Byte = 0x02
    const val CMD_STOP_SCAN: Byte = 0x03
    const val CMD_ATTACK: Byte = 0x04
    const val CMD_GET_CONFIG: Byte = 0x05
    const val CMD_REBOOT: Byte = 0x06
    const val CMD_UPDATE: Byte = 0x07
    
    // Response IDs
    const val RESP_ACK: Byte = 0x00
    const val RESP_NACK: Byte = 0x01
    const val RESP_PONG: Byte = 0x02
    
    data class BinaryPacket(
        val cmd: Byte,
        val length: Int,
        val payload: ByteArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as BinaryPacket
            if (cmd != other.cmd) return false
            if (length != other.length) return false
            if (!payload.contentEquals(other.payload)) return false
            return true
        }

        override fun hashCode(): Int {
            var result = cmd.toInt()
            result = 31 * result + length
            result = 31 * result + payload.contentHashCode()
            return result
        }
        
        fun toBytes(): ByteArray {
            val buffer = java.io.ByteArrayOutputStream()
            buffer.write(START_BYTE.toInt())
            buffer.write(cmd.toInt())
            buffer.write(length)
            if (length > 0) {
                buffer.write(payload)
            }
            buffer.write(END_BYTE.toInt())
            return buffer.toByteArray()
        }
    }
}

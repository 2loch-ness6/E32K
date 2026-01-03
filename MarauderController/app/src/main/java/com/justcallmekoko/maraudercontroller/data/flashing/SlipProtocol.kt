package com.justcallmekoko.maraudercontroller.data.flashing

/**
 * SLIP Protocol Implementation (RFC 1055)
 * 
 * Special characters: 
 * - END: 0xC0 (192) - Frame delimiter
 * - ESC: 0xDB (219) - Escape character
 * - ESC_END: 0xDC (220) - Escaped END
 * - ESC_ESC: 0xDD (221) - Escaped ESC
 */
object SlipProtocol {
    const val END: Byte = 0xC0.toByte()
    const val ESC: Byte = 0xDB.toByte()
    const val ESC_END: Byte = 0xDC.toByte()
    const val ESC_ESC: Byte = 0xDD.toByte()
    
    /**
     * Encode data using SLIP protocol
     * 
     * @param data Raw data to encode
     * @return SLIP-encoded byte array with END delimiters
     * 
     * Algorithm:
     * 1. Start with END byte
     * 2. For each byte in data:
     *    - If byte == END, replace with ESC + ESC_END
     *    - If byte == ESC, replace with ESC + ESC_ESC
     *    - Otherwise, keep byte as-is
     * 3. Append END byte
     */
    fun encode(data: ByteArray): ByteArray {
        val encoded = mutableListOf<Byte>()
        encoded.add(END)
        
        for (byte in data) {
            when (byte) {
                END -> {
                    encoded.add(ESC)
                    encoded.add(ESC_END)
                }
                ESC -> {
                    encoded.add(ESC)
                    encoded.add(ESC_ESC)
                }
                else -> encoded.add(byte)
            }
        }
        
        encoded.add(END)
        return encoded.toByteArray()
    }
    
    /**
     * Decode SLIP-encoded data
     * 
     * @param encoded SLIP-encoded byte array
     * @return Decoded byte array
     * @throws SlipDecodeException if invalid escape sequence found
     */
    fun decode(encoded: ByteArray): ByteArray {
        val decoded = mutableListOf<Byte>()
        var i = 0
        var inFrame = false
        
        while (i < encoded.size) {
            when (encoded[i]) {
                END -> {
                    if (inFrame) {
                        // Frame complete (may be empty)
                        break
                    }
                    inFrame = true
                }
                ESC -> {
                    if (i + 1 >= encoded.size) {
                        throw SlipDecodeException("Incomplete escape sequence")
                    }
                    when (encoded[i + 1]) {
                        ESC_END -> decoded.add(END)
                        ESC_ESC -> decoded.add(ESC)
                        else -> throw SlipDecodeException("Invalid escape sequence")
                    }
                    i++ // Skip next byte
                }
                else -> {
                    if (inFrame) decoded.add(encoded[i])
                }
            }
            i++
        }
        
        return decoded.toByteArray()
    }
}

class SlipDecodeException(message: String) : Exception(message)

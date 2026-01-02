package com.justcallmekoko.maraudercontroller.data.flashing

import org.junit.Test
import org.junit.Assert.*

class SlipProtocolTest {
    
    @Test
    fun `encode simple data without special characters`() {
        val data = byteArrayOf(0x01, 0x02, 0x03)
        val encoded = SlipProtocol.encode(data)
        
        // Should be: [END, 0x01, 0x02, 0x03, END]
        assertEquals(5, encoded.size)
        assertEquals(SlipProtocol.END, encoded[0])
        assertEquals(SlipProtocol.END, encoded[4])
    }
    
    @Test
    fun `encode data with END character`() {
        val data = byteArrayOf(0x01, SlipProtocol.END, 0x03)
        val encoded = SlipProtocol.encode(data)
        
        // Should replace END with ESC + ESC_END
        assertTrue(encoded.contains(SlipProtocol.ESC))
        assertTrue(encoded.contains(SlipProtocol.ESC_END))
    }
    
    @Test
    fun `encode data with ESC character`() {
        val data = byteArrayOf(0x01, SlipProtocol.ESC, 0x03)
        val encoded = SlipProtocol.encode(data)
        
        // Should replace ESC with ESC + ESC_ESC
        val escCount = encoded.count { it == SlipProtocol.ESC }
        assertTrue(escCount >= 2) // At least 2 ESC characters
    }
    
    @Test
    fun `decode reverses encode`() {
        val original = byteArrayOf(0x01, 0x02, SlipProtocol.END, SlipProtocol.ESC, 0x05)
        val encoded = SlipProtocol.encode(original)
        val decoded = SlipProtocol.decode(encoded)
        
        assertArrayEquals(original, decoded)
    }
    
    @Test(expected = SlipDecodeException::class)
    fun `decode throws on invalid escape sequence`() {
        val invalid = byteArrayOf(SlipProtocol.END, SlipProtocol.ESC, 0xFF.toByte(), SlipProtocol.END)
        SlipProtocol.decode(invalid)
    }
    
    @Test
    fun `encode empty data`() {
        val data = byteArrayOf()
        val encoded = SlipProtocol.encode(data)
        
        // Should be: [END, END]
        assertEquals(2, encoded.size)
        assertEquals(SlipProtocol.END, encoded[0])
        assertEquals(SlipProtocol.END, encoded[1])
    }
    
    @Test
    fun `decode empty frame`() {
        val encoded = byteArrayOf(SlipProtocol.END, SlipProtocol.END)
        val decoded = SlipProtocol.decode(encoded)
        
        assertEquals(0, decoded.size)
    }
    
    @Test
    fun `encode large data`() {
        val data = ByteArray(2048) { (it % 256).toByte() }
        val encoded = SlipProtocol.encode(data)
        val decoded = SlipProtocol.decode(encoded)
        
        assertArrayEquals(data, decoded)
    }
    
    @Test(expected = SlipDecodeException::class)
    fun `decode throws on incomplete escape at end`() {
        val invalid = byteArrayOf(SlipProtocol.END, 0x01, SlipProtocol.ESC)
        SlipProtocol.decode(invalid)
    }
}

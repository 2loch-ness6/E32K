package com.justcallmekoko.maraudercontroller.data.serial

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors
import com.justcallmekoko.maraudercontroller.data.protocol.MarauderBinaryProtocol
import java.io.ByteArrayOutputStream

/**
 * Manages USB serial connection to ESP32 Marauder device
 */
class SerialConnectionManager(private val context: Context) : SerialInputOutputManager.Listener {
    
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val usbManager: UsbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private val executor = Executors.newSingleThreadExecutor()
    
    private var serialPort: UsbSerialPort? = null
    private var serialConnection: UsbDeviceConnection? = null
    private var ioManager: SerialInputOutputManager? = null
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _receivedData = MutableStateFlow<String>("")
    val receivedData: StateFlow<String> = _receivedData.asStateFlow()
    
    private val _binaryEvents = kotlinx.coroutines.flow.MutableSharedFlow<MarauderBinaryProtocol.BinaryPacket>(extraBufferCapacity = 64)
    val binaryEvents: kotlinx.coroutines.flow.SharedFlow<MarauderBinaryProtocol.BinaryPacket> = _binaryEvents.asSharedFlow()
    
    private val readBuffer = StringBuilder()

    // Binary Protocol State
    private enum class ParserState { IDLE, WAIT_CMD, WAIT_LEN, WAIT_PAYLOAD, WAIT_END }
    private var parserState = ParserState.IDLE
    private var binaryCmd: Byte = 0
    private var binaryLen: Int = 0
    private val binaryPayload = ByteArrayOutputStream()
    
    // Command Queue Synchronization
    private val sendMutex = Mutex()
    private var activeCommandResponse: CompletableDeferred<Boolean>? = null
    private var activeResponsePattern: Regex? = null
    
    private var activeBinaryResponse: CompletableDeferred<MarauderBinaryProtocol.BinaryPacket>? = null
    private var activeBinaryCmd: Byte? = null
    
    companion object {
        private const val ACTION_USB_PERMISSION = "com.justcallmekoko.maraudercontroller.USB_PERMISSION"
        private const val BAUD_RATE = 115200
        private const val DATA_BITS = 8
        private const val STOP_BITS = UsbSerialPort.STOPBITS_1
        private const val PARITY = UsbSerialPort.PARITY_NONE
        private const val READ_TIMEOUT = 1000
        private const val WRITE_TIMEOUT = 1000
    }
    
    sealed class ConnectionState {
        object Disconnected : ConnectionState()
        object Connecting : ConnectionState()
        data class Connected(val deviceName: String) : ConnectionState()
        data class Error(val message: String) : ConnectionState()
    }
    
    private val usbReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_USB_PERMISSION -> {
                    synchronized(this) {
                        val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                        if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                            device?.let { connectToDevice(it) }
                        } else {
                            _connectionState.value = ConnectionState.Error("USB permission denied")
                        }
                    }
                }
                UsbManager.ACTION_USB_DEVICE_DETACHED -> {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    if (device == serialPort?.device) {
                        disconnect()
                    }
                }
            }
        }
    }
    
    init {
        val filter = IntentFilter().apply {
            addAction(ACTION_USB_PERMISSION)
            addAction(UsbManager.ACTION_USB_DEVICE_DETACHED)
        }
        // Register receiver with RECEIVER_NOT_EXPORTED for Android 13+ compatibility
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(usbReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(usbReceiver, filter)
        }
    }
    
    /**
     * Find all available ESP32/Serial devices
     */
    fun findDevices(): List<UsbSerialDriver> {
        return UsbSerialProber.getDefaultProber().findAllDrivers(usbManager)
    }
    
    /**
     * Request permission and connect to device
     */
    fun requestConnection(driver: UsbSerialDriver) {
        val device = driver.device
        
        if (usbManager.hasPermission(device)) {
            connectToDevice(device)
        } else {
            val permissionIntent = PendingIntent.getBroadcast(
                context,
                0,
                Intent(ACTION_USB_PERMISSION),
                PendingIntent.FLAG_MUTABLE
            )
            usbManager.requestPermission(device, permissionIntent)
        }
    }
    
    /**
     * Connect to USB device
     */
    private fun connectToDevice(device: UsbDevice) {
        scope.launch {
            try {
                _connectionState.value = ConnectionState.Connecting
                
                val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
                    ?: throw IOException("No driver found for device")
                
                val connection = usbManager.openDevice(device)
                    ?: throw IOException("Failed to open device")
                
                val port = driver.ports[0]
                port.open(connection)
                port.setParameters(BAUD_RATE, DATA_BITS, STOP_BITS, PARITY)
                port.dtr = true
                port.rts = true
                
                serialPort = port
                serialConnection = connection
                
                // Start IO manager
                ioManager = SerialInputOutputManager(port, this@SerialConnectionManager).apply {
                    start()
                }
                
                _connectionState.value = ConnectionState.Connected(device.deviceName)
                
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Connection failed: ${e.message}")
                cleanup()
            }
        }
    }
    
    /**
     * Disconnect from device
     */
    fun disconnect() {
        scope.launch {
            cleanup()
            _connectionState.value = ConnectionState.Disconnected
        }
    }
    
    private fun cleanup() {
        ioManager?.stop()
        ioManager = null
        
        try {
            serialPort?.close()
        } catch (e: Exception) {
            // Ignore
        }
        serialPort = null
        
        serialConnection?.close()
        serialConnection = null
        
        readBuffer.clear()
    }
    
    /**
     * Send command to device (fire and forget, serialized)
     */
    fun sendCommand(command: String) {
        scope.launch {
            sendMutex.withLock {
                try {
                    val port = serialPort ?: throw IOException("Not connected")
                    val data = "$command\n".toByteArray()
                    port.write(data, WRITE_TIMEOUT)
                } catch (e: Exception) {
                    _connectionState.value = ConnectionState.Error("Send failed: ${e.message}")
                }
            }
        }
    }
    
    /**
     * Send command and wait for specific response pattern
     * Returns true if response matched, false on timeout or error
     */
    suspend fun sendCommandAndWait(command: String, responsePattern: Regex, timeoutMs: Long = 2000L): Boolean {
        return sendMutex.withLock {
            try {
                val port = serialPort ?: return@withLock false
                
                // Setup waiter
                val deferred = CompletableDeferred<Boolean>()
                activeResponsePattern = responsePattern
                activeCommandResponse = deferred
                
                // Send
                val data = "$command\n".toByteArray()
                port.write(data, WRITE_TIMEOUT)
                
                // Wait
                withTimeoutOrNull(timeoutMs) {
                    deferred.await()
                } ?: false
                
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("SendWait failed: ${e.message}")
                false
            } finally {
                activeCommandResponse = null
                activeResponsePattern = null
            }
        }
    }

    /**
     * Send raw data to device
     */
    fun sendRaw(data: ByteArray) {
        scope.launch {
            try {
                val port = serialPort ?: throw IOException("Not connected")
                port.write(data, WRITE_TIMEOUT)
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Send failed: ${e.message}")
            }
        }
    }

    /**
     * Send raw data chunk to device
     */
    fun sendRawData(data: ByteArray, length: Int) {
        scope.launch {
            try {
                val port = serialPort ?: throw IOException("Not connected")
                // Copy buffer if needed or just write the slice if the library supports it.
                // usb-serial-for-android write takes a byte array and timeout. 
                // We typically slice it if the array is larger than length, 
                // but usually the caller passes a buffer of exact size or we write the whole thing.
                // Given the caller usage (buffer read from stream), we should respect length.
                val actualData = if (data.size == length) data else data.copyOf(length)
                port.write(actualData, WRITE_TIMEOUT)
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Send chunk failed: ${e.message}")
            }
        }
    }

    /**
     * Send binary command packet
     */
    fun sendBinaryCommand(packet: MarauderBinaryProtocol.BinaryPacket) {
        sendRaw(packet.toBytes())
    }

    /**
     * Send binary command and wait for response
     */
    suspend fun sendBinaryCommandAndWait(
        packet: MarauderBinaryProtocol.BinaryPacket, 
        timeoutMs: Long = 2000L
    ): MarauderBinaryProtocol.BinaryPacket? {
        return sendMutex.withLock {
            try {
                val port = serialPort ?: return@withLock null
                
                // Setup waiter
                val deferred = CompletableDeferred<MarauderBinaryProtocol.BinaryPacket>()
                activeBinaryResponse = deferred
                activeBinaryCmd = packet.cmd
                
                // Send
                port.write(packet.toBytes(), WRITE_TIMEOUT)
                
                // Wait
                withTimeoutOrNull(timeoutMs) {
                    deferred.await()
                }
                
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("SendWaitBin failed: ${e.message}")
                null
            } finally {
                activeBinaryResponse = null
                activeBinaryCmd = null
            }
        }
    }
    
    // SerialInputOutputManager.Listener implementation
    override fun onNewData(data: ByteArray) {
        scope.launch {
            try {
                for (byte in data) {
                    processByte(byte)
                }
            } catch (e: Exception) {
                // Log error
            }
        }
    }

    private fun processByte(byte: Byte) {
        when (parserState) {
            ParserState.IDLE -> {
                if (byte == MarauderBinaryProtocol.START_BYTE) {
                    parserState = ParserState.WAIT_CMD
                } else {
                    // Text data
                    val char = byte.toInt().toChar()
                    readBuffer.append(char)
                    if (char == '\n') {
                        val line = readBuffer.toString().trim()
                        readBuffer.setLength(0) // Clear buffer
                        if (line.isNotEmpty()) {
                            _receivedData.value = line
                            
                            // Check for active waiter (Text mode)
                            activeResponsePattern?.let { pattern ->
                                if (pattern.containsMatchIn(line)) {
                                    activeCommandResponse?.complete(true)
                                }
                            }
                        }
                    }
                }
            }
            ParserState.WAIT_CMD -> {
                binaryCmd = byte
                parserState = ParserState.WAIT_LEN
            }
            ParserState.WAIT_LEN -> {
                binaryLen = byte.toInt() and 0xFF
                binaryPayload.reset()
                if (binaryLen > 0) {
                    parserState = ParserState.WAIT_PAYLOAD
                } else {
                    parserState = ParserState.WAIT_END
                }
            }
            ParserState.WAIT_PAYLOAD -> {
                binaryPayload.write(byte.toInt())
                if (binaryPayload.size() == binaryLen) {
                    parserState = ParserState.WAIT_END
                }
            }
            ParserState.WAIT_END -> {
                if (byte == MarauderBinaryProtocol.END_BYTE) {
                    // Valid Packet Received
                    handleBinaryPacket(binaryCmd, binaryPayload.toByteArray())
                } else {
                    // Invalid, discard or treat as text? 
                    // For now, reset to idle.
                }
                parserState = ParserState.IDLE
            }
        }
    }

    private fun handleBinaryPacket(cmd: Byte, payload: ByteArray) {
        val packet = MarauderBinaryProtocol.BinaryPacket(cmd, payload.size, payload)
        
        // 1. Check for active waiter
        if (activeBinaryResponse != null) {
            // If we are waiting for a specific command response (e.g. ACK/NACK for a sent CMD)
            // The protocol might define that CMD_PING receives RESP_PONG, etc.
            // Or we might just accept any packet if we are waiting.
            // For now, let's complete if it matches expectation or if we are generic.
            
            // Simple logic: If waiting, deliver it.
            // The caller is responsible for checking if the packet is what they wanted.
            activeBinaryResponse?.complete(packet)
        }
        
        // 2. Always emit to Flow for observers
        scope.launch {
            _binaryEvents.emit(packet)
        }
    }

    
    override fun onRunError(e: Exception) {
        scope.launch {
            _connectionState.value = ConnectionState.Error("Communication error: ${e.message}")
            cleanup()
        }
    }
    
    fun release() {
        disconnect()
        try {
            context.unregisterReceiver(usbReceiver)
        } catch (e: Exception) {
            // Already unregistered
        }
        executor.shutdown()
    }
}

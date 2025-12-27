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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import java.nio.ByteBuffer
import java.util.concurrent.Executors

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
    
    private val readBuffer = StringBuilder()
    
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
        context.registerReceiver(usbReceiver, filter)
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
                    executor.submit(this)
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
     * Send command to device
     */
    fun sendCommand(command: String) {
        scope.launch {
            try {
                val port = serialPort ?: throw IOException("Not connected")
                val data = "$command\n".toByteArray()
                port.write(data, WRITE_TIMEOUT)
            } catch (e: Exception) {
                _connectionState.value = ConnectionState.Error("Send failed: ${e.message}")
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
    
    // SerialInputOutputManager.Listener implementation
    override fun onNewData(data: ByteArray) {
        scope.launch {
            try {
                val text = String(data, Charsets.UTF_8)
                readBuffer.append(text)
                
                // Emit complete lines
                var newlineIndex = readBuffer.indexOf('\n')
                while (newlineIndex != -1) {
                    val line = readBuffer.substring(0, newlineIndex)
                    readBuffer.delete(0, newlineIndex + 1)
                    _receivedData.value = line
                    newlineIndex = readBuffer.indexOf('\n')
                }
            } catch (e: Exception) {
                // Log error
            }
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

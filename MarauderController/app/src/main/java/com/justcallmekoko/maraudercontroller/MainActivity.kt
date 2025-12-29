package com.justcallmekoko.maraudercontroller

import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.justcallmekoko.maraudercontroller.data.preferences.PreferencesManager
import com.justcallmekoko.maraudercontroller.data.repository.MarauderRepository
import com.justcallmekoko.maraudercontroller.ui.screens.MainScreen
import com.justcallmekoko.maraudercontroller.ui.theme.MarauderControllerTheme
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

class MainActivity : ComponentActivity() {
    
    private val preferencesManager by lazy { PreferencesManager(applicationContext) }
    private val repository by lazy { MarauderRepository(applicationContext) }
    
    private val viewModel: MarauderViewModel by viewModels {
        MarauderViewModel.Factory(repository, preferencesManager)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        handleIntent(intent)
        
        setContent {
            val themeMode by preferencesManager.themeModeFlow.collectAsState(
                initial = PreferencesManager.ThemeMode.SYSTEM
            )
            
            MarauderControllerTheme(
                darkTheme = when (themeMode) {
                    PreferencesManager.ThemeMode.LIGHT -> false
                    PreferencesManager.ThemeMode.DARK -> true
                    PreferencesManager.ThemeMode.SYSTEM -> androidx.compose.foundation.isSystemInDarkTheme()
                }
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
            device?.let { viewModel.connectToDevice(it) }
        }
    }
}

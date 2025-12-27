package com.justcallmekoko.maraudercontroller

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.justcallmekoko.maraudercontroller.data.repository.MarauderRepository
import com.justcallmekoko.maraudercontroller.ui.screens.MainScreen
import com.justcallmekoko.maraudercontroller.ui.theme.MarauderControllerTheme
import com.justcallmekoko.maraudercontroller.ui.viewmodel.MarauderViewModel

class MainActivity : ComponentActivity() {
    
    private val repository by lazy { MarauderRepository(applicationContext) }
    
    private val viewModel: MarauderViewModel by viewModels {
        MarauderViewModel.Factory(repository)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MarauderControllerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}

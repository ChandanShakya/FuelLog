package com.chandanshakya.fuellog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.chandanshakya.fuellog.ui.navigation.AppNavHost
import com.chandanshakya.fuellog.ui.theme.FuelLogTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main entry point for FuelLog application.
 * 
 * Single-activity architecture with Jetpack Compose navigation.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelLogApp()
        }
    }
}

/**
 * Root composable for the FuelLog application.
 */
@Composable
fun FuelLogApp() {
    FuelLogTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            val navController = rememberNavController()

            AppNavHost(
                navController = navController
            )
        }
    }
}

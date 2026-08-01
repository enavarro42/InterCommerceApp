package com.inter.intercommerceapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.inter.intercommerceapp.presentation.navigation.AppNavHost
import com.inter.intercommerceapp.ui.theme.InterCommerceAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            InterCommerceAppTheme {
                AppNavHost()
            }
        }
    }
}

package com.inter.intercommerceapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inter.intercommerceapp.presentation.catalog.CatalogRoute
import kotlinx.serialization.Serializable

@Serializable
object CatalogDestination

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CatalogDestination) {
        composable<CatalogDestination> {
            CatalogRoute()
        }
    }
}

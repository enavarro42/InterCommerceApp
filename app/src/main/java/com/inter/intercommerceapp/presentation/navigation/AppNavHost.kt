package com.inter.intercommerceapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inter.intercommerceapp.presentation.catalog.CatalogRoute
import com.inter.intercommerceapp.presentation.productdetail.ProductDetailRoute
import kotlinx.serialization.Serializable

@Serializable
object CatalogDestination

@Serializable
data class ProductDetailDestination(val productId: Int)

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CatalogDestination) {
        composable<CatalogDestination> {
            CatalogRoute(
                onProductClick = { product ->
                    navController.navigate(ProductDetailDestination(product.id))
                },
            )
        }
        composable<ProductDetailDestination> {
            ProductDetailRoute(onBack = { navController.navigateUp() })
        }
    }
}

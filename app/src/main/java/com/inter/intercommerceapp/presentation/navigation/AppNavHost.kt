package com.inter.intercommerceapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.inter.intercommerceapp.presentation.cart.CartRoute
import com.inter.intercommerceapp.presentation.catalog.CatalogRoute
import com.inter.intercommerceapp.presentation.ordersuccess.OrderSuccessRoute
import com.inter.intercommerceapp.presentation.productdetail.ProductDetailRoute
import kotlinx.serialization.Serializable

@Serializable
object CatalogDestination

@Serializable
data class ProductDetailDestination(val productId: Int)

@Serializable
object CartDestination

@Serializable
object OrderSuccessDestination

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = CatalogDestination) {
        composable<CatalogDestination> {
            CatalogRoute(
                onProductClick = { product ->
                    navController.navigate(ProductDetailDestination(product.id))
                },
                onCartClick = { navController.navigate(CartDestination) },
            )
        }
        composable<ProductDetailDestination> {
            ProductDetailRoute(
                onBack = { navController.navigateUp() },
                onCartClick = { navController.navigate(CartDestination) },
            )
        }
        composable<CartDestination> {
            CartRoute(
                onBack = { navController.navigateUp() },
                onOrderPlaced = { navController.navigate(OrderSuccessDestination) },
            )
        }
        composable<OrderSuccessDestination> {
            OrderSuccessRoute(
                onBack = { navController.navigateUp() },
                onBackToCatalog = {
                    navController.navigate(CatalogDestination) {
                        popUpTo(CatalogDestination) { inclusive = false }
                        launchSingleTop = true
                    }
                },
            )
        }
    }
}

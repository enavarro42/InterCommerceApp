package com.inter.intercommerceapp.domain.repository

import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface CartRepository {

    fun observeCart(): Flow<List<CartItem>>

    suspend fun addToCart(product: Product, quantity: Int = 1)

    suspend fun updateQuantity(productId: Int, quantity: Int)

    suspend fun removeFromCart(productId: Int)

    suspend fun clearCart()
}

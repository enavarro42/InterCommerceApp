package com.inter.intercommerceapp.data.repository

import com.inter.intercommerceapp.data.local.db.CartDao
import com.inter.intercommerceapp.data.mapper.CartMapper
import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.repository.CartRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
) : CartRepository {

    override fun observeCart(): Flow<List<CartItem>> =
        cartDao.getAll().map { entities -> entities.map(CartMapper::toDomain) }

    override suspend fun addToCart(product: Product, quantity: Int) {
        cartDao.upsert(CartMapper.toEntity(product, quantity))
    }

    override suspend fun updateQuantity(productId: Int, quantity: Int) {
        cartDao.updateQuantity(productId, quantity)
    }

    override suspend fun removeFromCart(productId: Int) {
        cartDao.removeItem(productId)
    }

    override suspend fun clearCart() {
        cartDao.clear()
    }
}

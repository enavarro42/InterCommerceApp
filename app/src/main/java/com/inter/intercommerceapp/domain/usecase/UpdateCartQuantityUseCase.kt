package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.repository.CartRepository
import javax.inject.Inject

class UpdateCartQuantityUseCase @Inject constructor(
    private val repository: CartRepository,
) {

    suspend operator fun invoke(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            repository.removeFromCart(productId)
        } else {
            repository.updateQuantity(productId, quantity)
        }
    }
}

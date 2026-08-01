package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.repository.CartRepository
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {

    suspend operator fun invoke(product: Product, quantity: Int = 1) =
        repository.addToCart(product, quantity)
}

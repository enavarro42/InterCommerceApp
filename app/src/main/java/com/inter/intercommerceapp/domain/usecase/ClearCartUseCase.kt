package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.repository.CartRepository
import javax.inject.Inject

class ClearCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {

    suspend operator fun invoke() = repository.clearCart()
}

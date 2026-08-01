package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.repository.CartRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveCartUseCase @Inject constructor(
    private val repository: CartRepository,
) {

    operator fun invoke(): Flow<List<CartItem>> = repository.observeCart()
}

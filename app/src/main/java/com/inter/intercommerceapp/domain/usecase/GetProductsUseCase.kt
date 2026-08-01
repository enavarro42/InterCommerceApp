package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.ProductsResult
import com.inter.intercommerceapp.domain.repository.ProductRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetProductsUseCase @Inject constructor(
    private val repository: ProductRepository,
) {

    operator fun invoke(limit: Int, skip: Int): Flow<ProductsResult> =
        repository.getProducts(limit, skip)
}

package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.ProductResult
import com.inter.intercommerceapp.domain.repository.ProductRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class GetProductByIdUseCase @Inject constructor(
    private val repository: ProductRepository,
) {

    operator fun invoke(id: Int): Flow<Result<ProductResult>> =
        repository.getProductById(id)
}

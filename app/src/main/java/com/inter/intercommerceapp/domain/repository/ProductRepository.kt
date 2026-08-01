package com.inter.intercommerceapp.domain.repository

import com.inter.intercommerceapp.domain.model.ProductResult
import com.inter.intercommerceapp.domain.model.ProductsResult
import kotlinx.coroutines.flow.Flow

interface ProductRepository {

    fun getProducts(limit: Int, skip: Int): Flow<ProductsResult>

    fun searchProducts(query: String): Flow<ProductsResult>

    fun getProductById(id: Int): Flow<Result<ProductResult>>
}

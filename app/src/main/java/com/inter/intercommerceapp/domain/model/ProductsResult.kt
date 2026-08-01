package com.inter.intercommerceapp.domain.model

data class ProductsResult(
    val products: List<Product>,
    val isFromCache: Boolean,
)

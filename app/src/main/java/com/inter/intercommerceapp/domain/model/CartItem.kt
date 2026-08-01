package com.inter.intercommerceapp.domain.model

data class CartItem(
    val productId: Int,
    val title: String,
    val thumbnailUrl: String,
    val unitPrice: Double,
    val discountPercentage: Double,
    val quantity: Int,
)

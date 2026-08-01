package com.inter.intercommerceapp.domain.model

data class CartTotals(
    val subtotal: Double,
    val discountAmount: Double,
    val taxAmount: Double,
    val total: Double,
)

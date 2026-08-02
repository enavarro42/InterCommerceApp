package com.inter.intercommerceapp.presentation.cart

import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.CartTotals

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val totals: CartTotals = CartTotals(subtotal = 0.0, discountAmount = 0.0, taxAmount = 0.0, total = 0.0),
    val isEmpty: Boolean = true,
)

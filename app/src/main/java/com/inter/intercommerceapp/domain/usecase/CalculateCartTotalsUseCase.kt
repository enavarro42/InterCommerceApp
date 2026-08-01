package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.CartTotals
import javax.inject.Inject

class CalculateCartTotalsUseCase @Inject constructor() {

    operator fun invoke(items: List<CartItem>): CartTotals {
        val subtotal = items.sumOf { it.unitPrice * it.quantity }
        val discountAmount = items.sumOf { it.unitPrice * it.quantity * it.discountPercentage / 100 }
        val taxAmount = (subtotal - discountAmount) * CART_TAX_RATE
        val total = subtotal - discountAmount + taxAmount

        return CartTotals(
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            total = total,
        )
    }

    private companion object {
        const val CART_TAX_RATE = 0.08
    }
}

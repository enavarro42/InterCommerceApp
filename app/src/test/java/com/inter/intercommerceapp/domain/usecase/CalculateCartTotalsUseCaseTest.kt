package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.CartItem
import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateCartTotalsUseCaseTest {

    private val useCase = CalculateCartTotalsUseCase()

    private fun cartItem(
        productId: Int,
        unitPrice: Double,
        quantity: Int,
        discountPercentage: Double = 0.0,
    ) = CartItem(
        productId = productId,
        title = "Product $productId",
        thumbnailUrl = "thumb",
        unitPrice = unitPrice,
        discountPercentage = discountPercentage,
        quantity = quantity,
    )

    @Test
    fun `invoke computes subtotal, discount, tax, and total for a multi-item cart`() {
        val items = listOf(
            cartItem(productId = 1, unitPrice = 100.0, quantity = 1, discountPercentage = 10.0),
            cartItem(productId = 2, unitPrice = 10.0, quantity = 2, discountPercentage = 0.0),
        )

        val totals = useCase(items)

        assertEquals(120.0, totals.subtotal, 0.0001)
        assertEquals(10.0, totals.discountAmount, 0.0001)
        assertEquals(8.8, totals.taxAmount, 0.0001)
        assertEquals(118.8, totals.total, 0.0001)
    }

    @Test
    fun `invoke returns all-zero totals for an empty cart`() {
        val totals = useCase(emptyList())

        assertEquals(0.0, totals.subtotal, 0.0001)
        assertEquals(0.0, totals.discountAmount, 0.0001)
        assertEquals(0.0, totals.taxAmount, 0.0001)
        assertEquals(0.0, totals.total, 0.0001)
    }
}

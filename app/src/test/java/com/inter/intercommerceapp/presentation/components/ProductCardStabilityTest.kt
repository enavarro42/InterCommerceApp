package com.inter.intercommerceapp.presentation.components

import com.inter.intercommerceapp.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * `ProductCard`'s only non-lambda parameter is [Product]. For Compose to skip recomposition when
 * neither `product` nor `onClick` changes, [Product] must be structurally immutable: a data class
 * with value-based equals/hashCode. This is the equals/hashCode-based proxy for the Compose
 * compiler's stability report, which isn't wired into unit tests here.
 */
class ProductCardStabilityTest {

    private fun product(id: Int) = Product(
        id = id,
        title = "Product $id",
        description = "desc",
        category = "misc",
        price = 10.0,
        discountPercentage = 0.0,
        rating = 4.0,
        stock = 1,
        brand = null,
        thumbnail = "thumb",
        images = emptyList(),
    )

    @Test
    fun `structurally equal Product instances are equal and share a hashCode`() {
        val a = product(1)
        val b = product(1)

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
    }

    @Test
    fun `Products differing by a single field are not equal`() {
        val a = product(1)
        val b = product(1).copy(title = "Different")

        assertNotEquals(a, b)
    }
}

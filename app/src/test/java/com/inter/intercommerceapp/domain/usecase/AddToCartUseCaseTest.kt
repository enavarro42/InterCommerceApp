package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

private class FakeCartRepository : CartRepository {

    private val items = MutableStateFlow<List<CartItem>>(emptyList())

    override fun observeCart(): Flow<List<CartItem>> = items

    override suspend fun addToCart(product: Product, quantity: Int) {
        val existing = items.value.find { it.productId == product.id }
        items.value = if (existing != null) {
            items.value.map {
                if (it.productId == product.id) it.copy(quantity = it.quantity + quantity) else it
            }
        } else {
            items.value + CartItem(
                productId = product.id,
                title = product.title,
                thumbnailUrl = product.thumbnail,
                unitPrice = product.price,
                discountPercentage = product.discountPercentage,
                quantity = quantity,
            )
        }
    }

    override suspend fun updateQuantity(productId: Int, quantity: Int) = Unit

    override suspend fun removeFromCart(productId: Int) = Unit

    override suspend fun clearCart() = Unit
}

class AddToCartUseCaseTest {

    private val repository = FakeCartRepository()
    private val useCase = AddToCartUseCase(repository)

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
    fun `invoke adds a new line item for a product not yet in the cart`() = runBlocking {
        useCase(product(1), 1)

        val cart = repository.observeCart().first()
        assertEquals(listOf(1 to 1), cart.map { it.productId to it.quantity })
    }

    @Test
    fun `invoke increments quantity when the product is already in the cart`() = runBlocking {
        useCase(product(1), 2)
        useCase(product(1), 1)

        val cart = repository.observeCart().first()
        assertEquals(1, cart.size)
        assertEquals(3, cart.single().quantity)
    }
}

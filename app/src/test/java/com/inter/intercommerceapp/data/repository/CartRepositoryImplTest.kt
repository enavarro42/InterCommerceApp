package com.inter.intercommerceapp.data.repository

import app.cash.turbine.test
import com.inter.intercommerceapp.data.local.db.CartDao
import com.inter.intercommerceapp.data.local.db.entity.CartItemEntity
import com.inter.intercommerceapp.domain.model.Product
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CartRepositoryImplTest {

    private val cartDao = mockk<CartDao>()
    private lateinit var repository: CartRepositoryImpl

    private val cartItems = MutableStateFlow<List<CartItemEntity>>(emptyList())

    private fun product(id: Int) = Product(
        id = id,
        title = "Product $id",
        description = "desc",
        category = "misc",
        price = 10.0,
        discountPercentage = 5.0,
        rating = 4.0,
        stock = 1,
        brand = null,
        thumbnail = "thumb",
        images = emptyList(),
    )

    @Before
    fun setUp() {
        every { cartDao.getAll() } returns cartItems
        repository = CartRepositoryImpl(cartDao)
    }

    @Test
    fun `observeCart emits mapped CartItems reactively when the dao flow emits`() = runTest {
        repository.observeCart().test {
            assertEquals(emptyList<Any>(), awaitItem())

            cartItems.value = listOf(
                CartItemEntity(
                    productId = 1,
                    title = "Product 1",
                    thumbnailUrl = "thumb",
                    unitPrice = 10.0,
                    discountPercentage = 5.0,
                    quantity = 2,
                    addedAt = 1_000L,
                ),
            )

            val emitted = awaitItem()
            assertEquals(1, emitted.size)
            assertEquals(1, emitted.single().productId)
            assertEquals(2, emitted.single().quantity)
        }
    }

    @Test
    fun `addToCart calls CartDao upsert with the correctly mapped entity`() = runTest {
        coEvery { cartDao.upsert(any()) } returns Unit

        repository.addToCart(product(1), quantity = 3)

        coVerify {
            cartDao.upsert(
                match {
                    it.productId == 1 &&
                        it.title == "Product 1" &&
                        it.unitPrice == 10.0 &&
                        it.discountPercentage == 5.0 &&
                        it.quantity == 3
                },
            )
        }
    }
}

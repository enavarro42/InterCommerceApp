package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.repository.CartRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Test

class UpdateCartQuantityUseCaseTest {

    private val repository = mockk<CartRepository>()
    private val useCase = UpdateCartQuantityUseCase(repository)

    @Test
    fun `invoke removes the item when quantity is set to zero`() = runBlocking {
        coEvery { repository.removeFromCart(1) } returns Unit

        useCase(productId = 1, quantity = 0)

        coVerify { repository.removeFromCart(1) }
    }

    @Test
    fun `invoke removes the item when quantity is negative`() = runBlocking {
        coEvery { repository.removeFromCart(1) } returns Unit

        useCase(productId = 1, quantity = -3)

        coVerify { repository.removeFromCart(1) }
    }

    @Test
    fun `invoke updates the quantity normally for positive values`() = runBlocking {
        coEvery { repository.updateQuantity(1, 5) } returns Unit

        useCase(productId = 1, quantity = 5)

        coVerify { repository.updateQuantity(1, 5) }
    }
}

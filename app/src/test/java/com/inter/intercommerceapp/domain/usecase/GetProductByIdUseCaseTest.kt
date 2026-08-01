package com.inter.intercommerceapp.domain.usecase

import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.model.ProductResult
import com.inter.intercommerceapp.domain.repository.ProductRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProductByIdUseCaseTest {

    private val repository = mockk<ProductRepository>()
    private val useCase = GetProductByIdUseCase(repository)

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
    fun `invoke delegates to repository getProductById with the given id`() = runBlocking {
        val expected = Result.success(ProductResult(product(1), isFromCache = false))
        every { repository.getProductById(1) } returns flowOf(expected)

        val emitted = useCase(1).first()

        verify { repository.getProductById(1) }
        assertEquals(expected, emitted)
    }
}

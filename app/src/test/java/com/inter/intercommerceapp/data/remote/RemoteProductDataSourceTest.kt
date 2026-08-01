package com.inter.intercommerceapp.data.remote

import com.inter.intercommerceapp.data.remote.dto.ProductDto
import com.inter.intercommerceapp.data.remote.dto.ProductListResponseDto
import com.inter.intercommerceapp.domain.model.ProductError
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RemoteProductDataSourceTest {

    private val api = mockk<ProductApi>()
    private lateinit var dataSource: RemoteProductDataSource

    private val dto = ProductDto(
        id = 1,
        title = "iPhone 9",
        description = "An apple mobile.",
        category = "smartphones",
        price = 549.0,
        discountPercentage = 12.96,
        rating = 4.69,
        stock = 94,
        brand = "Apple",
        thumbnail = "https://cdn.dummyjson.com/product-images/1/thumbnail.webp",
        images = emptyList(),
    )

    @Before
    fun setUp() {
        dataSource = RemoteProductDataSource(api)
    }

    private fun httpException(code: Int) = HttpException(
        Response.error<Any>(code, "".toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun `getProducts returns Product list on success`() = runTest {
        coEvery { api.getProducts(limit = 10, skip = 0) } returns
            ProductListResponseDto(products = listOf(dto), total = 1, skip = 0, limit = 10)

        val result = dataSource.getProducts(limit = 10, skip = 0)

        assertEquals(1, result.size)
        assertEquals("iPhone 9", result.first().title)
    }

    @Test
    fun `an IOException is translated to NetworkUnavailable`() = runTest {
        coEvery { api.getProducts(any(), any()) } throws IOException("timeout")

        try {
            dataSource.getProducts(limit = 10, skip = 0)
            fail("expected ProductError.NetworkUnavailable")
        } catch (e: ProductError.NetworkUnavailable) {
            // expected
        }
    }

    @Test
    fun `an HTTP 404 is translated to NotFound`() = runTest {
        coEvery { api.getProductById(1) } throws httpException(404)

        try {
            dataSource.getProductById(1)
            fail("expected ProductError.NotFound")
        } catch (e: ProductError.NotFound) {
            assertEquals(1, e.id)
        }
    }

    @Test
    fun `an HTTP 500 is translated to ServerError`() = runTest {
        coEvery { api.getProducts(any(), any()) } throws httpException(500)

        try {
            dataSource.getProducts(limit = 10, skip = 0)
            fail("expected ProductError.ServerError")
        } catch (e: ProductError.ServerError) {
            // expected
        }
    }
}

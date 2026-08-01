package com.inter.intercommerceapp.data.repository

import app.cash.turbine.test
import com.inter.intercommerceapp.data.local.LocalProductDataSource
import com.inter.intercommerceapp.data.local.image.ProductImageCache
import com.inter.intercommerceapp.data.remote.RemoteProductDataSource
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.model.ProductError
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ProductRepositoryImplTest {

    private val remoteDataSource = mockk<RemoteProductDataSource>()
    private val localDataSource = mockk<LocalProductDataSource>()
    private val imageCache = mockk<ProductImageCache>()
    private lateinit var repository: ProductRepositoryImpl

    private val cachedProducts = MutableStateFlow<List<Product>>(emptyList())
    private val cachedProduct = MutableStateFlow<Product?>(null)

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

    @Before
    fun setUp() {
        every { localDataSource.getCachedProducts() } returns cachedProducts
        every { localDataSource.getCachedProductById(any()) } returns cachedProduct
        coEvery { imageCache.getOrDownload(any(), any()) } returns null
        repository = ProductRepositoryImpl(remoteDataSource, localDataSource, imageCache)
    }

    @Test
    fun `getProducts emits fresh data and writes to cache on a successful remote fetch`() = runTest {
        val remoteList = listOf(product(1))
        coEvery { remoteDataSource.getProducts(10, 0) } returns remoteList
        coEvery { localDataSource.replaceCachedProducts(remoteList) } answers { cachedProducts.value = remoteList }

        repository.getProducts(10, 0).test {
            val result = awaitItem()
            assertEquals(remoteList, result.products)
            assertFalse(result.isFromCache)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { localDataSource.replaceCachedProducts(remoteList) }
    }

    @Test
    fun `getProducts falls back to cached data when the remote fetch fails and cache is not empty`() = runTest {
        cachedProducts.value = listOf(product(1))
        coEvery { remoteDataSource.getProducts(10, 0) } throws ProductError.NetworkUnavailable()

        repository.getProducts(10, 0).test {
            val result = awaitItem()
            assertEquals(1, result.products.size)
            assertTrue(result.isFromCache)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProducts propagates the error when the remote fetch fails and the cache is empty`() = runTest {
        coEvery { remoteDataSource.getProducts(10, 0) } throws ProductError.NetworkUnavailable()

        repository.getProducts(10, 0).test {
            assertTrue(awaitError() is ProductError.NetworkUnavailable)
        }
    }

    @Test
    fun `getProductById emits fresh data and writes to cache on a successful remote fetch`() = runTest {
        val remoteProduct = product(1)
        coEvery { remoteDataSource.getProductById(1) } returns remoteProduct
        coEvery { localDataSource.cacheProduct(remoteProduct) } answers { cachedProduct.value = remoteProduct }

        repository.getProductById(1).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.product?.id)
            assertFalse(result.getOrNull()?.isFromCache ?: true)
            cancelAndIgnoreRemainingEvents()
        }
        coVerify { localDataSource.cacheProduct(remoteProduct) }
    }

    @Test
    fun `getProductById falls back to the cached product when the remote fetch fails`() = runTest {
        cachedProduct.value = product(1)
        coEvery { remoteDataSource.getProductById(1) } throws ProductError.NetworkUnavailable()

        repository.getProductById(1).test {
            val result = awaitItem()
            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrNull()?.product?.id)
            assertTrue(result.getOrNull()?.isFromCache ?: false)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getProductById propagates the error when neither remote nor cache has the product`() = runTest {
        coEvery { remoteDataSource.getProductById(1) } throws ProductError.NetworkUnavailable()

        repository.getProductById(1).test {
            assertTrue(awaitError() is ProductError.NetworkUnavailable)
        }
    }

    @Test
    fun `searchProducts falls back to the previous result for the same query when remote fails`() = runTest {
        coEvery { remoteDataSource.searchProducts("phone") } returns listOf(product(1))
        repository.searchProducts("phone").test {
            awaitItem()
            awaitComplete()
        }

        coEvery { remoteDataSource.searchProducts("phone") } throws ProductError.NetworkUnavailable()
        repository.searchProducts("phone").test {
            val result = awaitItem()
            assertTrue(result.isFromCache)
            assertEquals(1, result.products.size)
            awaitComplete()
        }
    }

    @Test
    fun `searchProducts propagates the error for a different query with no matching cache`() = runTest {
        coEvery { remoteDataSource.searchProducts("phone") } returns listOf(product(1))
        repository.searchProducts("phone").test {
            awaitItem()
            awaitComplete()
        }

        coEvery { remoteDataSource.searchProducts("laptop") } throws ProductError.NetworkUnavailable()
        repository.searchProducts("laptop").test {
            assertTrue(awaitError() is ProductError.NetworkUnavailable)
        }
    }
}

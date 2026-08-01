package com.inter.intercommerceapp.data.local

import app.cash.turbine.test
import com.inter.intercommerceapp.data.local.db.ProductDao
import com.inter.intercommerceapp.data.local.db.entity.ProductEntity
import com.inter.intercommerceapp.data.mapper.ProductMapper
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class LocalProductDataSourceTest {

    private val dao = mockk<ProductDao>()
    private lateinit var dataSource: LocalProductDataSource

    @Before
    fun setUp() {
        dataSource = LocalProductDataSource(dao)
    }

    private fun entity(id: Int) = ProductEntity(
        id = id,
        title = "Product $id",
        description = "desc",
        price = 10.0,
        discountPercentage = 0.0,
        rating = 4.0,
        stock = 1,
        brand = null,
        category = "misc",
        thumbnail = "thumb",
        images = emptyList(),
        cachedAt = 0L,
    )

    @Test
    fun `getCachedProducts emits mapped products reactively as the cache changes`() = runTest {
        val cache = MutableStateFlow(emptyList<ProductEntity>())
        every { dao.getAll() } returns cache

        dataSource.getCachedProducts().test {
            assertEquals(emptyList<Any>(), awaitItem())

            cache.value = listOf(entity(1))

            val emitted = awaitItem()
            assertEquals(1, emitted.size)
            assertEquals("Product 1", emitted.first().title)
        }
    }

    @Test
    fun `getCachedProductById emits null when the product is not cached`() = runTest {
        every { dao.getById(99) } returns MutableStateFlow(null)

        dataSource.getCachedProductById(99).test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun `replaceCachedProducts delegates to dao replaceAll via the mapper`() = runTest {
        val slot = slot<List<ProductEntity>>()
        coEvery { dao.replaceAll(capture(slot)) } returns Unit

        dataSource.replaceCachedProducts(listOf(ProductMapper.toDomain(entity(1))))

        assertEquals(1, slot.captured.size)
        assertEquals(1, slot.captured.first().id)
    }

    @Test
    fun `clearCache delegates to dao clear`() = runTest {
        coEvery { dao.clear() } returns Unit

        dataSource.clearCache()

        coVerify { dao.clear() }
    }
}

package com.inter.intercommerceapp.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.inter.intercommerceapp.data.local.db.entity.ProductEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ProductDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.productDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun product(id: Int) = ProductEntity(
        id = id,
        title = "Product $id",
        description = "description",
        price = 9.99,
        discountPercentage = 0.0,
        rating = 4.5,
        stock = 10,
        brand = "brand",
        category = "category",
        thumbnail = "thumbnail.png",
        images = listOf("a.png", "b.png"),
        cachedAt = 1_000L,
    )

    @Test
    fun replaceAll_persistsProducts_readableViaGetAll() = runTest {
        dao.replaceAll(listOf(product(1), product(2)))

        dao.getAll().test {
            assertEquals(2, awaitItem().size)
        }
    }

    @Test
    fun replaceAll_calledTwice_fullyReplacesPreviousData() = runTest {
        dao.replaceAll(listOf(product(1), product(2)))
        dao.replaceAll(listOf(product(3)))

        dao.getAll().test {
            assertEquals(listOf(3), awaitItem().map { it.id })
        }
    }

    @Test
    fun getById_returnsMatch_andNullWhenMissing() = runTest {
        dao.replaceAll(listOf(product(1)))

        dao.getById(1).test {
            assertEquals(1, awaitItem()?.id)
        }
        dao.getById(99).test {
            assertNull(awaitItem())
        }
    }

    @Test
    fun clear_emptiesTheCatalog() = runTest {
        dao.replaceAll(listOf(product(1), product(2)))
        dao.clear()

        dao.getAll().test {
            assertTrue(awaitItem().isEmpty())
        }
    }
}

package com.inter.intercommerceapp.data.local.db

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.inter.intercommerceapp.data.local.db.entity.CartItemEntity
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: CartDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java,
        ).allowMainThreadQueries().build()
        dao = database.cartDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    private fun cartItem(productId: Int, quantity: Int = 1) = CartItemEntity(
        productId = productId,
        title = "Product $productId",
        thumbnailUrl = "thumbnail.png",
        unitPrice = 9.99,
        discountPercentage = 0.0,
        quantity = quantity,
        addedAt = 1_000L,
    )

    @Test
    fun upsert_newProductId_insertsRowWithGivenQuantity() = runTest {
        dao.upsert(cartItem(productId = 1, quantity = 2))

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(2, items.single().quantity)
        }
    }

    @Test
    fun upsert_existingProductId_incrementsQuantityInsteadOfDuplicating() = runTest {
        dao.upsert(cartItem(productId = 1, quantity = 2))
        dao.upsert(cartItem(productId = 1, quantity = 3))

        dao.getAll().test {
            val items = awaitItem()
            assertEquals(1, items.size)
            assertEquals(5, items.single().quantity)
        }
    }

    @Test
    fun updateQuantity_changesQuantityOfExistingRow() = runTest {
        dao.upsert(cartItem(productId = 1, quantity = 1))

        dao.updateQuantity(productId = 1, quantity = 7)

        dao.getAll().test {
            assertEquals(7, awaitItem().single().quantity)
        }
    }

    @Test
    fun removeItem_removesOnlyTargetedRow() = runTest {
        dao.upsert(cartItem(productId = 1))
        dao.upsert(cartItem(productId = 2))

        dao.removeItem(productId = 1)

        dao.getAll().test {
            assertEquals(listOf(2), awaitItem().map { it.productId })
        }
    }

    @Test
    fun getAll_emitsUpdatedList_afterInsertUpdateRemoveAndClear() = runTest {
        dao.getAll().test {
            assertTrue(awaitItem().isEmpty())

            dao.upsert(cartItem(productId = 1, quantity = 1))
            assertEquals(1, awaitItem().single().quantity)

            dao.updateQuantity(productId = 1, quantity = 4)
            assertEquals(4, awaitItem().single().quantity)

            dao.removeItem(productId = 1)
            assertTrue(awaitItem().isEmpty())
        }
    }

    @Test
    fun clear_emptiesTheCart() = runTest {
        dao.upsert(cartItem(productId = 1))
        dao.upsert(cartItem(productId = 2))

        dao.clear()

        dao.getAll().test {
            assertTrue(awaitItem().isEmpty())
        }
    }
}

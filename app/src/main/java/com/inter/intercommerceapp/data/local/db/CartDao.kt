package com.inter.intercommerceapp.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.inter.intercommerceapp.data.local.db.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items")
    fun getAll(): Flow<List<CartItemEntity>>

    @Query("SELECT * FROM cart_items WHERE productId = :productId")
    suspend fun getByProductId(productId: Int): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :quantity WHERE productId = :productId")
    suspend fun updateQuantity(productId: Int, quantity: Int)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun removeItem(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clear()

    @Transaction
    suspend fun upsert(item: CartItemEntity) {
        val existing = getByProductId(item.productId)
        if (existing == null) {
            insert(item)
        } else {
            updateQuantity(item.productId, existing.quantity + item.quantity)
        }
    }
}

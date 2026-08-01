package com.inter.intercommerceapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.inter.intercommerceapp.data.local.db.entity.CartItemEntity
import com.inter.intercommerceapp.data.local.db.entity.ProductEntity

@Database(entities = [ProductEntity::class, CartItemEntity::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun cartDao(): CartDao
}

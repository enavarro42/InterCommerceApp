package com.inter.intercommerceapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: Int,
    val title: String,
    val thumbnailUrl: String,
    val unitPrice: Double,
    val discountPercentage: Double,
    val quantity: Int,
    val addedAt: Long,
)

package com.inter.intercommerceapp.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val description: String,
    val price: Double,
    val discountPercentage: Double,
    val rating: Double,
    val stock: Int,
    val brand: String?,
    val category: String,
    val thumbnail: String,
    val images: List<String>,
    val localThumbnailPath: String? = null,
    val cachedAt: Long,
)

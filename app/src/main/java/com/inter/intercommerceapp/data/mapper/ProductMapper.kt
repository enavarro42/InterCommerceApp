package com.inter.intercommerceapp.data.mapper

import com.inter.intercommerceapp.data.local.db.entity.ProductEntity
import com.inter.intercommerceapp.data.remote.dto.ProductDto
import com.inter.intercommerceapp.domain.model.Product

object ProductMapper {

    fun toDomain(dto: ProductDto): Product = Product(
        id = dto.id,
        title = dto.title,
        description = dto.description,
        category = dto.category,
        price = dto.price,
        discountPercentage = dto.discountPercentage,
        rating = dto.rating,
        stock = dto.stock,
        brand = dto.brand,
        thumbnail = dto.thumbnail,
        images = dto.images,
    )

    fun toDomain(entity: ProductEntity): Product = Product(
        id = entity.id,
        title = entity.title,
        description = entity.description,
        category = entity.category,
        price = entity.price,
        discountPercentage = entity.discountPercentage,
        rating = entity.rating,
        stock = entity.stock,
        brand = entity.brand,
        thumbnail = entity.thumbnail,
        images = entity.images,
    )

    fun toEntity(product: Product, cachedAt: Long = System.currentTimeMillis()): ProductEntity = ProductEntity(
        id = product.id,
        title = product.title,
        description = product.description,
        price = product.price,
        discountPercentage = product.discountPercentage,
        rating = product.rating,
        stock = product.stock,
        brand = product.brand,
        category = product.category,
        thumbnail = product.thumbnail,
        images = product.images,
        cachedAt = cachedAt,
    )
}

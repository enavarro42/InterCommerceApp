package com.inter.intercommerceapp.data.mapper

import com.inter.intercommerceapp.data.local.db.entity.ProductEntity
import com.inter.intercommerceapp.data.remote.dto.ProductDto
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductMapperTest {

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
        images = listOf("https://cdn.dummyjson.com/product-images/1/1.webp"),
    )

    @Test
    fun `toDomain maps a ProductDto to the expected Product`() {
        val product = ProductMapper.toDomain(dto)

        assertEquals(dto.id, product.id)
        assertEquals(dto.title, product.title)
        assertEquals(dto.category, product.category)
        assertEquals(dto.price, product.price, 0.0)
        assertEquals(dto.brand, product.brand)
        assertEquals(dto.images, product.images)
    }

    @Test
    fun `toEntity maps a Product to the expected ProductEntity`() {
        val product = ProductMapper.toDomain(dto)

        val entity = ProductMapper.toEntity(product, cachedAt = 1_000L)

        assertEquals(product.id, entity.id)
        assertEquals(product.title, entity.title)
        assertEquals(product.category, entity.category)
        assertEquals(product.price, entity.price, 0.0)
        assertEquals(product.brand, entity.brand)
        assertEquals(product.images, entity.images)
        assertEquals(1_000L, entity.cachedAt)
    }

    @Test
    fun `toDomain maps a ProductEntity back to the expected Product`() {
        val entity = ProductEntity(
            id = 1,
            title = "iPhone 9",
            description = "An apple mobile.",
            price = 549.0,
            discountPercentage = 12.96,
            rating = 4.69,
            stock = 94,
            brand = "Apple",
            category = "smartphones",
            thumbnail = "https://cdn.dummyjson.com/product-images/1/thumbnail.webp",
            images = listOf("https://cdn.dummyjson.com/product-images/1/1.webp"),
            cachedAt = 1_000L,
        )

        val product = ProductMapper.toDomain(entity)

        assertEquals(entity.id, product.id)
        assertEquals(entity.title, product.title)
        assertEquals(entity.category, product.category)
        assertEquals(entity.price, product.price, 0.0)
        assertEquals(entity.brand, product.brand)
        assertEquals(entity.images, product.images)
    }
}

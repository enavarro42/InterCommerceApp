package com.inter.intercommerceapp.data.mapper

import com.inter.intercommerceapp.data.local.db.entity.CartItemEntity
import com.inter.intercommerceapp.domain.model.Product
import org.junit.Assert.assertEquals
import org.junit.Test

class CartMapperTest {

    private val product = Product(
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
        localThumbnailPath = "/data/data/app/files/product_images/1.jpg",
    )

    @Test
    fun `toEntity snapshots the product's title, thumbnail, price, discount, and local thumbnail path`() {
        val entity = CartMapper.toEntity(product, quantity = 2, addedAt = 1_000L)

        assertEquals(product.id, entity.productId)
        assertEquals(product.title, entity.title)
        assertEquals(product.thumbnail, entity.thumbnailUrl)
        assertEquals(product.price, entity.unitPrice, 0.0)
        assertEquals(product.discountPercentage, entity.discountPercentage, 0.0)
        assertEquals(2, entity.quantity)
        assertEquals(1_000L, entity.addedAt)
        assertEquals(product.localThumbnailPath, entity.localThumbnailPath)
    }

    @Test
    fun `toDomain maps a CartItemEntity to the expected CartItem`() {
        val entity = CartItemEntity(
            productId = 1,
            title = "iPhone 9",
            thumbnailUrl = "https://cdn.dummyjson.com/product-images/1/thumbnail.webp",
            unitPrice = 549.0,
            discountPercentage = 12.96,
            quantity = 3,
            addedAt = 1_000L,
            localThumbnailPath = "/data/data/app/files/product_images/1.jpg",
        )

        val cartItem = CartMapper.toDomain(entity)

        assertEquals(entity.productId, cartItem.productId)
        assertEquals(entity.title, cartItem.title)
        assertEquals(entity.thumbnailUrl, cartItem.thumbnailUrl)
        assertEquals(entity.unitPrice, cartItem.unitPrice, 0.0)
        assertEquals(entity.discountPercentage, cartItem.discountPercentage, 0.0)
        assertEquals(entity.quantity, cartItem.quantity)
        assertEquals(entity.localThumbnailPath, cartItem.localThumbnailPath)
    }
}

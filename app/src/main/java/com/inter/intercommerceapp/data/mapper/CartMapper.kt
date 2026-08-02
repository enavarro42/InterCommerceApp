package com.inter.intercommerceapp.data.mapper

import com.inter.intercommerceapp.data.local.db.entity.CartItemEntity
import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.Product

object CartMapper {

    fun toEntity(product: Product, quantity: Int, addedAt: Long = System.currentTimeMillis()): CartItemEntity =
        CartItemEntity(
            productId = product.id,
            title = product.title,
            thumbnailUrl = product.thumbnail,
            unitPrice = product.price,
            discountPercentage = product.discountPercentage,
            quantity = quantity,
            addedAt = addedAt,
            localThumbnailPath = product.localThumbnailPath,
        )

    fun toDomain(entity: CartItemEntity): CartItem = CartItem(
        productId = entity.productId,
        title = entity.title,
        thumbnailUrl = entity.thumbnailUrl,
        unitPrice = entity.unitPrice,
        discountPercentage = entity.discountPercentage,
        quantity = entity.quantity,
        localThumbnailPath = entity.localThumbnailPath,
    )
}

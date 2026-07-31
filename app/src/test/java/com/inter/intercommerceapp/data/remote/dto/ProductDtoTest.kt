package com.inter.intercommerceapp.data.remote.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes a single product matching DummyJSON's shape`() {
        val body = """
            {
              "id": 1,
              "title": "Essence Mascara Lash Princess",
              "description": "The Essence Mascara Lash Princess is a popular mascara.",
              "category": "beauty",
              "price": 9.99,
              "discountPercentage": 10.48,
              "rating": 2.56,
              "stock": 99,
              "brand": "Essence",
              "thumbnail": "https://cdn.dummyjson.com/product-images/1/thumbnail.webp",
              "images": [
                "https://cdn.dummyjson.com/product-images/1/1.webp"
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductDto>(body)

        assertEquals(1, dto.id)
        assertEquals("Essence Mascara Lash Princess", dto.title)
        assertEquals("beauty", dto.category)
        assertEquals(9.99, dto.price, 0.0)
        assertEquals("Essence", dto.brand)
        assertEquals(1, dto.images.size)
    }

    @Test
    fun `decodes a paginated product list response`() {
        val body = """
            {
              "products": [
                {
                  "id": 1,
                  "title": "Essence Mascara Lash Princess",
                  "description": "The Essence Mascara Lash Princess is a popular mascara.",
                  "category": "beauty",
                  "price": 9.99,
                  "discountPercentage": 10.48,
                  "rating": 2.56,
                  "stock": 99,
                  "brand": "Essence",
                  "thumbnail": "https://cdn.dummyjson.com/product-images/1/thumbnail.webp",
                  "images": []
                }
              ],
              "total": 194,
              "skip": 0,
              "limit": 30
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProductListResponseDto>(body)

        assertEquals(1, dto.products.size)
        assertEquals(194, dto.total)
        assertEquals(0, dto.skip)
        assertEquals(30, dto.limit)
    }
}

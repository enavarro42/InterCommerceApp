package com.inter.intercommerceapp.data.remote

import com.inter.intercommerceapp.data.remote.dto.ProductDto
import com.inter.intercommerceapp.data.remote.dto.ProductListResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProductApi {

    @GET("products")
    suspend fun getProducts(
        @Query("limit") limit: Int,
        @Query("skip") skip: Int,
    ): ProductListResponseDto

    @GET("products/search")
    suspend fun searchProducts(
        @Query("q") query: String,
    ): ProductListResponseDto

    @GET("products/{id}")
    suspend fun getProductById(
        @Path("id") id: Int,
    ): ProductDto
}

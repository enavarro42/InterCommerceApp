package com.inter.intercommerceapp.data.remote.client

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object RetrofitFactory {

    private const val BASE_URL = "https://dummyjson.com/"

    private val json = Json { ignoreUnknownKeys = true }

    fun create(okHttpClient: OkHttpClient = OkHttpClientFactory.create()): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
}

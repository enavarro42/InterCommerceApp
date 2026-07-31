package com.inter.intercommerceapp.data.remote.client

import com.inter.intercommerceapp.BuildConfig
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object OkHttpClientFactory {

    private val CONNECT_TIMEOUT = 10L to TimeUnit.SECONDS
    private val READ_TIMEOUT = 15L to TimeUnit.SECONDS
    private val WRITE_TIMEOUT = 15L to TimeUnit.SECONDS

    fun create(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

        return OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT.first, CONNECT_TIMEOUT.second)
            .readTimeout(READ_TIMEOUT.first, READ_TIMEOUT.second)
            .writeTimeout(WRITE_TIMEOUT.first, WRITE_TIMEOUT.second)
            .addInterceptor(loggingInterceptor)
            .build()
    }
}

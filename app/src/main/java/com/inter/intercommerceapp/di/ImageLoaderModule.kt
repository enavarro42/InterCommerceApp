package com.inter.intercommerceapp.di

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.OkHttpClient

private const val DISK_CACHE_MAX_SIZE_BYTES = 50L * 1024 * 1024
private const val MEMORY_CACHE_MAX_SIZE_PERCENT = 0.25

@Module
@InstallIn(SingletonComponent::class)
object ImageLoaderModule {

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
    ): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(MEMORY_CACHE_MAX_SIZE_PERCENT)
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(context.cacheDir.resolve("product_thumbnail_cache"))
                .maxSizeBytes(DISK_CACHE_MAX_SIZE_BYTES)
                .build()
        }
        .build()
}

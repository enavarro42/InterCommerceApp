package com.inter.intercommerceapp.data.local.image

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

@Singleton
class ProductImageCache @Inject constructor(
    @ApplicationContext private val context: Context,
    private val okHttpClient: OkHttpClient,
) {

    private val imagesDir: File by lazy {
        File(context.filesDir, "product_images").apply { mkdirs() }
    }

    // Persisted under filesDir (not cacheDir) so it survives alongside the Room cache
    // instead of being wiped whenever the OS or user clears the app's cache.
    suspend fun getOrDownload(productId: Int, url: String): String? = withContext(Dispatchers.IO) {
        val file = File(imagesDir, "$productId.jpg")
        if (file.exists() && file.length() > 0) return@withContext file.absolutePath

        try {
            val request = Request.Builder().url(url).build()
            okHttpClient.newCall(request).execute().use { response ->
                val body = response.body
                if (!response.isSuccessful || body == null) return@withContext null
                file.outputStream().use { output -> body.byteStream().copyTo(output) }
            }
            file.absolutePath
        } catch (e: IOException) {
            file.delete()
            null
        }
    }
}

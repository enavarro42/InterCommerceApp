package com.inter.intercommerceapp.data.remote

import java.util.concurrent.TimeUnit
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class ProductApiTest {

    private lateinit var server: MockWebServer
    private val json = Json { ignoreUnknownKeys = true }

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    private fun createApi(client: OkHttpClient = OkHttpClient()): ProductApi {
        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
        return retrofit.create(ProductApi::class.java)
    }

    @Test
    fun `getProducts builds the expected request URL and query params`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"products": [], "total": 0, "skip": 0, "limit": 10}"""
            )
        )

        val api = createApi()
        val response = api.getProducts(limit = 10, skip = 0)

        val recordedRequest = server.takeRequest()
        assertEquals("/products?limit=10&skip=0", recordedRequest.path)
        assertEquals(0, response.products.size)
    }

    @Test
    fun `searchProducts builds the expected search URL and parses the response`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """
                {
                  "products": [
                    {
                      "id": 1,
                      "title": "iPhone 9",
                      "description": "An apple mobile.",
                      "category": "smartphones",
                      "price": 549,
                      "discountPercentage": 12.96,
                      "rating": 4.69,
                      "stock": 94,
                      "brand": "Apple",
                      "thumbnail": "https://cdn.dummyjson.com/product-images/1/thumbnail.webp",
                      "images": []
                    }
                  ],
                  "total": 1,
                  "skip": 0,
                  "limit": 1
                }
                """.trimIndent()
            )
        )

        val api = createApi()
        val response = api.searchProducts(query = "phone")

        val recordedRequest = server.takeRequest()
        assertEquals("/products/search?q=phone", recordedRequest.path)
        assertEquals(1, response.products.size)
        assertEquals("iPhone 9", response.products.first().title)
    }

    @Test
    fun `a non-responding server causes the call to fail with a timeout`() = runTest {
        server.enqueue(
            MockResponse().setSocketPolicy(SocketPolicy.NO_RESPONSE)
        )

        val shortTimeoutClient = OkHttpClient.Builder()
            .readTimeout(200, TimeUnit.MILLISECONDS)
            .build()
        val api = createApi(shortTimeoutClient)

        val startTime = System.nanoTime()
        var timedOut = false
        try {
            api.getProductById(id = 1)
        } catch (e: Exception) {
            timedOut = true
        }
        val elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime)

        assertTrue("expected the call to fail with a timeout", timedOut)
        assertTrue("expected the failure well within a couple of seconds", elapsedMillis < 5_000)
    }
}

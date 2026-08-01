package com.inter.intercommerceapp.presentation.components

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import coil.Coil
import coil.ImageLoader
import coil.annotation.ExperimentalCoilApi
import coil.request.ErrorResult
import coil.test.FakeImageLoaderEngine
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.ui.theme.InterCommerceAppTheme
import kotlinx.coroutines.CompletableDeferred
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoilApi::class)
class ProductCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val product = Product(
        id = 1,
        title = "Product 1",
        description = "desc",
        category = "misc",
        price = 9.99,
        discountPercentage = 0.0,
        rating = 4.0,
        stock = 1,
        brand = null,
        thumbnail = "https://example.com/thumb.jpg",
        images = emptyList(),
    )

    @After
    fun tearDown() {
        Coil.reset()
    }

    private fun setFakeImageLoader(engine: FakeImageLoaderEngine) {
        val context = ApplicationProvider.getApplicationContext<Context>()
        Coil.setImageLoader(ImageLoader.Builder(context).components { add(engine) }.build())
    }

    @Test
    fun placeholderIsShownWhileImageIsLoading() {
        val neverCompletes = CompletableDeferred<Unit>()
        setFakeImageLoader(
            FakeImageLoaderEngine.Builder()
                .intercept(predicate = { it == product.thumbnail }) { _ ->
                    neverCompletes.await()
                    null
                }
                .build(),
        )

        composeTestRule.setContent {
            InterCommerceAppTheme {
                ProductCard(product = product, onClick = {})
            }
        }

        composeTestRule.onNodeWithTag(PRODUCT_CARD_IMAGE_LOADING_TEST_TAG).assertExists()
    }

    @Test
    fun errorPlaceholderIsShownWhenImageFails() {
        setFakeImageLoader(
            FakeImageLoaderEngine.Builder()
                .intercept(predicate = { it == product.thumbnail }) { chain ->
                    ErrorResult(
                        drawable = null,
                        request = chain.request,
                        throwable = IllegalStateException("image request failed"),
                    )
                }
                .build(),
        )

        composeTestRule.setContent {
            InterCommerceAppTheme {
                ProductCard(product = product, onClick = {})
            }
        }

        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule
                .onAllNodesWithTag(PRODUCT_CARD_IMAGE_ERROR_TEST_TAG)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(PRODUCT_CARD_IMAGE_ERROR_TEST_TAG).assertExists()
    }

    @Test
    fun tappingCardInvokesOnClickExactlyOnce() {
        setFakeImageLoader(FakeImageLoaderEngine.Builder().default(ColorDrawable(Color.RED)).build())
        var clickCount = 0

        composeTestRule.setContent {
            InterCommerceAppTheme {
                ProductCard(product = product, onClick = { clickCount++ })
            }
        }

        composeTestRule.onNodeWithTag(productCardTestTag(product.id)).performClick()
        assertEquals(1, clickCount)
    }
}

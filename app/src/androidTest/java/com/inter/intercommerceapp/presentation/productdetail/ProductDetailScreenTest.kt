package com.inter.intercommerceapp.presentation.productdetail

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.ui.theme.InterCommerceAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class ProductDetailScreenTest {

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
        thumbnail = "",
        images = emptyList(),
    )

    private fun setContent(uiState: ProductDetailUiState, onRetry: () -> Unit = {}) {
        composeTestRule.setContent {
            InterCommerceAppTheme {
                ProductDetailScreen(
                    uiState = uiState,
                    onRetry = onRetry,
                    onBack = {},
                )
            }
        }
    }

    @Test
    fun shimmerIsShownWhileLoadingWithNoProduct() {
        setContent(ProductDetailUiState(isLoading = true, product = null))

        composeTestRule.onNodeWithTag(PRODUCT_DETAIL_SHIMMER_TEST_TAG).assertExists()
    }

    @Test
    fun errorStateWithRetryIsShownWhenErrorAndNoProduct() {
        var retried = false
        setContent(
            ProductDetailUiState(errorMessage = "Network error", product = null),
            onRetry = { retried = true },
        )

        composeTestRule.onNodeWithTag(PRODUCT_DETAIL_ERROR_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(PRODUCT_DETAIL_RETRY_BUTTON_TEST_TAG).performClick()
        assertTrue(retried)
    }

    @Test
    fun errorStateIsHiddenWhenProductIsPresent() {
        setContent(ProductDetailUiState(errorMessage = "Network error", product = product))

        composeTestRule.onNodeWithTag(PRODUCT_DETAIL_ERROR_TEST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(PRODUCT_DETAIL_CONTENT_TEST_TAG).assertExists()
    }

    @Test
    fun offlineBannerIsShownOnlyWhenFromCache() {
        setContent(ProductDetailUiState(product = product, isFromCache = true))

        composeTestRule.onNodeWithTag(PRODUCT_DETAIL_OFFLINE_BANNER_TEST_TAG).assertExists()
    }

    @Test
    fun offlineBannerIsHiddenWhenNotFromCache() {
        setContent(ProductDetailUiState(product = product, isFromCache = false))

        composeTestRule.onNodeWithTag(PRODUCT_DETAIL_OFFLINE_BANNER_TEST_TAG).assertDoesNotExist()
    }
}

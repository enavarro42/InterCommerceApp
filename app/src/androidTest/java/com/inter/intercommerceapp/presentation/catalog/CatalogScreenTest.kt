package com.inter.intercommerceapp.presentation.catalog

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.ui.theme.InterCommerceAppTheme
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CatalogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun product(id: Int) = Product(
        id = id,
        title = "Product $id",
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

    private fun setContent(uiState: CatalogUiState, onRetry: () -> Unit = {}) {
        composeTestRule.setContent {
            InterCommerceAppTheme {
                CatalogScreen(
                    uiState = uiState,
                    onSearchQueryChanged = {},
                    onLoadNextPage = {},
                    onRetry = onRetry,
                )
            }
        }
    }

    @Test
    fun shimmerIsShownWhileLoadingWithNoProducts() {
        setContent(CatalogUiState(isLoading = true, products = emptyList()))

        composeTestRule.onNodeWithTag(CATALOG_SHIMMER_TEST_TAG).assertExists()
    }

    @Test
    fun gridRendersOneTilePerProduct() {
        val products = (1..4).map(::product)
        setContent(CatalogUiState(isLoading = false, products = products))

        products.forEach { product ->
            composeTestRule.onNodeWithTag(productGridItemTestTag(product.id)).assertExists()
        }
    }

    @Test
    fun offlineBannerIsShownOnlyWhenFromCache() {
        setContent(CatalogUiState(products = listOf(product(1)), isFromCache = true))

        composeTestRule.onNodeWithTag(CATALOG_OFFLINE_BANNER_TEST_TAG).assertExists()
    }

    @Test
    fun offlineBannerIsHiddenWhenNotFromCache() {
        setContent(CatalogUiState(products = listOf(product(1)), isFromCache = false))

        composeTestRule.onNodeWithTag(CATALOG_OFFLINE_BANNER_TEST_TAG).assertDoesNotExist()
    }

    @Test
    fun errorStateWithRetryIsShownWhenErrorAndNoProducts() {
        var retried = false
        setContent(
            CatalogUiState(errorMessage = "Network error", products = emptyList()),
            onRetry = { retried = true },
        )

        composeTestRule.onNodeWithTag(CATALOG_ERROR_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(CATALOG_RETRY_BUTTON_TEST_TAG).performClick()
        assertTrue(retried)
    }

    @Test
    fun errorStateIsHiddenWhenProductsArePresent() {
        setContent(CatalogUiState(errorMessage = "Network error", products = listOf(product(1))))

        composeTestRule.onNodeWithTag(CATALOG_ERROR_TEST_TAG).assertDoesNotExist()
    }
}

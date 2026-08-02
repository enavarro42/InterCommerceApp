package com.inter.intercommerceapp.presentation.cart

import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.CartTotals
import com.inter.intercommerceapp.ui.theme.InterCommerceAppTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class CartScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun cartItem(productId: Int, quantity: Int = 1) = CartItem(
        productId = productId,
        title = "Product $productId",
        thumbnailUrl = "",
        unitPrice = 10.0,
        discountPercentage = 0.0,
        quantity = quantity,
    )

    private fun setContent(
        uiState: CartUiState,
        onQuantityChanged: (Int, Int) -> Unit = { _, _ -> },
        onRemoveItem: (Int) -> Unit = {},
    ) {
        composeTestRule.setContent {
            InterCommerceAppTheme {
                CartScreen(
                    uiState = uiState,
                    onQuantityChanged = onQuantityChanged,
                    onRemoveItem = onRemoveItem,
                )
            }
        }
    }

    @Test
    fun cartItemsRenderWithTitlePriceAndQuantity() {
        setContent(CartUiState(items = listOf(cartItem(productId = 1, quantity = 3)), isEmpty = false))

        composeTestRule.onNodeWithText("Product 1").assertExists()
        composeTestRule.onNodeWithText("$10.0").assertExists()
        composeTestRule.onNodeWithTag(cartItemQuantityTestTag(1)).assertExists()
        composeTestRule.onNodeWithTag(cartItemQuantityTestTag(1)).assert(hasText("3"))
    }

    @Test
    fun quantityStepperInvokesOnQuantityChangedWithExpectedValue() {
        var changedProductId: Int? = null
        var changedQuantity: Int? = null
        setContent(
            CartUiState(items = listOf(cartItem(productId = 1, quantity = 2)), isEmpty = false),
            onQuantityChanged = { productId, quantity ->
                changedProductId = productId
                changedQuantity = quantity
            },
        )

        composeTestRule.onNodeWithTag(cartItemIncrementTestTag(1)).performClick()

        assertEquals(1, changedProductId)
        assertEquals(3, changedQuantity)
    }

    @Test
    fun removeActionInvokesOnRemoveItemForCorrectItem() {
        var removedProductId: Int? = null
        setContent(
            CartUiState(
                items = listOf(cartItem(productId = 1), cartItem(productId = 2)),
                isEmpty = false,
            ),
            onRemoveItem = { productId -> removedProductId = productId },
        )

        composeTestRule.onNodeWithTag(cartItemRemoveTestTag(2)).performClick()

        assertEquals(2, removedProductId)
    }

    @Test
    fun totalsSummaryDisplaysValuesFromUiState() {
        setContent(
            CartUiState(
                items = listOf(cartItem(productId = 1)),
                totals = CartTotals(subtotal = 100.0, discountAmount = 10.0, taxAmount = 7.2, total = 97.2),
                isEmpty = false,
            ),
        )

        composeTestRule.onNodeWithTag(CART_TOTALS_TEST_TAG).assertExists()
        composeTestRule.onNodeWithText("Subtotal: $100.0").assertExists()
        composeTestRule.onNodeWithText("Descuento: -$10.0").assertExists()
        composeTestRule.onNodeWithText("Impuestos: $7.2").assertExists()
        composeTestRule.onNodeWithText("Total: $97.2").assertExists()
    }

    @Test
    fun emptyCartStateIsShownAndListAndTotalsAreNotShownWhenEmpty() {
        setContent(CartUiState(items = emptyList(), isEmpty = true))

        composeTestRule.onNodeWithTag(CART_EMPTY_STATE_TEST_TAG).assertExists()
        composeTestRule.onNodeWithTag(CART_LIST_TEST_TAG).assertDoesNotExist()
        composeTestRule.onNodeWithTag(CART_TOTALS_TEST_TAG).assertDoesNotExist()
    }
}

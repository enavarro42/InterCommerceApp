package com.inter.intercommerceapp.presentation.cart

import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.usecase.CalculateCartTotalsUseCase
import com.inter.intercommerceapp.domain.usecase.ClearCartUseCase
import com.inter.intercommerceapp.domain.usecase.ObserveCartUseCase
import com.inter.intercommerceapp.domain.usecase.RemoveFromCartUseCase
import com.inter.intercommerceapp.domain.usecase.UpdateCartQuantityUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CartViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val observeCartUseCase = mockk<ObserveCartUseCase>()
    private val calculateCartTotalsUseCase = CalculateCartTotalsUseCase()
    private val updateCartQuantityUseCase = mockk<UpdateCartQuantityUseCase>()
    private val removeFromCartUseCase = mockk<RemoveFromCartUseCase>()
    private val clearCartUseCase = mockk<ClearCartUseCase>()

    private val cartFlow = MutableStateFlow<List<CartItem>>(emptyList())

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeCartUseCase() } returns cartFlow
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun cartItem(productId: Int, quantity: Int, unitPrice: Double = 10.0, discountPercentage: Double = 0.0) =
        CartItem(
            productId = productId,
            title = "Product $productId",
            thumbnailUrl = "thumb",
            unitPrice = unitPrice,
            discountPercentage = discountPercentage,
            quantity = quantity,
        )

    private fun createViewModel() = CartViewModel(
        observeCartUseCase,
        calculateCartTotalsUseCase,
        updateCartQuantityUseCase,
        removeFromCartUseCase,
        clearCartUseCase,
    )

    @Test
    fun `uiState reflects the current cart items and matching computed totals`() = runTest(testDispatcher) {
        val items = listOf(cartItem(productId = 1, quantity = 2, unitPrice = 10.0))
        cartFlow.value = items

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(items, state.items)
        assertEquals(calculateCartTotalsUseCase(items), state.totals)
        assertTrue(!state.isEmpty)
    }

    @Test
    fun `a quantity change updates uiState's items and recalculates totals`() = runTest(testDispatcher) {
        val original = cartItem(productId = 1, quantity = 1, unitPrice = 10.0)
        cartFlow.value = listOf(original)
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { updateCartQuantityUseCase(1, 5) } answers {
            cartFlow.value = listOf(original.copy(quantity = 5))
        }

        viewModel.onQuantityChanged(productId = 1, quantity = 5)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(5, state.items.single().quantity)
        assertEquals(50.0, state.totals.subtotal, 0.0001)
    }

    @Test
    fun `removing an item updates uiState's items and recalculates totals`() = runTest(testDispatcher) {
        val remaining = cartItem(productId = 1, quantity = 1, unitPrice = 10.0)
        val toRemove = cartItem(productId = 2, quantity = 1, unitPrice = 20.0)
        cartFlow.value = listOf(remaining, toRemove)
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { removeFromCartUseCase(2) } answers { cartFlow.value = listOf(remaining) }

        viewModel.onRemoveItem(productId = 2)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf(1), state.items.map { it.productId })
        assertEquals(10.0, state.totals.subtotal, 0.0001)
    }

    @Test
    fun `removing the last item sets isEmpty true with zero totals`() = runTest(testDispatcher) {
        val onlyItem = cartItem(productId = 1, quantity = 1, unitPrice = 10.0)
        cartFlow.value = listOf(onlyItem)
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { removeFromCartUseCase(1) } answers { cartFlow.value = emptyList() }

        viewModel.onRemoveItem(productId = 1)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertTrue(state.items.isEmpty())
        assertEquals(0.0, state.totals.subtotal, 0.0001)
        assertEquals(0.0, state.totals.total, 0.0001)
    }

    @Test
    fun `onQuantityChanged with quantity 0 results in the item no longer appearing in items`() = runTest(testDispatcher) {
        val item = cartItem(productId = 1, quantity = 1, unitPrice = 10.0)
        cartFlow.value = listOf(item)
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { updateCartQuantityUseCase(1, 0) } answers { cartFlow.value = emptyList() }

        viewModel.onQuantityChanged(productId = 1, quantity = 0)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.items.isEmpty())
    }

    @Test
    fun `onOrderClicked clears the cart`() = runTest(testDispatcher) {
        val item = cartItem(productId = 1, quantity = 1, unitPrice = 10.0)
        cartFlow.value = listOf(item)
        val viewModel = createViewModel()
        advanceUntilIdle()

        coEvery { clearCartUseCase() } answers { cartFlow.value = emptyList() }

        viewModel.onOrderClicked()
        advanceUntilIdle()

        coVerify(exactly = 1) { clearCartUseCase() }
        assertTrue(viewModel.uiState.value.isEmpty)
    }
}

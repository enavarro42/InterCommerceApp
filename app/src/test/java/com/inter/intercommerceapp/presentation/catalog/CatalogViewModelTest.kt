package com.inter.intercommerceapp.presentation.catalog

import app.cash.turbine.test
import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.model.ProductError
import com.inter.intercommerceapp.domain.model.ProductsResult
import com.inter.intercommerceapp.domain.usecase.GetProductsUseCase
import com.inter.intercommerceapp.domain.usecase.ObserveCartUseCase
import com.inter.intercommerceapp.domain.usecase.SearchProductsUseCase
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getProductsUseCase = mockk<GetProductsUseCase>()
    private val searchProductsUseCase = mockk<SearchProductsUseCase>()
    private val observeCartUseCase = mockk<ObserveCartUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { observeCartUseCase() } returns flowOf(emptyList())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun product(id: Int) = Product(
        id = id,
        title = "Product $id",
        description = "desc",
        category = "misc",
        price = 10.0,
        discountPercentage = 0.0,
        rating = 4.0,
        stock = 1,
        brand = null,
        thumbnail = "thumb",
        images = emptyList(),
    )

    private fun cartItem(productId: Int, quantity: Int) = CartItem(
        productId = productId,
        title = "Product $productId",
        thumbnailUrl = "thumb",
        unitPrice = 10.0,
        discountPercentage = 0.0,
        quantity = quantity,
    )

    private fun createViewModel() =
        CatalogViewModel(getProductsUseCase, searchProductsUseCase, observeCartUseCase)

    @Test
    fun `initial load emits loading then the first page`() = runTest(testDispatcher) {
        val page = (1..10).map(::product)
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(page, isFromCache = false))

        val viewModel = createViewModel()

        viewModel.uiState.test {
            assertTrue(awaitItem().isLoading)
            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            assertEquals(page, loaded.products)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loadNextPage appends results without duplicating existing items`() = runTest(testDispatcher) {
        val firstPage = (1..10).map(::product)
        val secondPage = (8..17).map(::product)
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(firstPage, isFromCache = false))
        every { getProductsUseCase(limit = 10, skip = 10) } returns
            flowOf(ProductsResult(secondPage, isFromCache = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadNextPage()
        advanceUntilIdle()

        assertEquals((1..17).toList(), viewModel.uiState.value.products.map { it.id })
    }

    @Test
    fun `concurrent loadNextPage calls only trigger a single additional load`() = runTest(testDispatcher) {
        val firstPage = (1..10).map(::product)
        val secondPage = (11..20).map(::product)
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(firstPage, isFromCache = false))
        every { getProductsUseCase(limit = 10, skip = 10) } returns
            flowOf(ProductsResult(secondPage, isFromCache = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.loadNextPage()
        viewModel.loadNextPage()
        advanceUntilIdle()

        verify(exactly = 1) { getProductsUseCase(limit = 10, skip = 10) }
        assertEquals(20, viewModel.uiState.value.products.size)
    }

    @Test
    fun `a page smaller than the limit sets endReached and stops further loads`() = runTest(testDispatcher) {
        val shortPage = (1..5).map(::product)
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(shortPage, isFromCache = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.endReached)

        viewModel.loadNextPage()
        advanceUntilIdle()

        verify(exactly = 0) { getProductsUseCase(limit = 10, skip = 5) }
        assertEquals(5, viewModel.uiState.value.products.size)
    }

    @Test
    fun `changing the search query switches results and clearing restores the paginated list`() = runTest(testDispatcher) {
        val pagedProducts = (1..10).map(::product)
        val searchResults = listOf(product(99))
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(pagedProducts, isFromCache = false))
        every { searchProductsUseCase("phone") } returns
            flowOf(ProductsResult(searchResults, isFromCache = false))

        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onSearchQueryChanged("phone")
        advanceUntilIdle()
        assertEquals(searchResults, viewModel.uiState.value.products)

        viewModel.onSearchQueryChanged("")
        advanceUntilIdle()
        assertEquals(pagedProducts, viewModel.uiState.value.products)
    }

    @Test
    fun `a cached result sets the cached indicator without an error message`() = runTest(testDispatcher) {
        val page = (1..10).map(::product)
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(page, isFromCache = true))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFromCache)
        assertNull(state.errorMessage)
    }

    @Test
    fun `retry re-attempts the first page after a failed initial load`() = runTest(testDispatcher) {
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flow { throw ProductError.NetworkUnavailable() }

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("No network connection available", viewModel.uiState.value.errorMessage)

        val recovered = (1..10).map(::product)
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(recovered, isFromCache = false))

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(recovered, viewModel.uiState.value.products)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `retry re-attempts the active search query after a failed search`() = runTest(testDispatcher) {
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(emptyList(), isFromCache = false))
        val viewModel = createViewModel()
        advanceUntilIdle()

        every { searchProductsUseCase("phone") } returns
            flow { throw ProductError.NetworkUnavailable() }
        viewModel.onSearchQueryChanged("phone")
        advanceUntilIdle()
        assertEquals("No network connection available", viewModel.uiState.value.errorMessage)

        val results = listOf(product(99))
        every { searchProductsUseCase("phone") } returns
            flowOf(ProductsResult(results, isFromCache = false))

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(results, viewModel.uiState.value.products)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun `cartItemCount is zero when the cart is empty`() = runTest(testDispatcher) {
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(emptyList(), isFromCache = false))
        every { observeCartUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(0, viewModel.uiState.value.cartItemCount)
    }

    @Test
    fun `cartItemCount reflects the sum of quantities and updates as the cart changes`() = runTest(testDispatcher) {
        every { getProductsUseCase(limit = 10, skip = 0) } returns
            flowOf(ProductsResult(emptyList(), isFromCache = false))
        val cartFlow = MutableStateFlow<List<CartItem>>(emptyList())
        every { observeCartUseCase() } returns cartFlow

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals(0, viewModel.uiState.value.cartItemCount)

        cartFlow.value = listOf(cartItem(1, quantity = 2), cartItem(2, quantity = 3))
        advanceUntilIdle()

        assertEquals(5, viewModel.uiState.value.cartItemCount)
    }
}

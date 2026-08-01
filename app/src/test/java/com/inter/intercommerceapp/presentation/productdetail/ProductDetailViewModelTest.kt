package com.inter.intercommerceapp.presentation.productdetail

import androidx.lifecycle.SavedStateHandle
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.model.ProductError
import com.inter.intercommerceapp.domain.model.ProductResult
import com.inter.intercommerceapp.domain.usecase.GetProductByIdUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
class ProductDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val getProductByIdUseCase = mockk<GetProductByIdUseCase>()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
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

    private fun createViewModel(productId: Int = 1) = ProductDetailViewModel(
        savedStateHandle = SavedStateHandle(mapOf("productId" to productId)),
        getProductByIdUseCase = getProductByIdUseCase,
    )

    @Test
    fun `a successful load sets product and stops loading`() = runTest(testDispatcher) {
        every { getProductByIdUseCase(1) } returns
            flowOf(Result.success(ProductResult(product(1), isFromCache = false)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.product?.id)
        assertFalse(state.isLoading)
        assertFalse(state.isFromCache)
        assertNull(state.errorMessage)
    }

    @Test
    fun `a failure with nothing cached sets an error message`() = runTest(testDispatcher) {
        every { getProductByIdUseCase(1) } returns
            flow { throw ProductError.NetworkUnavailable() }

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("No network connection available", state.errorMessage)
        assertFalse(state.isLoading)
        assertNull(state.product)
    }

    @Test
    fun `retry re-triggers the load after a failure`() = runTest(testDispatcher) {
        every { getProductByIdUseCase(1) } returns
            flow { throw ProductError.NetworkUnavailable() }

        val viewModel = createViewModel()
        advanceUntilIdle()
        assertEquals("No network connection available", viewModel.uiState.value.errorMessage)

        every { getProductByIdUseCase(1) } returns
            flowOf(Result.success(ProductResult(product(1), isFromCache = false)))

        viewModel.retry()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1, state.product?.id)
        assertNull(state.errorMessage)
    }

    @Test
    fun `a cached result sets the cached indicator without an error message`() = runTest(testDispatcher) {
        every { getProductByIdUseCase(1) } returns
            flowOf(Result.success(ProductResult(product(1), isFromCache = true)))

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isFromCache)
        assertNull(state.errorMessage)
    }
}

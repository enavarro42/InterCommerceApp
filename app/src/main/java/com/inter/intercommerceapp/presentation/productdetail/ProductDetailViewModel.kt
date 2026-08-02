package com.inter.intercommerceapp.presentation.productdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inter.intercommerceapp.domain.model.ProductError
import com.inter.intercommerceapp.domain.model.ProductResult
import com.inter.intercommerceapp.domain.usecase.AddToCartUseCase
import com.inter.intercommerceapp.domain.usecase.GetProductByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getProductByIdUseCase: GetProductByIdUseCase,
    private val addToCartUseCase: AddToCartUseCase,
) : ViewModel() {

    // ProductDetailDestination's serialized fields are populated into the SavedStateHandle as flat
    // keys by Navigation Compose (the same source SavedStateHandle.toRoute<T>() itself reads from),
    // so reading "productId" directly is equivalent to toRoute() in production but avoids
    // toRoute()'s internal android.os.Bundle round-trip, which is unmocked in plain JUnit tests.
    private val productId: Int = checkNotNull(savedStateHandle["productId"])

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    // A Channel (not part of ProductDetailUiState) so "added to cart" fires exactly once per tap
    // and never replays on recomposition or configuration change.
    private val _addedToCartEvent = Channel<Unit>(Channel.BUFFERED)
    val addedToCartEvent: Flow<Unit> = _addedToCartEvent.receiveAsFlow()

    init {
        load()
    }

    fun retry() = load()

    fun onAddToCartClicked() {
        val product = _uiState.value.product ?: return
        viewModelScope.launch {
            addToCartUseCase(product, quantity = 1)
            _addedToCartEvent.send(Unit)
        }
    }

    private fun load() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { getProductByIdUseCase(productId).first() }
                .onSuccess(::onResult)
                .onFailure(::onLoadFailed)
        }
    }

    private fun onResult(result: Result<ProductResult>) {
        result.onSuccess { productResult ->
            _uiState.update {
                it.copy(
                    product = productResult.product,
                    isFromCache = productResult.isFromCache,
                    isLoading = false,
                    errorMessage = null,
                )
            }
        }.onFailure(::onLoadFailed)
    }

    private fun onLoadFailed(error: Throwable) {
        val message = (error as? ProductError)?.message ?: error.message ?: "Unexpected error"
        _uiState.update { it.copy(isLoading = false, errorMessage = message) }
    }
}

package com.inter.intercommerceapp.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.model.ProductError
import com.inter.intercommerceapp.domain.model.ProductsResult
import com.inter.intercommerceapp.domain.usecase.GetProductsUseCase
import com.inter.intercommerceapp.domain.usecase.ObserveCartUseCase
import com.inter.intercommerceapp.domain.usecase.SearchProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val PAGE_SIZE = 10
private const val SEARCH_DEBOUNCE_MILLIS = 300L

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    private val searchProductsUseCase: SearchProductsUseCase,
    private val observeCartUseCase: ObserveCartUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val searchQueryChanges = MutableSharedFlow<String>(extraBufferCapacity = 1)

    private var currentSkip = 0
    private var isLoadInFlight = false

    // Kept separately from uiState.products so clearing an active search restores the
    // paginated list instantly, without re-fetching it.
    private var pagedProducts: List<Product> = emptyList()
    private var pagedIsFromCache = false
    private var pagedEndReached = false

    init {
        viewModelScope.launch {
            searchQueryChanges.debounce(SEARCH_DEBOUNCE_MILLIS).collectLatest(::executeSearch)
        }
        viewModelScope.launch {
            observeCartUseCase().collect { items ->
                val itemCount = items.sumOf { it.quantity }
                _uiState.update { it.copy(cartItemCount = itemCount) }
            }
        }
        loadFirstPage()
    }

    fun loadFirstPage() {
        if (isLoadInFlight) return
        isLoadInFlight = true
        currentSkip = 0
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { getProductsUseCase(limit = PAGE_SIZE, skip = 0).first() }
                .onSuccess(::onFirstPageLoaded)
                .onFailure(::onLoadFailed)
        }
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (isLoadInFlight || state.endReached || state.searchQuery.isNotEmpty()) return
        isLoadInFlight = true
        _uiState.update { it.copy(isLoadingMore = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { getProductsUseCase(limit = PAGE_SIZE, skip = currentSkip).first() }
                .onSuccess(::onNextPageLoaded)
                .onFailure(::onLoadFailed)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        if (query.isEmpty()) {
            restorePagedResults()
        } else {
            searchQueryChanges.tryEmit(query)
        }
    }

    fun retry() {
        val activeQuery = _uiState.value.searchQuery
        if (activeQuery.isNotEmpty()) {
            executeSearch(activeQuery)
        } else {
            loadFirstPage()
        }
    }

    private fun executeSearch(query: String) {
        if (isLoadInFlight) return
        isLoadInFlight = true
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            runCatching { searchProductsUseCase(query).first() }
                .onSuccess(::onSearchLoaded)
                .onFailure(::onLoadFailed)
        }
    }

    private fun restorePagedResults() {
        isLoadInFlight = false
        _uiState.update {
            it.copy(
                products = pagedProducts,
                isLoading = false,
                isLoadingMore = false,
                isFromCache = pagedIsFromCache,
                errorMessage = null,
                endReached = pagedEndReached,
            )
        }
    }

    private fun onFirstPageLoaded(result: ProductsResult) {
        isLoadInFlight = false
        currentSkip = result.products.size
        pagedProducts = result.products
        pagedIsFromCache = result.isFromCache
        pagedEndReached = result.products.size < PAGE_SIZE
        _uiState.update {
            it.copy(
                products = pagedProducts,
                isLoading = false,
                isFromCache = pagedIsFromCache,
                errorMessage = null,
                endReached = pagedEndReached,
            )
        }
    }

    private fun onNextPageLoaded(result: ProductsResult) {
        isLoadInFlight = false
        currentSkip += result.products.size
        val existingIds = pagedProducts.mapTo(mutableSetOf(), Product::id)
        pagedProducts = pagedProducts + result.products.filterNot { it.id in existingIds }
        pagedIsFromCache = result.isFromCache
        pagedEndReached = result.products.size < PAGE_SIZE
        _uiState.update {
            it.copy(
                products = pagedProducts,
                isLoadingMore = false,
                isFromCache = pagedIsFromCache,
                errorMessage = null,
                endReached = pagedEndReached,
            )
        }
    }

    private fun onSearchLoaded(result: ProductsResult) {
        isLoadInFlight = false
        _uiState.update {
            it.copy(
                products = result.products,
                isLoading = false,
                isFromCache = result.isFromCache,
                errorMessage = null,
                endReached = true,
            )
        }
    }

    private fun onLoadFailed(error: Throwable) {
        isLoadInFlight = false
        val message = (error as? ProductError)?.message ?: error.message ?: "Unexpected error"
        _uiState.update {
            it.copy(
                isLoading = false,
                isLoadingMore = false,
                errorMessage = message,
            )
        }
    }
}

package com.inter.intercommerceapp.presentation.catalog

import com.inter.intercommerceapp.domain.model.Product

data class CatalogUiState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val isFromCache: Boolean = false,
    val errorMessage: String? = null,
    val searchQuery: String = "",
    val endReached: Boolean = false,
    val cartItemCount: Int = 0,
)

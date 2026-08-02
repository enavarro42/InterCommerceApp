package com.inter.intercommerceapp.presentation.productdetail

import com.inter.intercommerceapp.domain.model.Product

data class ProductDetailUiState(
    val product: Product? = null,
    val isLoading: Boolean = false,
    val isFromCache: Boolean = false,
    val errorMessage: String? = null,
    val cartItemCount: Int = 0,
)

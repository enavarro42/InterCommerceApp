package com.inter.intercommerceapp.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inter.intercommerceapp.domain.usecase.CalculateCartTotalsUseCase
import com.inter.intercommerceapp.domain.usecase.ClearCartUseCase
import com.inter.intercommerceapp.domain.usecase.ObserveCartUseCase
import com.inter.intercommerceapp.domain.usecase.RemoveFromCartUseCase
import com.inter.intercommerceapp.domain.usecase.UpdateCartQuantityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class CartViewModel @Inject constructor(
    observeCartUseCase: ObserveCartUseCase,
    private val calculateCartTotalsUseCase: CalculateCartTotalsUseCase,
    private val updateCartQuantityUseCase: UpdateCartQuantityUseCase,
    private val removeFromCartUseCase: RemoveFromCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
) : ViewModel() {

    val uiState: StateFlow<CartUiState> = observeCartUseCase()
        .map { items ->
            CartUiState(
                items = items,
                totals = calculateCartTotalsUseCase(items),
                isEmpty = items.isEmpty(),
            )
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, CartUiState())

    fun onQuantityChanged(productId: Int, quantity: Int) {
        viewModelScope.launch { updateCartQuantityUseCase(productId, quantity) }
    }

    fun onRemoveItem(productId: Int) {
        viewModelScope.launch { removeFromCartUseCase(productId) }
    }

    fun onOrderClicked() {
        viewModelScope.launch { clearCartUseCase() }
    }
}

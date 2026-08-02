package com.inter.intercommerceapp.presentation.cart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.inter.intercommerceapp.R
import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.CartTotals

const val CART_LIST_TEST_TAG = "cart_list"
const val CART_EMPTY_STATE_TEST_TAG = "cart_empty_state"
const val CART_TOTALS_TEST_TAG = "cart_totals"

fun cartItemRowTestTag(productId: Int) = "cart_item_$productId"
fun cartItemQuantityTestTag(productId: Int) = "cart_item_${productId}_quantity"
fun cartItemIncrementTestTag(productId: Int) = "cart_item_${productId}_increment"
fun cartItemDecrementTestTag(productId: Int) = "cart_item_${productId}_decrement"
fun cartItemRemoveTestTag(productId: Int) = "cart_item_${productId}_remove"

@Composable
fun CartRoute(
    modifier: Modifier = Modifier,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CartScreen(
        uiState = uiState,
        onQuantityChanged = viewModel::onQuantityChanged,
        onRemoveItem = viewModel::onRemoveItem,
        modifier = modifier,
    )
}

@Composable
fun CartScreen(
    uiState: CartUiState,
    onQuantityChanged: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(modifier = modifier) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (uiState.isEmpty) {
                CartEmptyState(modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag(CART_LIST_TEST_TAG),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.items, key = CartItem::productId) { item ->
                        CartItemRow(
                            item = item,
                            onQuantityChanged = { quantity -> onQuantityChanged(item.productId, quantity) },
                            onRemove = { onRemoveItem(item.productId) },
                        )
                    }
                }
                CartTotalsSummary(totals = uiState.totals)
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChanged: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(cartItemRowTestTag(item.productId)),
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .size(56.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = item.title, maxLines = 1, style = MaterialTheme.typography.titleSmall)
                Text(
                    text = stringResource(R.string.product_price_format, item.unitPrice),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { onQuantityChanged(item.quantity - 1) },
                    modifier = Modifier.testTag(cartItemDecrementTestTag(item.productId)),
                ) {
                    Text("−")
                }
                Text(
                    text = item.quantity.toString(),
                    modifier = Modifier.testTag(cartItemQuantityTestTag(item.productId)),
                )
                TextButton(
                    onClick = { onQuantityChanged(item.quantity + 1) },
                    modifier = Modifier.testTag(cartItemIncrementTestTag(item.productId)),
                ) {
                    Text("+")
                }
            }
            TextButton(
                onClick = onRemove,
                modifier = Modifier.testTag(cartItemRemoveTestTag(item.productId)),
            ) {
                Text(stringResource(R.string.cart_action_remove))
            }
        }
    }
}

@Composable
fun CartTotalsSummary(totals: CartTotals, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CART_TOTALS_TEST_TAG)
            .padding(16.dp),
    ) {
        Text(stringResource(R.string.cart_subtotal_format, totals.subtotal))
        Text(stringResource(R.string.cart_discount_format, totals.discountAmount))
        Text(stringResource(R.string.cart_tax_format, totals.taxAmount))
        Text(
            text = stringResource(R.string.cart_total_format, totals.total),
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
fun CartEmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.testTag(CART_EMPTY_STATE_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = stringResource(R.string.cart_empty_message))
    }
}

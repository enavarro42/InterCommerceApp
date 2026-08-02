package com.inter.intercommerceapp.presentation.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.inter.intercommerceapp.R
import com.inter.intercommerceapp.domain.model.CartItem
import com.inter.intercommerceapp.domain.model.CartTotals
import com.inter.intercommerceapp.presentation.components.BackIconButton

const val CART_LIST_TEST_TAG = "cart_list"
const val CART_EMPTY_STATE_TEST_TAG = "cart_empty_state"
const val CART_TOTALS_TEST_TAG = "cart_totals"
const val CART_ORDER_BUTTON_TEST_TAG = "cart_order_button"

fun cartItemRowTestTag(productId: Int) = "cart_item_$productId"
fun cartItemQuantityTestTag(productId: Int) = "cart_item_${productId}_quantity"
fun cartItemIncrementTestTag(productId: Int) = "cart_item_${productId}_increment"
fun cartItemDecrementTestTag(productId: Int) = "cart_item_${productId}_decrement"
fun cartItemRemoveTestTag(productId: Int) = "cart_item_${productId}_remove"

@Composable
fun CartRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CartViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CartScreen(
        uiState = uiState,
        onQuantityChanged = viewModel::onQuantityChanged,
        onRemoveItem = viewModel::onRemoveItem,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
fun CartScreen(
    uiState: CartUiState,
    onQuantityChanged: (Int, Int) -> Unit,
    onRemoveItem: (Int) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CartHeader(
                totalItemCount = uiState.items.sumOf { it.quantity },
                onBack = onBack,
            )
        },
    ) { innerPadding ->
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
                Button(
                    onClick = {},
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag(CART_ORDER_BUTTON_TEST_TAG),
                ) {
                    Text(stringResource(R.string.cart_order_action))
                }
            }
        }
    }
}

@Composable
private fun CartHeader(
    totalItemCount: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackIconButton(onClick = onBack)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.cart_screen_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.cart_total_items_format, totalItemCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartItemRow(
    item: CartItem,
    onQuantityChanged: (Int) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRemove()
            }
            true
        },
    )

    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier
            .fillMaxWidth()
            .testTag(cartItemRowTestTag(item.productId)),
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            CartItemDeleteBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(cartItemRemoveTestTag(item.productId)),
            )
        },
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
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
                QuantityStepper(
                    quantity = item.quantity,
                    onQuantityChanged = onQuantityChanged,
                    productId = item.productId,
                )
            }
        }
    }
}

@Composable
private fun CartItemDeleteBackground(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.error)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterEnd,
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = stringResource(R.string.cart_action_remove),
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    onQuantityChanged: (Int) -> Unit,
    productId: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        QuantityStepButton(
            icon = Icons.Default.Remove,
            onClick = { onQuantityChanged(quantity - 1) },
            modifier = Modifier.testTag(cartItemDecrementTestTag(productId)),
        )
        Text(
            text = quantity.toString(),
            modifier = Modifier
                .width(28.dp)
                .testTag(cartItemQuantityTestTag(productId)),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
        )
        QuantityStepButton(
            icon = Icons.Default.Add,
            onClick = { onQuantityChanged(quantity + 1) },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.testTag(cartItemIncrementTestTag(productId)),
        )
    }
}

@Composable
private fun QuantityStepButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.Transparent,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
fun CartTotalsSummary(totals: CartTotals, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CART_TOTALS_TEST_TAG)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(R.string.cart_total_label),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = stringResource(R.string.cart_total_amount_format, totals.total),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
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

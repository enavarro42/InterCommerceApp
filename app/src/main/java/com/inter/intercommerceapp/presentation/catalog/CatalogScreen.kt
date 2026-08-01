package com.inter.intercommerceapp.presentation.catalog

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.inter.intercommerceapp.domain.model.Product
import java.io.File
import kotlinx.coroutines.flow.map

private const val LOAD_MORE_THRESHOLD = 3

const val CATALOG_GRID_TEST_TAG = "catalog_grid"
const val CATALOG_OFFLINE_BANNER_TEST_TAG = "catalog_offline_banner"
const val CATALOG_ERROR_TEST_TAG = "catalog_error"
const val CATALOG_RETRY_BUTTON_TEST_TAG = "catalog_retry_button"
const val CATALOG_SEARCH_FIELD_TEST_TAG = "catalog_search_field"
const val CATALOG_OFFLINE_RETRY_BUTTON_TEST_TAG = "catalog_offline_retry_button"

fun productGridItemTestTag(productId: Int) = "product_item_$productId"

@Composable
fun CatalogRoute(
    modifier: Modifier = Modifier,
    viewModel: CatalogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    CatalogScreen(
        uiState = uiState,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onLoadNextPage = viewModel::loadNextPage,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

@Composable
fun CatalogScreen(
    uiState: CatalogUiState,
    onSearchQueryChanged: (String) -> Unit,
    onLoadNextPage: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onProductClick: (Product) -> Unit = {},
) {
    val gridState = rememberLazyGridState()
    val latestUiState by rememberUpdatedState(uiState)
    val latestOnLoadNextPage by rememberUpdatedState(onLoadNextPage)

    LaunchedEffect(gridState) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .map { lastVisibleIndex -> lastVisibleIndex ?: -1 }
            .collect { lastVisibleIndex ->
                val state = latestUiState
                val nearEnd = lastVisibleIndex >= state.products.size - 1 - LOAD_MORE_THRESHOLD
                val canLoadMore = !state.isLoadingMore && !state.endReached && state.searchQuery.isEmpty()
                if (nearEnd && canLoadMore && state.products.isNotEmpty()) {
                    latestOnLoadNextPage()
                }
            }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
        topBar = {
            CatalogSearchBar(query = uiState.searchQuery, onQueryChanged = onSearchQueryChanged)
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (uiState.isFromCache) {
                OfflineBanner(onRetry = onRetry)
            }
            when {
                uiState.isLoading && uiState.products.isEmpty() -> {
                    CatalogShimmerPlaceholder(modifier = Modifier.fillMaxSize())
                }
                uiState.errorMessage != null && uiState.products.isEmpty() -> {
                    CatalogErrorState(
                        message = uiState.errorMessage,
                        onRetry = onRetry,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
                else -> {
                    PullToRefreshBox(
                        isRefreshing = uiState.isLoading,
                        onRefresh = onRetry,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        ProductGrid(
                            products = uiState.products,
                            isLoadingMore = uiState.isLoadingMore,
                            gridState = gridState,
                            onProductClick = onProductClick,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogSearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 2.dp) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(12.dp)
                .testTag(CATALOG_SEARCH_FIELD_TEST_TAG),
            placeholder = { Text("Buscar productos") },
            singleLine = true,
        )
    }
}

@Composable
private fun OfflineBanner(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(CATALOG_OFFLINE_BANNER_TEST_TAG),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sin conexión, mostrando datos guardados",
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onRetry,
                modifier = Modifier.testTag(CATALOG_OFFLINE_RETRY_BUTTON_TEST_TAG),
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun CatalogErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.testTag(CATALOG_ERROR_TEST_TAG),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Text(text = message, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                modifier = Modifier.testTag(CATALOG_RETRY_BUTTON_TEST_TAG),
            ) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun ProductGrid(
    products: List<Product>,
    isLoadingMore: Boolean,
    gridState: LazyGridState,
    onProductClick: (Product) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = gridState,
        modifier = modifier.testTag(CATALOG_GRID_TEST_TAG),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(products, key = { it.id }) { product ->
            ProductGridItem(product = product, onClick = { onProductClick(product) })
        }
        if (isLoadingMore) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(28.dp))
                }
            }
        }
    }
}

@Composable
private fun ProductGridItem(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(productGridItemTestTag(product.id)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = product.localThumbnailPath?.let(::File) ?: product.thumbnail,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp)),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = product.title, maxLines = 1)
            Text(
                text = "$${product.price}",
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

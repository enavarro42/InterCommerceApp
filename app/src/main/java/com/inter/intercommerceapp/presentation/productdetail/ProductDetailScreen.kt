package com.inter.intercommerceapp.presentation.productdetail

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.inter.intercommerceapp.R
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.presentation.components.BackIconButton
import com.inter.intercommerceapp.presentation.components.CartIconButton
import com.inter.intercommerceapp.ui.theme.BrandRatingStar
import com.inter.intercommerceapp.ui.theme.BrandSuccess
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

const val PRODUCT_DETAIL_SHIMMER_TEST_TAG = "product_detail_shimmer"
const val PRODUCT_DETAIL_ERROR_TEST_TAG = "product_detail_error"
const val PRODUCT_DETAIL_RETRY_BUTTON_TEST_TAG = "product_detail_retry_button"
const val PRODUCT_DETAIL_OFFLINE_BANNER_TEST_TAG = "product_detail_offline_banner"
const val PRODUCT_DETAIL_OFFLINE_RETRY_BUTTON_TEST_TAG = "product_detail_offline_retry_button"
const val PRODUCT_DETAIL_CONTENT_TEST_TAG = "product_detail_content"
const val PRODUCT_DETAIL_ADD_TO_CART_BUTTON_TEST_TAG = "product_detail_add_to_cart_button"

private const val ADD_TO_CART_VIBRATION_DURATION_MS = 50L

@Composable
fun ProductDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProductDetailViewModel = hiltViewModel(),
    onCartClick: () -> Unit = {},
) {
    val uiState by viewModel.uiState.collectAsState()
    ProductDetailScreen(
        uiState = uiState,
        onRetry = viewModel::retry,
        onBack = onBack,
        onAddToCartClicked = viewModel::onAddToCartClicked,
        addedToCartEvent = viewModel.addedToCartEvent,
        onCartClick = onCartClick,
        modifier = modifier,
    )
}

@Composable
fun ProductDetailScreen(
    uiState: ProductDetailUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    onAddToCartClicked: () -> Unit = {},
    addedToCartEvent: Flow<Unit> = emptyFlow(),
    onCartClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val addedToCartMessage = stringResource(R.string.product_detail_added_to_cart_message)

    LaunchedEffect(addedToCartEvent) {
        addedToCartEvent.collect {
            vibrateOnAddToCart(context)
            snackbarHostState.showSnackbar(addedToCartMessage)
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            ProductDetailHeader(
                cartItemCount = uiState.cartItemCount,
                onBack = onBack,
                onCartClick = onCartClick,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            if (uiState.isFromCache) {
                ProductDetailOfflineBanner(onRetry = onRetry)
            }
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.product != null -> {
                        ProductDetailContent(product = uiState.product, modifier = Modifier.fillMaxSize())
                    }
                    uiState.errorMessage != null -> {
                        ProductDetailErrorState(
                            message = uiState.errorMessage,
                            onRetry = onRetry,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    else -> {
                        ProductDetailShimmerPlaceholder(modifier = Modifier.fillMaxSize())
                    }
                }
            }
            Button(
                onClick = onAddToCartClicked,
                enabled = uiState.product != null,
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag(PRODUCT_DETAIL_ADD_TO_CART_BUTTON_TEST_TAG),
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.product_detail_add_to_cart))
            }
        }
    }
}

@Composable
private fun ProductDetailHeader(
    cartItemCount: Int,
    onBack: () -> Unit,
    onCartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BackIconButton(onClick = onBack)
        CartIconButton(cartItemCount = cartItemCount, onClick = onCartClick)
    }
}

private fun vibrateOnAddToCart(context: Context) {
    val effect = VibrationEffect.createOneShot(
        ADD_TO_CART_VIBRATION_DURATION_MS,
        VibrationEffect.DEFAULT_AMPLITUDE,
    )
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator.vibrate(effect)
        }
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O -> {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            vibrator.vibrate(effect)
        }
        else -> {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            @Suppress("DEPRECATION")
            vibrator.vibrate(ADD_TO_CART_VIBRATION_DURATION_MS)
        }
    }
}

@Composable
private fun ProductDetailOfflineBanner(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag(PRODUCT_DETAIL_OFFLINE_BANNER_TEST_TAG),
        color = MaterialTheme.colorScheme.secondaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.catalog_offline_banner_message),
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                modifier = Modifier.weight(1f),
            )
            TextButton(
                onClick = onRetry,
                modifier = Modifier.testTag(PRODUCT_DETAIL_OFFLINE_RETRY_BUTTON_TEST_TAG),
            ) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
private fun ProductDetailErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.testTag(PRODUCT_DETAIL_ERROR_TEST_TAG),
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
                modifier = Modifier.testTag(PRODUCT_DETAIL_RETRY_BUTTON_TEST_TAG),
            ) {
                Text(stringResource(R.string.action_retry))
            }
        }
    }
}

@Composable
private fun ProductDetailContent(product: Product, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .testTag(PRODUCT_DETAIL_CONTENT_TEST_TAG),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            AsyncImage(
                model = product.localThumbnailPath?.let(::File) ?: product.thumbnail,
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .aspectRatio(1f),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(MaterialTheme.colorScheme.background)
                .padding(20.dp),
        ) {
            Text(
                text = product.title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.product_price_format, product.price),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            ProductAvailabilityRow(rating = product.rating, stock = product.stock)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                product.brand?.let { brand ->
                    ProductInfoChip(
                        label = stringResource(R.string.product_detail_brand_label),
                        value = brand,
                        modifier = Modifier.weight(1f),
                    )
                }
                ProductInfoChip(
                    label = stringResource(R.string.product_detail_category_label),
                    value = product.category,
                    modifier = Modifier.weight(1f),
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = stringResource(R.string.product_detail_description_label),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = product.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ProductAvailabilityRow(rating: Double, stock: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = BrandRatingStar,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = rating.toString(), style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "•",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(8.dp))
        if (stock > 0) {
            Text(
                text = stringResource(R.string.product_detail_stock_available_format, stock),
                style = MaterialTheme.typography.bodyMedium,
                color = BrandSuccess,
            )
        } else {
            Text(
                text = stringResource(R.string.product_detail_out_of_stock),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
private fun ProductInfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(12.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ProductDetailShimmerPlaceholder(modifier: Modifier = Modifier) {
    val shimmerColor = shimmerColor()
    Column(
        modifier = modifier
            .padding(16.dp)
            .testTag(PRODUCT_DETAIL_SHIMMER_TEST_TAG),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(12.dp))
                .background(shimmerColor),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor),
        )
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .height(20.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(shimmerColor),
        )
    }
}

@Composable
private fun shimmerColor(): Color {
    val base = MaterialTheme.colorScheme.surfaceVariant
    val highlight = MaterialTheme.colorScheme.surface
    val transition = rememberInfiniteTransition(label = "productDetailShimmer")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "productDetailShimmerProgress",
    )
    return lerp(base, highlight, progress)
}

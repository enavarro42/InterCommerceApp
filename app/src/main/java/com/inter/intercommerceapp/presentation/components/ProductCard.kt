package com.inter.intercommerceapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.inter.intercommerceapp.R
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.ui.theme.InterCommerceAppTheme
import java.io.File

const val PRODUCT_CARD_IMAGE_TEST_TAG = "product_card_image"

fun productCardTestTag(productId: Int) = "product_card_$productId"

@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .testTag(productCardTestTag(product.id)),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            AsyncImage(
                model = product.localThumbnailPath?.let(::File) ?: product.thumbnail,
                contentDescription = product.title,
                placeholder = ColorPainter(MaterialTheme.colorScheme.surfaceVariant),
                error = ColorPainter(MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .testTag(PRODUCT_CARD_IMAGE_TEST_TAG),
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = product.title, maxLines = 1)
            Text(
                text = stringResource(R.string.product_price_format, product.price),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

private fun previewProduct(thumbnail: String) = Product(
    id = 1,
    title = "Product 1",
    description = "desc",
    category = "misc",
    price = 9.99,
    discountPercentage = 0.0,
    rating = 4.0,
    stock = 1,
    brand = null,
    thumbnail = thumbnail,
    images = emptyList(),
)

@Preview(name = "Loaded / Loading", showBackground = true)
@Composable
private fun ProductCardPreview() {
    InterCommerceAppTheme {
        ProductCard(
            product = previewProduct("https://cdn.dummyjson.com/product-images/1/thumbnail.webp"),
            onClick = {},
        )
    }
}

@Preview(name = "Error", showBackground = true)
@Composable
private fun ProductCardErrorPreview() {
    InterCommerceAppTheme {
        ProductCard(
            product = previewProduct(""),
            onClick = {},
        )
    }
}

package com.inter.intercommerceapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import com.inter.intercommerceapp.R

const val CART_ICON_BUTTON_TEST_TAG = "cart_icon_button"
const val CART_ICON_BADGE_TEST_TAG = "cart_icon_badge"

@Composable
fun CartIconButton(
    cartItemCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BadgedBox(
        modifier = modifier,
        badge = {
            if (cartItemCount > 0) {
                Badge(modifier = Modifier.testTag(CART_ICON_BADGE_TEST_TAG)) {
                    Text(text = cartItemCount.toString())
                }
            }
        },
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .testTag(CART_ICON_BUTTON_TEST_TAG),
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = stringResource(R.string.cart_icon_content_description),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }
}

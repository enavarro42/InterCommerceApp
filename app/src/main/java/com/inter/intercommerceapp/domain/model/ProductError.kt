package com.inter.intercommerceapp.domain.model

sealed class ProductError(message: String, cause: Throwable? = null) : Exception(message, cause) {

    data class NetworkUnavailable(val originalCause: Throwable? = null) :
        ProductError("No network connection available", originalCause)

    data class NotFound(val id: Int? = null) :
        ProductError(if (id != null) "Product $id not found" else "Product not found")

    data class ServerError(val originalCause: Throwable? = null) :
        ProductError("The server returned an unexpected error", originalCause)
}

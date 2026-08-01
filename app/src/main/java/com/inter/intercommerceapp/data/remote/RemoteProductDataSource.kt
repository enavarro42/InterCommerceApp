package com.inter.intercommerceapp.data.remote

import com.inter.intercommerceapp.data.mapper.ProductMapper
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.model.ProductError
import java.io.IOException
import javax.inject.Inject
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

class RemoteProductDataSource @Inject constructor(
    private val api: ProductApi,
) {

    suspend fun getProducts(limit: Int, skip: Int): List<Product> = safeCall {
        api.getProducts(limit, skip).products.map(ProductMapper::toDomain)
    }

    suspend fun searchProducts(query: String): List<Product> = safeCall {
        api.searchProducts(query).products.map(ProductMapper::toDomain)
    }

    suspend fun getProductById(id: Int): Product = safeCall(id) {
        ProductMapper.toDomain(api.getProductById(id))
    }

    private suspend fun <T> safeCall(id: Int? = null, block: suspend () -> T): T {
        try {
            return block()
        } catch (e: IOException) {
            throw ProductError.NetworkUnavailable(e)
        } catch (e: HttpException) {
            throw if (e.code() == 404) ProductError.NotFound(id) else ProductError.ServerError(e)
        } catch (e: SerializationException) {
            throw ProductError.ServerError(e)
        }
    }
}

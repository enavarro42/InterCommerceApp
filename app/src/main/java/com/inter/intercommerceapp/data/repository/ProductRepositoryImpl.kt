package com.inter.intercommerceapp.data.repository

import com.inter.intercommerceapp.data.local.LocalProductDataSource
import com.inter.intercommerceapp.data.remote.RemoteProductDataSource
import com.inter.intercommerceapp.domain.model.Product
import com.inter.intercommerceapp.domain.model.ProductError
import com.inter.intercommerceapp.domain.model.ProductsResult
import com.inter.intercommerceapp.domain.repository.ProductRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

class ProductRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteProductDataSource,
    private val localDataSource: LocalProductDataSource,
) : ProductRepository {

    // La función de busqueda no dispone de un grupo de cache propio y persistente los resultados de la última consulta realizada con exito
    // se conservan únicamente en memoria y solo se reutilizan como alternativa para esa misma consulta concreta.
    private var lastSearchQuery: String? = null
    private var lastSearchResults: List<Product> = emptyList()

    override fun getProducts(limit: Int, skip: Int): Flow<ProductsResult> = flow {
        var refreshSucceeded = false
        try {
            val remoteProducts = remoteDataSource.getProducts(limit, skip)
            localDataSource.replaceCachedProducts(remoteProducts)
            refreshSucceeded = true
        } catch (e: ProductError) {
            if (localDataSource.getCachedProducts().first().isEmpty()) throw e
        }
        emitAll(
            localDataSource.getCachedProducts()
                .map { cached -> ProductsResult(products = cached, isFromCache = !refreshSucceeded) }
        )
    }

    override fun searchProducts(query: String): Flow<ProductsResult> = flow {
        try {
            val remoteResults = remoteDataSource.searchProducts(query)
            lastSearchQuery = query
            lastSearchResults = remoteResults
            emit(ProductsResult(products = remoteResults, isFromCache = false))
        } catch (e: ProductError) {
            if (lastSearchQuery != query) throw e
            emit(ProductsResult(products = lastSearchResults, isFromCache = true))
        }
    }

    override fun getProductById(id: Int): Flow<Result<Product>> = flow {
        try {
            val remoteProduct = remoteDataSource.getProductById(id)
            localDataSource.cacheProduct(remoteProduct)
        } catch (e: ProductError) {
            if (localDataSource.getCachedProductById(id).first() == null) throw e
        }
        emitAll(
            localDataSource.getCachedProductById(id)
                .map { cached ->
                    cached?.let { Result.success(it) } ?: Result.failure(ProductError.NotFound(id))
                }
        )
    }
}

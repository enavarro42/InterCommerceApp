package com.inter.intercommerceapp.data.local

import com.inter.intercommerceapp.data.local.db.ProductDao
import com.inter.intercommerceapp.data.mapper.ProductMapper
import com.inter.intercommerceapp.domain.model.Product
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LocalProductDataSource @Inject constructor(
    private val dao: ProductDao,
) {

    fun getCachedProducts(): Flow<List<Product>> =
        dao.getAll().map { entities -> entities.map(ProductMapper::toDomain) }

    fun getCachedProductById(id: Int): Flow<Product?> =
        dao.getById(id).map { entity -> entity?.let(ProductMapper::toDomain) }

    suspend fun replaceCachedProducts(products: List<Product>) {
        dao.replaceAll(products.map { ProductMapper.toEntity(it) })
    }

    suspend fun clearCache() {
        dao.clear()
    }
}

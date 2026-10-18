package com.closetfinder.app.data

import com.closetfinder.app.util.CodeGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ItemRepository(private val dao: ItemDao) {

    fun observeAll(): Flow<List<Item>> = dao.observeAll()

    fun observeStores(): Flow<List<String>> = dao.observeStores()

    fun observeTags(): Flow<List<String>> = dao.observeTagsColumn().map { columns ->
        columns.asSequence()
            .flatMap { it.split(',') }
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .sorted()
            .toList()
    }

    suspend fun getById(id: Long): Item? = dao.getById(id)

    suspend fun addItem(
        imagePath: String,
        store: String,
        price: Int,
        salePrice: Int?,
        tags: String,
        notes: String?,
    ): Long {
        val previousCode = dao.getMostRecent()?.code
        val item = Item(
            code = CodeGenerator.next(previousCode),
            imagePath = imagePath,
            store = store,
            price = price,
            salePrice = salePrice,
            tags = tags,
            notes = notes,
        )
        return dao.insert(item)
    }

    suspend fun update(item: Item) = dao.update(item)

    suspend fun delete(item: Item) = dao.delete(item)
}

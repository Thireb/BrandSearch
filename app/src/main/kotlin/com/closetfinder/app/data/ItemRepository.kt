package com.closetfinder.app.data

import com.closetfinder.app.util.CodeGenerator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val CODE_PATTERN = Regex("^[A-Za-z]+\\d+$")

class ItemRepository(
    private val dao: ItemDao,
    private val ftsDao: ItemFtsDao,
) {

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

    fun observeFiltered(store: String?, minPrice: Int?, maxPrice: Int?, since: Long?): Flow<List<Item>> =
        dao.observeFiltered(store, minPrice, maxPrice, since)

    suspend fun getById(id: Long): Item? = dao.getById(id)

    /** Direct code lookup (e.g. "A023") or, failing that, an FTS prefix search over code/store/tags/notes. */
    suspend fun search(query: String): List<Item> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        if (CODE_PATTERN.matches(trimmed)) {
            dao.getByCode(trimmed)?.let { return listOf(it) }
        }

        val ftsQuery = trimmed.split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { token -> token.replace("\"", "") + "*" }
        if (ftsQuery.isBlank()) return emptyList()

        val ids = ftsDao.search(ftsQuery)
        if (ids.isEmpty()) return emptyList()
        return dao.getByIds(ids)
    }

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
        val id = dao.insert(item)
        indexForSearch(item.copy(id = id))
        return id
    }

    suspend fun update(item: Item) {
        dao.update(item)
        ftsDao.deleteByItemId(item.id)
        indexForSearch(item)
    }

    suspend fun delete(item: Item) {
        dao.delete(item)
        ftsDao.deleteByItemId(item.id)
    }

    private suspend fun indexForSearch(item: Item) {
        ftsDao.insert(
            ItemFtsEntry(
                itemId = item.id,
                code = item.code,
                store = item.store,
                tags = item.tags,
                notes = item.notes.orEmpty(),
            ),
        )
    }
}

package com.closetfinder.app.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.closetfinder.app.embed.Embedder
import com.closetfinder.app.util.CodeGenerator
import com.closetfinder.app.util.CosineSimilarity
import com.closetfinder.app.util.EmbeddingCodec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val CODE_PATTERN = Regex("^[A-Za-z]+\\d+$")

class ItemRepository(
    private val dao: ItemDao,
    private val ftsDao: ItemFtsDao,
    private val embedder: Embedder,
    private val externalScope: CoroutineScope,
) {

    fun observeAll(): Flow<List<Item>> = dao.observeAll()

    fun observeStores(): Flow<List<String>> = dao.observeStores()

    suspend fun getLastUsedStore(): String? = dao.getMostRecent()?.store

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
        embedInBackground(id, imagePath)
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

    suspend fun embedImage(bitmap: Bitmap): FloatArray = embedder.embed(bitmap)

    suspend fun searchBySimilarity(queryEmbedding: FloatArray, topN: Int = 5): List<Pair<Item, Float>> =
        dao.getAllWithEmbeddings()
            .mapNotNull { item -> item.embedding?.let { item to CosineSimilarity.of(queryEmbedding, EmbeddingCodec.decode(it)) } }
            .sortedByDescending { it.second }
            .take(topN)

    /** Embeds anything missing an embedding, or embedded by an older model version. Runs in the background. */
    fun reembedStaleItemsAsync() {
        externalScope.launch {
            dao.getItemsNeedingEmbedding(embedder.modelVersion).forEach { item ->
                embedInBackground(item.id, item.imagePath)
            }
        }
    }

    /** Fire-and-forget: keeps add-item fast (<10s) by not blocking the save on embedding compute. */
    private fun embedInBackground(itemId: Long, imagePath: String) {
        externalScope.launch {
            val bitmap = BitmapFactory.decodeFile(imagePath) ?: return@launch
            val vector = embedder.embed(bitmap)
            bitmap.recycle()
            val current = dao.getById(itemId) ?: return@launch
            dao.update(current.copy(embedding = EmbeddingCodec.encode(vector), modelVersion = embedder.modelVersion))
        }
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

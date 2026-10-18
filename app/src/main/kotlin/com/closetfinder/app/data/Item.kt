package com.closetfinder.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "items")
data class Item(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val imagePath: String,
    val store: String,
    val price: Int,
    val salePrice: Int? = null,
    val tags: String = "",
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val embedding: ByteArray? = null,
    val modelVersion: Int = 0,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Item) return false
        return id == other.id &&
            code == other.code &&
            imagePath == other.imagePath &&
            store == other.store &&
            price == other.price &&
            salePrice == other.salePrice &&
            tags == other.tags &&
            notes == other.notes &&
            createdAt == other.createdAt &&
            modelVersion == other.modelVersion
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + imagePath.hashCode()
        return result
    }
}

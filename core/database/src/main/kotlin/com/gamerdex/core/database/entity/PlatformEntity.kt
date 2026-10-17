package com.gamerdex.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "platforms", indices = [Index(value = ["name"], unique = true)])
data class PlatformEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val generation: Int? = null,
    val logoUrl: String? = null,
    val colorHex: String,
) {
    init {
        require(name.isNotBlank())
        require(generation == null || generation > 0)
        require(Regex("#[0-9a-fA-F]{6}").matches(colorHex))
    }
}

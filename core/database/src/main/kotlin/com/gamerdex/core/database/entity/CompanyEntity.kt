package com.gamerdex.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "companies", indices = [Index(value = ["name"], unique = true)])
data class CompanyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val logoUrl: String? = null,
) { init { require(name.isNotBlank()) } }

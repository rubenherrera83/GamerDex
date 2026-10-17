package com.gamerdex.core.database

import androidx.room.TypeConverter
import com.gamerdex.core.domain.GameCondition
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class DatabaseConverters {
    @TypeConverter fun editionToString(value: com.gamerdex.core.domain.EditionType): String = value.name
    @TypeConverter fun stringToEdition(value: String) = com.gamerdex.core.domain.EditionType.valueOf(value)
    @TypeConverter fun physicalToString(value: com.gamerdex.core.domain.PhysicalCondition): String = value.name
    @TypeConverter fun stringToPhysical(value: String) = com.gamerdex.core.domain.PhysicalCondition.valueOf(value)
    @TypeConverter fun photosToJson(value: List<String>): String = Json.encodeToString(value)
    @TypeConverter fun jsonToPhotos(value: String): List<String> = Json.decodeFromString(value)
    @TypeConverter fun conditionToString(value: GameCondition): String = value.name
    @TypeConverter fun stringToCondition(value: String): GameCondition = GameCondition.valueOf(value)
}

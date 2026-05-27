package com.ponslink.capturebrain.data

import androidx.room.TypeConverter

class CaptureBrainConverters {
    @TypeConverter
    fun statusToString(status: ProcessingStatus): String = status.name

    @TypeConverter
    fun stringToStatus(raw: String): ProcessingStatus = ProcessingStatus.valueOf(raw)

    @TypeConverter
    fun stringListToStorage(values: List<String>): String = values.joinToString(separator = "")

    @TypeConverter
    fun storageToStringList(raw: String): List<String> =
        if (raw.isBlank()) emptyList() else raw.split("")
}

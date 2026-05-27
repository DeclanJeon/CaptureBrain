package com.ponslink.capturebrain.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [CaptureItemEntity::class], version = 1, exportSchema = true)
@TypeConverters(CaptureBrainConverters::class)
abstract class CaptureBrainDatabase : RoomDatabase() {
    abstract fun captureItemDao(): CaptureItemDao

    companion object {
        @Volatile private var instance: CaptureBrainDatabase? = null

        fun get(context: Context): CaptureBrainDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                CaptureBrainDatabase::class.java,
                "capturebrain.db"
            ).build().also { instance = it }
        }
    }
}

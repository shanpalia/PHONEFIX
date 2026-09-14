package com.paliaapk.phonefix.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanReportEntity::class], version = 1, exportSchema = false)
abstract class PhoneFixDatabase : RoomDatabase() {
    abstract fun scanReportDao(): ScanReportDao

    companion object {
        @Volatile
        private var INSTANCE: PhoneFixDatabase? = null

        fun getInstance(context: Context): PhoneFixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PhoneFixDatabase::class.java,
                    "phonefix_diagnostics.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}

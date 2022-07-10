package org.hans.standardcalculator.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [HistoryMemo::class], version = 1)
abstract class HistoryDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao

    companion object {
        var INSTANCE: HistoryDatabase? = null

        fun getInstance(context: Context): HistoryDatabase {
            if (INSTANCE == null) {
                synchronized(HistoryDatabase::class) {
                    INSTANCE = Room.databaseBuilder(context.applicationContext, HistoryDatabase::class.java, "history.db")
                        // HistoryMemo가 변경되면(버전 변경시) 드랍하고 새롭게 생성한다.
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }

            return INSTANCE!!
        }
    }
}
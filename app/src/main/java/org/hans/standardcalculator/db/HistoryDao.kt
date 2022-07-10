package org.hans.standardcalculator.db

import androidx.room.*

@Dao
interface HistoryDao {
    @Query("SELECT * FROM HistoryMemo")
    fun getAll(): List<HistoryMemo>

//    @Query("SELECT * FROM HistoryMemo WHERE result LIKE :result LIMIT 1")
//    fun findByResult(result: String): HistoryMemo
//
//    @Delete
//    fun deleteHistory(historyMemo: HistoryMemo)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertHistory(vararg historyMemo: HistoryMemo)

    @Query("DELETE FROM HistoryMemo")
    fun deleteAll()
}
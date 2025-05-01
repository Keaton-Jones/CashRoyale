package com.example.cashroyale

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Delete
    suspend fun delete(expense: Expense)

    @Update
    suspend fun update(expense: Expense)
}
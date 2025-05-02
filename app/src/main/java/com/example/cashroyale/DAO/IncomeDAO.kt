package com.example.cashroyale.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cashroyale.Models.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDAO {
    /** Inserts a new income. */
    @Insert
    fun insertIncome(income: Income)

    /** Updates an existing income. */
    @Update
    suspend fun update(income: Income)

    /** Deletes an income. */
    @Delete
    suspend fun delete(income: Income)

    /** Gets all income entries ordered by date descending. */
    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<Income>>

    /** Gets an income entry by its ID. */
    @Query("SELECT * FROM income WHERE incomeId = :id")
    suspend fun getIncomeById(id: Int): Income?
}
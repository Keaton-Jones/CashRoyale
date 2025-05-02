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
    @Insert
    fun insertIncome(income: Income)

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Query("SELECT * FROM income ORDER BY date DESC")
    fun getAllIncome(): Flow<List<Income>>

    @Query("SELECT * FROM income WHERE incomeId = :id")
    suspend fun getIncomeById(id: Int): Income?
}
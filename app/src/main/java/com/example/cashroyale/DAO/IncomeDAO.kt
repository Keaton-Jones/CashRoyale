package com.example.cashroyale.DAO

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.cashroyale.Income

@Dao
interface IncomeDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: Income)

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Query("SELECT * FROM income WHERE userId = :userId ORDER BY date DESC")
    fun getAllIncomeByUser(userId: String): LiveData<List<Income>>

    @Query("SELECT * FROM income WHERE incomeId = :id")
    suspend fun getIncomeById(id: Int): Income?
}
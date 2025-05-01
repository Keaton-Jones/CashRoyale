package com.example.cashroyale.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cashroyale.Models.MonthlyGoals

@Dao
interface MonthlyGoalDAO {
    @Insert
    fun insertMonthlyGoal(monthlyGoals: MonthlyGoals): Long

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId LIMIT 1")
    suspend fun getMonthlyGoalByUserId(userId: String): MonthlyGoals?

    @Update
    suspend fun updateMonthlyGoal(monthlyGoals: MonthlyGoals)
}
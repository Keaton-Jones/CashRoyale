package com.example.cashroyale.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cashroyale.Models.MonthlyGoals
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyGoalDAO {
    @Insert
    suspend fun insertMonthlyGoal(monthlyGoals: MonthlyGoals): Long

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId LIMIT 1")
    suspend fun getMonthlyGoalByUserId(userId: String): MonthlyGoals?

    @Query("SELECT * FROM monthly_goals WHERE userId = :userId ORDER BY goalId DESC LIMIT 1")
    fun getActiveMonthlyGoal(userId: String): Flow<MonthlyGoals?>


    @Update
    suspend fun updateMonthlyGoal(monthlyGoals: MonthlyGoals)
}
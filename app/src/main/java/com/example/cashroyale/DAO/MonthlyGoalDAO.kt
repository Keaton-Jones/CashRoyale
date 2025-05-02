package com.example.cashroyale.DAO

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.cashroyale.Models.MonthlyGoals
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthlyGoalDAO {
    /** Inserts a new monthly goal. */
    @Insert
    suspend fun insertMonthlyGoal(monthlyGoals: MonthlyGoals): Long

    /** Gets the monthly goal for a specific user. */
    @Query("SELECT * FROM monthly_goals WHERE userId = :userId LIMIT 1")
    suspend fun getMonthlyGoalByUserId(userId: String): MonthlyGoals?

    /** Gets the most recent monthly goal for a user. */
    @Query("SELECT * FROM monthly_goals WHERE userId = :userId ORDER BY goalId DESC LIMIT 1")
    fun getActiveMonthlyGoal(userId: String): Flow<MonthlyGoals?>

    /** Updates an existing monthly goal. */
    @Update
    suspend fun updateMonthlyGoal(monthlyGoals: MonthlyGoals)
}
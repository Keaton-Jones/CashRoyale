package com.example.cashroyale

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    // added to be able to pop the cycler view
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Int): Expense?

    @Query("SELECT * FROM expenses WHERE date = :selectedDate ORDER BY date DESC")
    fun getExpensesByDate(selectedDate: String): Flow<List<Expense>>

    // added to be able to pop the cycler view cause initial query was giving an error on fisrt pull
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesOnce(): List<Expense>
}


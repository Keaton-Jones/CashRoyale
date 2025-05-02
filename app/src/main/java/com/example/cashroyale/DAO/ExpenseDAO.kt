package com.example.cashroyale.DAO

import androidx.room.*
import com.example.cashroyale.Models.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDAO {
    /** Inserts a new expense, replacing if it conflicts. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    /** Updates an existing expense. */
    @Update
    suspend fun updateExpense(expense: Expense)

    /** Deletes an expense. */
    @Delete
    suspend fun deleteExpense(expense: Expense)

    /** Gets all expenses ordered by date descending. */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    /** Gets an expense by its ID. */
    @Query("SELECT * FROM expenses WHERE id = :expenseId")
    suspend fun getExpenseById(expenseId: Int): Expense?

    /** Gets expenses for a specific date, ordered by date descending. */
    @Query("SELECT * FROM expenses WHERE date = :selectedDate ORDER BY date DESC")
    fun getExpensesByDate(selectedDate: String): Flow<List<Expense>>

    /** Gets all expenses once (non-reactive), ordered by date descending. */
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    suspend fun getAllExpensesOnce(): List<Expense>
}


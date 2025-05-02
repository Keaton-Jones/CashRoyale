package com.example.cashroyale.repositories

import com.example.cashroyale.DAO.ExpenseDAO
import com.example.cashroyale.Models.Expense
import kotlinx.coroutines.flow.Flow

/**
 * Repository class that handles data operations for [Expense] entities.
 * It abstracts the data source (in this case, a Room database via [ExpenseDAO])
 * from the rest of the application.
 *
 * @param expenseDAO The Data Access Object for the [Expense] entity.
 */
class ExpenseRepository(private val expenseDAO: ExpenseDAO) {
    /**
     * Retrieves all expenses from the database as a Flow.
     * [Flow] allows for asynchronous observation of data changes.
     */
    val allExpenses: Flow<List<Expense>> = expenseDAO.getAllExpenses()

    /**
     * Inserts a new [Expense] into the database.
     *
     * @param expense The [Expense] object to be inserted.
     * @return A [Long] representing the row ID of the newly inserted expense.
     */
    suspend fun insert(expense: Expense): Long {
        return expenseDAO.insertExpense(expense)
    }

    /**
     * Updates an existing [Expense] in the database.
     */
    suspend fun update(expense: Expense) {
        expenseDAO.updateExpense(expense)
    }

    /**
     * Deletes an [Expense] from the database.
     */
    suspend fun delete(expense: Expense) {
        expenseDAO.deleteExpense(expense)
    }

    /**
     * Retrieves an [Expense] from the database based on its ID.
     * @return The [Expense] object with the given ID, or null if not found.
     */
    suspend fun getById(id: Int): Expense? {
        return expenseDAO.getExpenseById(id)
    }
}
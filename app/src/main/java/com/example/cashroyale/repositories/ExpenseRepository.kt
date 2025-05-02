package com.example.cashroyale.repositories

import com.example.cashroyale.DAO.ExpenseDAO
import com.example.cashroyale.Models.Expense
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDAO: ExpenseDAO) {
    val allExpenses: Flow<List<Expense>> = expenseDAO.getAllExpenses()

    suspend fun insert(expense: Expense): Long {
        return expenseDAO.insertExpense(expense)
    }

    suspend fun update(expense: Expense) {
        expenseDAO.updateExpense(expense)
    }

    suspend fun delete(expense: Expense) {
        expenseDAO.deleteExpense(expense)
    }

    suspend fun getById(id: Int): Expense? {
        return expenseDAO.getExpenseById(id)
    }
}
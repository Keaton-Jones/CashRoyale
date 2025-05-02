package com.example.cashroyale.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashroyale.DAO.ExpenseDAO
import com.example.cashroyale.DAO.MonthlyGoalDAO
import com.example.cashroyale.DAO.UserDAO

/**
 * Factory class for creating instances of [CalenderViewModel].
 * Provides the necessary dependencies to the ViewModel during its creation.
 */
class CalenderViewModelFactory(
    private val application: Application,
    private val userDao: UserDAO,
    private val monthlyGoalsDao: MonthlyGoalDAO,
    private val expenseDAO: ExpenseDAO
) : ViewModelProvider.AndroidViewModelFactory(application) {

    /**
     * Creates a new instance of the specified ViewModel class.
     * This factory is specifically designed to create [CalenderViewModel].
     * @return A new instance of [CalenderViewModel] if the modelClass matches,
     * otherwise, it delegates to the superclass's create method.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalenderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalenderViewModel(application, userDao, monthlyGoalsDao, expenseDAO) as T
        }
        return super.create(modelClass)
    }
}
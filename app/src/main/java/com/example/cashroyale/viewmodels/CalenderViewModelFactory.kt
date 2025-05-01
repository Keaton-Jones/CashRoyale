package com.example.cashroyale.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashroyale.DAO.MonthlyGoalDAO
import com.example.cashroyale.DAO.UserDAO

class CalenderViewModelFactory(
    private val application: Application,
    private val userDao: UserDAO,
    private val monthlyGoalsDao: MonthlyGoalDAO
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CalenderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CalenderViewModel(application, userDao, monthlyGoalsDao) as T
        }
        return super.create(modelClass)
    }
}
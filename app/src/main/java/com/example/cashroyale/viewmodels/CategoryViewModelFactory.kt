// CategoryViewModelFactory.kt
package com.example.cashroyale.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.DAO.CategoryDAO
import com.example.cashroyale.DAO.MonthlyGoalDAO

class CategoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val categoryDao: CategoryDAO by lazy {
        AppDatabase.getDatabase(context.applicationContext).categoryDAO()
    }

    private val monthlyGoalDao: MonthlyGoalDAO by lazy {
        AppDatabase.getDatabase(context.applicationContext).monthlyGoalDAO()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryListViewModel(categoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
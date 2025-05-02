// CategoryViewModelFactory.kt
package com.example.cashroyale.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashroyale.DAO.CategoryDAO
import com.example.cashroyale.DAO.MonthlyGoalDAO
import com.example.cashroyale.Models.AppDatabase

/**
 * Factory class for creating instances of [CategoryListViewModel].
 * Provides the necessary dependencies to the ViewModel during its creation.
 */
class CategoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val categoryDao: CategoryDAO by lazy {
        AppDatabase.getDatabase(context.applicationContext).categoryDAO()
    }

    private val monthlyGoalDao: MonthlyGoalDAO by lazy {
        AppDatabase.getDatabase(context.applicationContext).monthlyGoalDAO()
    }

    /**
     * Creates a new instance of the specified ViewModel class.
     * This factory is specifically designed to create [CategoryListViewModel].
     * @return A new instance of [CategoryListViewModel] if the modelClass matches,
     * otherwise, it throws an IllegalArgumentException.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryListViewModel(categoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
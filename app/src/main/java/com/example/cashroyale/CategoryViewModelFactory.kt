// CategoryViewModelFactory.kt
package com.example.cashroyale.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.cashroyale.AppDatabase
import com.example.cashroyale.CategoryDAO
import com.example.cashroyale.CategoryListViewModel

class CategoryViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    private val categoryDao: CategoryDAO by lazy {
        AppDatabase.getDatabase(context.applicationContext).categoryDAO()
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CategoryListViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CategoryListViewModel(categoryDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
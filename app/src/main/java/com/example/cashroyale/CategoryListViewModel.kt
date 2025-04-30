package com.example.cashroyale

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class CategoryListViewModel(private val categoryDao: CategoryDAO) : ViewModel() {

    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories().asLiveData()

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.update(category)
        }
    }


    fun getCategoryById(categoryId: Int): LiveData<Category?> {
        return categoryDao.getCategoryById(categoryId).asLiveData()
    }
}
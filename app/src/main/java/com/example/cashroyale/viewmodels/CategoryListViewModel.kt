package com.example.cashroyale.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.cashroyale.DAO.CategoryDAO
import com.example.cashroyale.Models.Category
import kotlinx.coroutines.launch

/**
 * ViewModel for the CategoryListFragment, responsible for managing and providing category data.
 * It interacts with the CategoryDAO.
 */
class CategoryListViewModel(private val categoryDao: CategoryDAO) : ViewModel() {

    /**
     * LiveData that holds a list of all categories.
     * Updates automatically when the data in the database changes.
     */
    val allCategories: LiveData<List<Category>> = categoryDao.getAllCategories().asLiveData()

    /**
     * LiveData that holds a list of all income categories.
     * Updates automatically when the data in the database changes.
     */
    fun getIncomeCategories(): LiveData<List<Category>> = categoryDao.getCategoriesByType("income").asLiveData()

    /**
     * LiveData that holds a list of all expense categories.
     * Updates automatically when the data in the database changes.
     */
    fun getExpenseCategories(): LiveData<List<Category>> = categoryDao.getCategoriesByType("expense").asLiveData()

    /**
     * Deletes a specific category from the database.
     */
    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.delete(category)
        }
    }

    /**
     * Updates an existing category in the database.
     */
    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryDao.update(category)
        }
    }

    /**
     * Retrieves a specific category from the database based on its ID.
     * @return LiveData that holds the Category object with the given ID, or null if not found.
     */
    fun getCategoryById(categoryId: Int): LiveData<Category?> {
        return categoryDao.getCategoryById(categoryId).asLiveData()
    }
}
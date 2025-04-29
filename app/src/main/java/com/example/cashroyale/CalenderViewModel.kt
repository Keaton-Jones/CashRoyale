package com.example.cashroyale

import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import com.example.cashroyale.databinding.ActivityMainBinding

class CalenderViewModel : ViewModel() {
    private val _navigateToExpenses = MutableLiveData<Boolean>()
    val navigateToExpenses: LiveData<Boolean> get() = _navigateToExpenses

    fun onExpensesButtonClicked() {
        _navigateToExpenses.value = true
    }

    fun onExpensesNavigationComplete() {
        _navigateToExpenses.value = false
    }
}
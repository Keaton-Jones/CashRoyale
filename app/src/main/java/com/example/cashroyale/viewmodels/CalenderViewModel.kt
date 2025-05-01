package com.example.cashroyale.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.cashroyale.DAO.MonthlyGoalDAO
import com.example.cashroyale.DAO.UserDAO
import com.example.cashroyale.Models.MonthlyGoals
import androidx.lifecycle.viewModelScope
import com.example.cashroyale.Models.User
import kotlinx.coroutines.launch

class CalenderViewModel(application: Application, private val userDao: UserDAO, private val monthlyGoalsDao: MonthlyGoalDAO) : AndroidViewModel(application) {

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> = _loggedInUser
    private val _monthlyGoalsSet = MutableLiveData<Boolean>()
    val monthlyGoalsSet: LiveData<Boolean> = _monthlyGoalsSet

    init {
        getLoggedInUserAndCheckGoals()
    }

    private fun getLoggedInUserAndCheckGoals() {
        Log.d("CalenderViewModel", "getLoggedInUserAndCheckGoals() called") // Add log
        viewModelScope.launch { // This ensures the code inside runs on a background thread
            val sharedPreferences = getApplication<Application>().getSharedPreferences(
                "user_prefs",
                Context.MODE_PRIVATE
            )
            val loggedInEmail = sharedPreferences.getString("loggedInEmail", null)
            Log.d(
                "CalenderViewModel",
                "getLoggedInUserAndCheckGoals() - loggedInEmail: $loggedInEmail"
            ) // Add log

            if (loggedInEmail != null) {
                val currentUser = userDao.getUserByEmail(loggedInEmail) // This line is the issue
                _loggedInUser.value = currentUser
                Log.d(
                    "CalenderViewModel",
                    "getLoggedInUserAndCheckGoals() - currentUser: $currentUser"
                ) // Add log
                checkIfGoalsAreSet(loggedInEmail)
            } else {
                _loggedInUser.value = null
                _monthlyGoalsSet.value = false
                Log.d("CalenderViewModel", "getLoggedInUserAndCheckGoals() - No logged-in user") // Add log
            }
        }
    }

    private fun checkIfGoalsAreSet(userId: String) {
        Log.d("CalenderViewModel", "checkIfGoalsAreSet() called for userId: $userId") // Add log
        viewModelScope.launch {
            val existingGoals = monthlyGoalsDao.getMonthlyGoalByUserId(userId)
            _monthlyGoalsSet.value = existingGoals != null
            Log.d(
                "CalenderViewModel",
                "checkIfGoalsAreSet() - existingGoals: $existingGoals, _monthlyGoalsSet.value: ${_monthlyGoalsSet.value}"
            ) // Add log
        }
    }

    fun saveMonthlyGoals(userId: String, maxGoal: Double, minGoal: Double) {
        viewModelScope.launch {
            val newMonthlyGoal = MonthlyGoals(
                userId = userId,
                maxGoalAmount = maxGoal,
                minGoalAmount = minGoal,
                goalSet = true
            )
            monthlyGoalsDao.insertMonthlyGoal(newMonthlyGoal)
            _monthlyGoalsSet.value = true // Goals are now set
        }
    }
}
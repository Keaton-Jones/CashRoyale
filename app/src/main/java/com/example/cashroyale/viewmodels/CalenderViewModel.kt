package com.example.cashroyale.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import com.example.cashroyale.DAO.MonthlyGoalDAO
import com.example.cashroyale.DAO.UserDAO
import com.example.cashroyale.Models.MonthlyGoals
import androidx.lifecycle.viewModelScope
import com.example.cashroyale.DAO.ExpenseDAO
import com.example.cashroyale.Models.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class CalenderViewModel(application: Application, private val userDao: UserDAO, private val monthlyGoalsDao: MonthlyGoalDAO,private val expenseDAO: ExpenseDAO) : AndroidViewModel(application) {

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> = _loggedInUser
    private val _monthlyGoalsSet = MutableLiveData<Boolean>()
    val monthlyGoalsSet: LiveData<Boolean> = _monthlyGoalsSet
    private val _currentMonthlyGoals = MutableLiveData<MonthlyGoals?>()
    val currentMonthlyGoals: LiveData<MonthlyGoals?> = _currentMonthlyGoals
    val maxMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.maxGoalAmount }
    val minMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.minGoalAmount }

    private val _selectedDate = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedDate: LiveData<Long> = _selectedDate

    val totalExpenses: LiveData<Double> = _selectedDate.switchMap { date ->
        liveData {
            val startDateLong = getStartOfMonth(date)
            val endDateLong = getEndOfMonth(date)
            val currentUserId = getCurrentUserId()
            expenseDAO.getAllExpenses() // Get ALL expenses
                .collectLatest { allExpenses ->
                    var sum = 0.0
                    val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) // Assuming your date format

                    allExpenses.forEach { expense ->
                        try {
                            val expenseDate = dateFormatter.parse(expense.date)?.time ?: 0
                            // Inefficient filtering in memory
                            // WARNING: Assuming all expenses belong to the current user!
                            if (expenseDate >= startDateLong && expenseDate <= endDateLong) {
                                sum += expense.amount
                            }
                        } catch (e: Exception) {
                            Log.e("CalenderViewModel", "Error parsing date: ${expense.date}", e)
                        }
                    }
                    emit(sum)
                }
        }
    }


    // New LiveData for remaining maximum budget
    val remainingMaxBudget: LiveData<Double?> = MediatorLiveData<Double?>().apply {
        var maxBudget: Double? = null
        var expenses: Double? = null

        fun update() {
            value = if (maxBudget != null && expenses != null) {
                maxBudget!! - expenses!!
            } else {
                null
            }
        }

        addSource(maxMonthlyBudget) {
            maxBudget = it
            update()
        }

        addSource(totalExpenses) {
            expenses = it
            update()
        }
    }

    init {
        getLoggedInUserAndCheckGoals()
    }

    private fun getLoggedInUserAndCheckGoals() {
        Log.d("CalenderViewModel", "getLoggedInUserAndCheckGoals() called")
        viewModelScope.launch {
            val sharedPreferences = getApplication<Application>().getSharedPreferences(
                "user_prefs",
                Context.MODE_PRIVATE
            )
            val loggedInEmail = sharedPreferences.getString("loggedInEmail", null)
            Log.d("CalenderViewModel", "getLoggedInUserAndCheckGoals() - loggedInEmail: $loggedInEmail")

            if (loggedInEmail != null) {
                val currentUser = userDao.getUserByEmail(loggedInEmail)
                _loggedInUser.value = currentUser
                Log.d("CalenderViewModel", "getLoggedInUserAndCheckGoals() - currentUser: $currentUser")
                checkIfGoalsAreSet(loggedInEmail)
                loadCurrentMonthlyGoals(loggedInEmail)
            } else {
                _loggedInUser.value = null
                _monthlyGoalsSet.value = false
                _currentMonthlyGoals.value = null
                Log.d("CalenderViewModel", "getLoggedInUserAndCheckGoals() - No logged-in user")
            }
        }
    }

    private fun checkIfGoalsAreSet(userId: String) {
        Log.d("CalenderViewModel", "checkIfGoalsAreSet() called for userId: $userId")
        viewModelScope.launch {
            val existingGoals = monthlyGoalsDao.getMonthlyGoalByUserId(userId)
            _monthlyGoalsSet.value = existingGoals != null
            Log.d(
                "CalenderViewModel",
                "checkIfGoalsAreSet() - existingGoals: $existingGoals, _monthlyGoalsSet.value: ${_monthlyGoalsSet.value}"
            )
        }
    }

    private fun loadCurrentMonthlyGoals(userId: String) {
        Log.d("CalenderViewModel", "loadCurrentMonthlyGoals() called for userId: $userId")
        viewModelScope.launch {
            val currentGoals = monthlyGoalsDao.getMonthlyGoalByUserId(userId)
            _currentMonthlyGoals.value = currentGoals
            Log.d(
                "CalenderViewModel",
                "loadCurrentMonthlyGoals() - currentGoals: $currentGoals, _currentMonthlyGoals.value: ${_currentMonthlyGoals.value}"
            )
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
            _monthlyGoalsSet.value = true
            loadCurrentMonthlyGoals(userId)
        }
    }

    private fun getCurrentUserId(): String {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "user_prefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString("loggedInEmail", "") ?: ""
    }

    private fun getStartOfMonth(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    private fun getEndOfMonth(date: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = date
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }
}
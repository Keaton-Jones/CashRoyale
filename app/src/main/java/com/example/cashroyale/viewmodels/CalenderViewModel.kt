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
import androidx.lifecycle.viewModelScope
import com.example.cashroyale.DAO.ExpenseDAO
import com.example.cashroyale.DAO.MonthlyGoalDAO
import com.example.cashroyale.DAO.UserDAO
import com.example.cashroyale.Models.MonthlyGoals
import com.example.cashroyale.Models.User
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel for the CalenderFragment, managing and providing calendar-related data.
 * Interacts with User, MonthlyGoal, and Expense DAOs.
 */
class CalenderViewModel(application: Application, private val userDao: UserDAO, private val monthlyGoalsDao: MonthlyGoalDAO, private val expenseDAO: ExpenseDAO) : AndroidViewModel(application) {

    // LiveData for the currently logged-in user
    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> = _loggedInUser

    // LiveData indicating if monthly goals are set
    private val _monthlyGoalsSet = MutableLiveData<Boolean>()
    val monthlyGoalsSet: LiveData<Boolean> = _monthlyGoalsSet

    // LiveData for the current monthly goals
    private val _currentMonthlyGoals = MutableLiveData<MonthlyGoals?>()
    val currentMonthlyGoals: LiveData<MonthlyGoals?> = _currentMonthlyGoals

    // LiveData for the maximum monthly budget
    val maxMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.maxGoalAmount }

    // LiveData for the minimum monthly budget (savings goal)
    val minMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.minGoalAmount }

    // LiveData for the selected date
    private val _selectedDate = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedDate: LiveData<Long> = _selectedDate

    /**
     * LiveData for the total expenses of the current month.
     * Retrieves all expenses and filters them for the current month.
     */
    val totalExpenses: LiveData<Double> = _selectedDate.switchMap { date ->
        liveData {
            val startDateLong = getStartOfMonth(date)
            val endDateLong = getEndOfMonth(date)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            expenseDAO.getAllExpenses()
                .collectLatest { allExpenses ->
                    var sum = 0.0
                    allExpenses.forEach { expense ->
                        try {
                            val expenseDate = dateFormatter.parse(expense.date)?.time ?: 0
                            // Assuming all fetched expenses belong to the current user
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

    /**
     * LiveData for the remaining maximum budget.
     * Combines maximum budget and total expenses.
     */
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

    /**
     * Retrieves the logged-in user and checks if goals are set.
     */
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

    /**
     * Checks if goals are set for a user.
     */
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

    /**
     * Loads the current monthly goals for a user.
     */
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

    /**
     * Saves monthly goals for a user.
     */
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

    /**
     * Gets the current user ID from SharedPreferences.
     *
     * @return The user ID or an empty string if not found.
     */
    private fun getCurrentUserId(): String {
        val sharedPreferences = getApplication<Application>().getSharedPreferences(
            "user_prefs",
            Context.MODE_PRIVATE
        )
        return sharedPreferences.getString("loggedInEmail", "") ?: ""
    }

    /**
     * Gets the timestamp of the start of the month for a given date.
     * @return The timestamp of the first day of the month at 00:00:00.
     */
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

    /**
     * Gets the timestamp of the end of the month for a given date.
     * @return The timestamp of the last day of the month at 23:59:59.999.
     */
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
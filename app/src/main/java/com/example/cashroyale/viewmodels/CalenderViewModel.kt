package com.example.cashroyale.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.cashroyale.Models.MonthlyGoals
import com.example.cashroyale.Models.User // Ensure this User model aligns with your authentication needs
import com.example.cashroyale.Services.FireStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalenderViewModel(
    application: Application,
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore, // Passed for completeness, but FireStore service uses it
    private val fireStore: FireStore // Your primary data source service
) : AndroidViewModel(application) {

    private val _loggedInUser = MutableLiveData<User?>()
    val loggedInUser: LiveData<User?> = _loggedInUser

    // Initialized to false, updated by Flow observer
    private val _monthlyGoalsSet = MutableLiveData<Boolean>(false)
    val monthlyGoalsSet: LiveData<Boolean> = _monthlyGoalsSet

    // This LiveData will be updated by the Flow observer
    private val _currentMonthlyGoals = MutableLiveData<MonthlyGoals?>()
    val currentMonthlyGoals: LiveData<MonthlyGoals?> = _currentMonthlyGoals

    // Mapped LiveData for UI directly from _currentMonthlyGoals
    val maxMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.maxGoalAmount }
    val minMonthlyBudget: LiveData<Double?> = _currentMonthlyGoals.map { it?.minGoalAmount }

    private val _selectedDate = MutableLiveData<Long>(System.currentTimeMillis())
    val selectedDate: LiveData<Long> = _selectedDate

    // Total expenses for the current month, observed via LiveData and re-calculated when date or transactions change
    val totalExpenses: LiveData<Double> = _selectedDate.switchMap { date ->
        liveData {
            val startDateLong = getStartOfMonth(date)
            val endDateLong = getEndOfMonth(date)
            val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

            val currentUserId = auth.currentUser?.uid
            if (currentUserId == null) {
                emit(0.0)
                return@liveData
            }

            // Collect latest emitted list of transactions from Firestore Flow
            fireStore.getAllTransactions(currentUserId)
                .collectLatest { allTransactions ->
                    var sum = 0.0
                    allTransactions.forEach { transaction ->
                        try {
                            val transactionDate = dateFormatter.parse(transaction.date)?.time ?: 0
                            if (transaction.type == "expense" && transactionDate >= startDateLong && transactionDate <= endDateLong) {
                                sum += transaction.amount
                            }
                        } catch (e: Exception) {
                            Log.e(
                                "CalenderViewModel",
                                "Error parsing date or processing transaction: ${transaction.date}",
                                e
                            )
                        }
                    }
                    emit(sum)
                }
        }
    }

    // Remaining budget calculation, combining maxMonthlyBudget and totalExpenses
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

    // Initialize block: Start observing goals and user status when ViewModel is created
    init {
        getLoggedInUserAndLoadGoals()
    }

    // Fetches logged-in user and sets up real-time observation for monthly goals
    private fun getLoggedInUserAndLoadGoals() {
        Log.d("CalenderViewModel", "getLoggedInUserAndLoadGoals() called")
        viewModelScope.launch {
            val currentUser = auth.currentUser
            Log.d(
                "CalenderViewModel",
                "getLoggedInUserAndLoadGoals() - currentUser UID: ${currentUser?.uid}"
            )

            if (currentUser != null) {
                // Set loggedInUser for UI display if needed.
                _loggedInUser.value = User(
                    email = currentUser.email ?: "",
                    password = ""
                ) // Adjust 'User' model as needed

                val userId = currentUser.uid
                // Start collecting from the real-time flow for monthly goals
                fireStore.getMonthlyGoalsFlow(userId).collectLatest { goals ->
                    _currentMonthlyGoals.value = goals // Update _currentMonthlyGoals LiveData
                    _monthlyGoalsSet.value =
                        goals != null && goals.goalSet // Update _monthlyGoalsSet
                    Log.d(
                        "CalenderViewModel",
                        "Goals Flow updated: $goals, goalsSet: ${_monthlyGoalsSet.value}"
                    )
                }
            } else {
                _loggedInUser.value = null
                _monthlyGoalsSet.value = false
                _currentMonthlyGoals.value = null
                Log.d(
                    "CalenderViewModel",
                    "getLoggedInUserAndLoadGoals() - No logged-in user or user is null"
                )
            }
        }
    }

    // Function to save monthly goals, called from UI (e.g., dialog)
    fun saveMonthlyGoals(maxGoal: Double, minGoal: Double) {
        viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                val newMonthlyGoal = MonthlyGoals(
                    userId = userId, // Assign the current user's ID
                    maxGoalAmount = maxGoal,
                    minGoalAmount = minGoal,
                    goalSet = true
                )
                try {
                    fireStore.saveMonthlyGoal(newMonthlyGoal)
                    Log.d(
                        "CalenderViewModel",
                        "Monthly goals save initiated via ViewModel. Flow will update UI."
                    )
                    // UI will automatically update because the Flow in getLoggedInUserAndLoadGoals()
                    // detects the change in Firestore and updates _currentMonthlyGoals.
                } catch (e: Exception) {
                    Log.e("CalenderViewModel", "Error saving goals via ViewModel: ${e.message}", e)
                    // You might want to expose this error to the UI (e.g., via a separate LiveData for error messages)
                }
            } else {
                Log.e("CalenderViewModel", "Cannot save goals: User not logged in.")
                // Similarly, expose error to UI
            }
        }
    }

    // Date utility functions
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
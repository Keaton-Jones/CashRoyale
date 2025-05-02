package com.example.cashroyale

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class ViewExpenses : AppCompatActivity() {

    private lateinit var edtSelectDate: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var appDatabase: AppDatabase
    private lateinit var expensesAdapter: ExpensesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_list)

        edtSelectDate = findViewById(R.id.edtSelectDate) // Get reference to the EditText for date selection
        recyclerView = findViewById(R.id.recyclerView) // Get reference to the RecyclerView for showing expenses

        appDatabase = AppDatabase.getDatabase(applicationContext) // Initialize the database

        // Set up the RecyclerView to display expenses
        recyclerView.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpensesAdapter(listOf()) // Initialize adapter with an empty list for now
        recyclerView.adapter = expensesAdapter

        // Load all expenses initially when the activity starts
        loadExpenses()

        // Set up date picker when the user clicks on the date field
        edtSelectDate.setOnClickListener {
            showDatePicker()
        }

        // Set up the "Return" button to navigate back
        val returnButton: Button = findViewById(R.id.button2) // Get reference to the return button
        returnButton.setOnClickListener {
            // Close this activity and go back to the previous screen
            finish()
        }
    }

    // Show a date picker dialog when the user clicks on the date field
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            // Format the selected date and set it to the EditText
            val selectedDate = "${selectedYear}-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
            edtSelectDate.setText(selectedDate)
            loadExpenses(selectedDate) // Load expenses for the selected date
        }, year, month, day)

        datePicker.show() // Display the date picker dialog
    }

    // Load expenses from the database (either all or filtered by selected date)
    private fun loadExpenses(date: String? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            // Fetch the list of expenses based on the date (if provided)
            val expensesList: List<Expense> = if (date != null) {
                appDatabase.expenseDAO().getExpensesByDate(date) // Get expenses for the selected date
                    .let { flow -> flow.first() }
            } else {
                appDatabase.expenseDAO().getAllExpensesOnce() // Get all expenses if no date is selected
            }

            // Update the UI with the fetched expenses on the main thread
            launch(Dispatchers.Main) {
                expensesAdapter.updateExpenses(expensesList)
            }
        }
    }
}
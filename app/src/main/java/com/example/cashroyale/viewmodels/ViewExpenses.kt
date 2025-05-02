package com.example.cashroyale.viewmodels

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.Models.Expense
import com.example.cashroyale.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

/**
 * An Activity to display a list of expenses, with options to filter by date and category.
 */
class ViewExpenses : AppCompatActivity() {

    private lateinit var edtSelectDate: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var appDatabase: AppDatabase
    private lateinit var expensesAdapter: ExpensesAdapter
    private lateinit var categorySpinner: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_list)

        // Initialize views
        edtSelectDate = findViewById(R.id.edtSelectDate)
        recyclerView = findViewById(R.id.recyclerView)
        categorySpinner = findViewById(R.id.spinner)
        val returnButton: Button = findViewById(R.id.button2)

        // Initialize the database
        appDatabase = AppDatabase.getDatabase(applicationContext)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpensesAdapter(listOf())
        recyclerView.adapter = expensesAdapter

        // Load all expenses on initial load
        loadExpenses()

        // Set up date selection functionality
        edtSelectDate.setOnClickListener {
            showDatePicker()
        }

        // Set up the button to return to the previous screen
        returnButton.setOnClickListener {
            finish()
        }

        // Load categories into the category filter Spinner
        loadCategoriesIntoSpinner()

        // Set up listener for category selection changes
        categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: android.view.View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position).toString()
                if (selectedCategory == "All") {
                    loadExpenses() // Show all expenses
                } else {
                    loadExpensesByCategory(selectedCategory) // Show expenses for the selected category
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                loadExpenses() // Default to showing all expenses
            }
        }
    }

    /**
     * Loads all available categories from the database and populates the category filter Spinner.
     * Includes an "All" option at the beginning of the list.
     */
    private fun loadCategoriesIntoSpinner() {
        lifecycleScope.launch(Dispatchers.IO) {
            val categoriesFlow = appDatabase.categoryDAO().getAllCategories()
            val categories = categoriesFlow.first()
            val categoryNames = mutableListOf("All") // Add "All" as the first filter option
            categoryNames.addAll(categories.map { it.name })

            launch(Dispatchers.Main) {
                val adapter = ArrayAdapter(
                    this@ViewExpenses,
                    android.R.layout.simple_spinner_item,
                    categoryNames
                )
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                categorySpinner.adapter = adapter // Set the adapter for the Spinner
            }
        }
    }

    /**
     * Displays a DatePickerDialog to allow the user to select a date for filtering expenses.
     */
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "${selectedYear}-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
            edtSelectDate.setText(selectedDate)
            loadExpenses(selectedDate) // Load expenses for the chosen date
        }, year, month, day)

        datePicker.show() // Show the date picker
    }

    /**
     * Loads expenses from the database, optionally filtered by a specific date.
     */
    private fun loadExpenses(date: String? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            val expensesList: List<Expense> = if (date != null) {
                appDatabase.expenseDAO().getExpensesByDate(date)
                    .let { flow -> flow.first() }
            } else {
                appDatabase.expenseDAO().getAllExpensesOnce()
            }

            launch(Dispatchers.Main) {
                expensesAdapter.updateExpenses(expensesList) // Update the RecyclerView with the loaded expenses
            }
        }
    }

    /**
     * Loads expenses from the database filtered by a specific category.
     */
    private fun loadExpensesByCategory(category: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val filteredExpenses = appDatabase.categoryDAO().getExpensesByCategory(category).first()

            launch(Dispatchers.Main) {
                expensesAdapter.updateExpenses(filteredExpenses) // Update the RecyclerView with the filtered expenses
            }
        }
    }
}
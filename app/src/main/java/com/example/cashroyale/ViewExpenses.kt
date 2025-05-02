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

        edtSelectDate = findViewById(R.id.edtSelectDate)
        recyclerView = findViewById(R.id.recyclerView)

        appDatabase = AppDatabase.getDatabase(applicationContext)

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        expensesAdapter = ExpensesAdapter(listOf()) // Empty list for now
        recyclerView.adapter = expensesAdapter

        // Load all expenses initially
        loadExpenses()

        // Set up date picker
        edtSelectDate.setOnClickListener {
            showDatePicker()
        }

        // Set up the "Return" button
        val returnButton: Button = findViewById(R.id.button2) // Assuming button2 is your return button
        returnButton.setOnClickListener {
            // Close this activity and return to Expenses
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            val selectedDate = "${selectedYear}-${(selectedMonth + 1).toString().padStart(2, '0')}-${selectedDay.toString().padStart(2, '0')}"
            edtSelectDate.setText(selectedDate)
            loadExpenses(selectedDate)
        }, year, month, day)

        datePicker.show()
    }

    private fun loadExpenses(date: String? = null) {
        lifecycleScope.launch(Dispatchers.IO) {
            val expensesList: List<Expense> = if (date != null) {
                appDatabase.expenseDAO().getExpensesByDate(date)
                    .let { flow -> flow.first() }
            } else {
                appDatabase.expenseDAO().getAllExpensesOnce() // This returns List<Expense>
            }

            launch(Dispatchers.Main) {
                expensesAdapter.updateExpenses(expensesList)
            }
        }
    }
}


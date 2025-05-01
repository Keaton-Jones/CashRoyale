package com.example.cashroyale

import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class ExpensesList : AppCompatActivity() {

    // RecyclerView and adapter setup
    private lateinit var expenseRecyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var expensesList: MutableList<Expense>
    private lateinit var originalExpensesList: MutableList<Expense>

    // no code is showing, versions < 11 have the permissions restricted
    private val REQUEST_CODE_PERMISSIONS = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expenses_list)

        // Go back to MainActivity
        val backButton = findViewById<Button>(R.id.btnBack)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        // RecyclerView with filler data
        expenseRecyclerView = findViewById(R.id.expenseRecyclerView)
        expenseRecyclerView.layoutManager = LinearLayoutManager(this)

        // Sample data with and without pics
        expensesList = mutableListOf(
            Expense("Lunch", 12.50, "2025-4-25"),
            Expense("Transport", 15.00, "2025-4-24", "android.resource://com.example.cashroyale/drawable/transport"),
            Expense("Coffee", 5.00, "2025-4-23", "android.resource://com.example.cashroyale/drawable/coffee")
        )

        // Keep a copy of the original list so we can reset if user leaves and comes back
        originalExpensesList = ArrayList(expensesList)

        expenseAdapter = ExpenseAdapter(expensesList)
        expenseRecyclerView.adapter = expenseAdapter

        // Set up the DatePicker button
        val selectDateButton: Button = findViewById(R.id.selectDateButton)
        selectDateButton.setOnClickListener {
            openDatePicker()
        }

        // code asks for storage permission, app does not ;(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    REQUEST_CODE_PERMISSIONS)
            }
        }
    }

    // shows the calendar to choose date and filters after
    private fun openDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedYear-${selectedMonth + 1}-$selectedDay"
                Toast.makeText(this, "Selected Date: $selectedDate", Toast.LENGTH_SHORT).show()

                filterExpensesByDate(selectedDate)
            },
            year, month, dayOfMonth
        )

        datePickerDialog.show()
    }

    // Filters the list and shows all again if nothing found
    private fun filterExpensesByDate(selectedDate: String) {
        val filteredList = originalExpensesList.filter { it.date == selectedDate }

        if (filteredList.isEmpty()) {
            Toast.makeText(this, "No expenses found for the selected date", Toast.LENGTH_SHORT).show()

            expensesList.clear()
            expensesList.addAll(originalExpensesList)
        } else {
            expensesList.clear()
            expensesList.addAll(filteredList)
        }

        expenseAdapter.notifyDataSetChanged()
    }

    // Supposed to pop up after user accpts or denies permission, going straght to deny
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_PERMISSIONS -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}

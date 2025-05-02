package com.example.cashroyale.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.cashroyale.DAO.CategoryDAO
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.Models.Category
import com.example.cashroyale.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A DialogFragment for adding new categories.
 */
class WidgetCategoriesFragment : DialogFragment() {
    private var categoryNameEditText: EditText? = null
    private var colourSpinner: Spinner? = null
    private var transactionSpinner: Spinner? = null
    private var manageCategoriesButton: Button? = null
    private lateinit var appDatabase: AppDatabase
    private lateinit var categoryDao: CategoryDAO

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_widget_categories, null)
        builder.setView(view)

        // Initialize database and DAO
        appDatabase = AppDatabase.getDatabase(requireContext())
        categoryDao = appDatabase.categoryDAO()
        // Initialize UI elements
        categoryNameEditText = view.findViewById(R.id.categoryNameEditText)
        colourSpinner = view.findViewById(R.id.colourSpinner)
        transactionSpinner = view.findViewById(R.id.transactionSpinner)
        val okButton = view.findViewById<Button>(R.id.widgetOkButton)
        val cancelButton = view.findViewById<Button>(R.id.widgetCancelButton)
        manageCategoriesButton = view.findViewById(R.id.manageCategoriesButton)

        // Populate the color spinner
        val colors = arrayOf("Red", "Green", "Blue", "Yellow", "Pink", "Orange", "White", "Black")
        val colorAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, colors)
        colourSpinner?.adapter = colorAdapter
        // Populate the transaction type spinner
        val transactionTypes = arrayOf("Income", "Expense")
        val transactionAdapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, transactionTypes)
        transactionSpinner?.adapter = transactionAdapter

        // Set OnClickListener for the OK button to save the new category
        okButton.setOnClickListener {
            val categoryName = categoryNameEditText?.text.toString().trim() // Remove leading/trailing whitespace
            val selectedColor = colourSpinner?.selectedItem.toString()
            val selectedTransaction = transactionSpinner?.selectedItem.toString()

            if (categoryName.isNotBlank()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    // Check if a category with the same name already exists
                    if (!categoryDao.exists(categoryName)) {
                        val category = Category(name = categoryName, color = selectedColor, type = selectedTransaction)
                        categoryDao.insert(category) // Insert the new category into the database
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show()
                            dismiss() // Close the dialog
                        }
                    } else {
                        withContext(Dispatchers.Main){
                            Toast.makeText(requireContext(), "Category name already exists", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        // Set OnClickListener for the Cancel button to dismiss the dialog
        cancelButton.setOnClickListener {
            dismiss()
        }

        // Set OnClickListener for the Manage Categories button to navigate to the CategoryListFragment
        manageCategoriesButton?.setOnClickListener {
            dismiss() // Close the current dialog
            findNavController().navigate(R.id.action_calenderFragment_to_categoryListFragment)
        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear references to views to prevent memory leaks
        categoryNameEditText = null
        colourSpinner = null
        transactionSpinner = null
        manageCategoriesButton = null
    }
}
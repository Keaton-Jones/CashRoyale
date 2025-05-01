package com.example.cashroyale.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
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
 * A simple [Fragment] subclass.
 * Use the [WidgetCategoriesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class WidgetCategoriesFragment : DialogFragment() {
    // TODO: Rename and change types of parameters
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

        appDatabase = AppDatabase.getDatabase(requireContext())
        categoryDao = appDatabase.categoryDAO()
        categoryNameEditText = view.findViewById(R.id.categoryNameEditText)
        colourSpinner = view.findViewById(R.id.colourSpinner)
        transactionSpinner = view.findViewById(R.id.transactionSpinner)
        val okButton = view.findViewById<Button>(R.id.widgetOkButton)
        val cancelButton = view.findViewById<Button>(R.id.widgetCancelButton)

        // Populate the color spinner
        val colors = arrayOf("Red", "Green", "Blue", "Yellow", "Pink", "Orange", "White", "Black") // Add your desired colors
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, colors)
        colourSpinner?.adapter = adapter

        val types = arrayOf("income", "expense")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)
        transactionSpinner?.adapter = typeAdapter

        okButton.setOnClickListener {
            val categoryName = categoryNameEditText?.text.toString().trim() //trim the input
            val selectedColor = colourSpinner?.selectedItem.toString()
            val selectedType = transactionSpinner?.selectedItem.toString()


            if (categoryName.isNotBlank()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (!categoryDao.exists(categoryName)) { // Check if the category exists
                        val category = Category(name = categoryName, color = selectedColor, type = selectedType )
                        categoryDao.insert(category)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show()
                            dismiss()
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

        cancelButton.setOnClickListener {
            dismiss()
        }
        manageCategoriesButton = view.findViewById(R.id.manageCategoriesButton)
        manageCategoriesButton?.setOnClickListener {
            dismiss() // Dismiss the widget
            // Navigate to the CategoryListFragment
            findNavController().navigate(R.id.action_calenderFragment_to_categoryListFragment)

        }

        return builder.create()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        categoryNameEditText = null
        colourSpinner = null
    }
}
package com.example.cashroyale.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.example.cashroyale.viewmodels.CategoryListViewModel
import com.example.cashroyale.Models.Category
import com.example.cashroyale.R
import com.example.cashroyale.viewmodels.CategoryViewModelFactory

class EditCategoryFragment : DialogFragment() {
    private var category: Category? = null
    private var editCategoryNameEditText: EditText? = null
    private var editColorSpinner: Spinner? = null
    private var editTypeSpinner: Spinner? = null // Spinner to edit the category type
    private val viewModel: CategoryListViewModel by activityViewModels {
        CategoryViewModelFactory(requireContext()) // ViewModel shared with the activity
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        /** Creates a new instance of EditCategoryFragment with the category to edit. */
        fun newInstance(category: Category): EditCategoryFragment {
            val fragment = EditCategoryFragment()
            val args = Bundle()
            args.putParcelable(ARG_CATEGORY, category) // Passes the category as a Parcelable
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Retrieves the category from the arguments
        arguments?.let {
            category = it.getParcelable(ARG_CATEGORY)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = LayoutInflater.from(requireContext())
        val view = inflater.inflate(R.layout.fragment_edit_category, null)
        builder.setView(view)

        // Initialize UI elements
        editCategoryNameEditText = view.findViewById(R.id.editCategoryNameEditText)
        editColorSpinner = view.findViewById(R.id.editColourSpinner)
        editTypeSpinner = view.findViewById(R.id.editTypeSpinner) // Initialize the type Spinner
        val cancelButton = view.findViewById<Button>(R.id.editCancelButton)
        val saveButton = view.findViewById<Button>(R.id.editSaveButton)

        // Define available colors and their hex codes
        val colors = arrayOf("Red", "Green", "Blue", "Yellow", "Pink", "Orange", "White", "Black")
        val colorMap = mapOf(
            "Red" to "#FF0000",
            "Green" to "#00FF00",
            "Blue" to "#0000FF",
            "Yellow" to "#FFFF00",
            "Pink" to "#FF1493",
            "Orange" to "#FFA500",
            "White" to "#FFFFFF",
            "Black" to "#000000"
        )
        // Set up the adapter for the color spinner
        val colorAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, colors)
        editColorSpinner?.adapter = colorAdapter

        // Define available category types
        val types = arrayOf("income", "expense")
        // Set up the adapter for the type spinner
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, types)
        editTypeSpinner?.adapter = typeAdapter

        // Populate the fields with the existing category data
        category?.let {
            editCategoryNameEditText?.setText(it.name)
            val colorIndex = colors.indexOf(it.color)
            if (colorIndex != -1) {
                editColorSpinner?.setSelection(colorIndex)
            }
            val typeIndex = types.indexOf(it.type)
            if (typeIndex != -1) {
                editTypeSpinner?.setSelection(typeIndex)
            }
        }

        // Set the OnClickListener for the save button
        saveButton.setOnClickListener {
            val updatedName = editCategoryNameEditText?.text.toString().trim()
            val selectedColorName = editColorSpinner?.selectedItem.toString()
            val updatedColor = colorMap[selectedColorName] ?: "#808080" // Default color if not found
            val selectedType = editTypeSpinner?.selectedItem.toString()

            // Check if the category name is not blank
            if (updatedName.isNotBlank() && category != null) {
                // Create an updated Category object
                val updatedCategory = category!!.copy(name = updatedName, color = updatedColor, type = selectedType)
                // Update the category using the ViewModel
                viewModel.updateCategory(updatedCategory)
                dismiss() // Dismiss the dialog
            } else {
                Toast.makeText(requireContext(), "Please enter a category name", Toast.LENGTH_SHORT).show()
            }
        }

        // Set the OnClickListener for the cancel button
        cancelButton.setOnClickListener {
            dismiss() // Dismiss the dialog
        }

        return builder.create()
    }
}
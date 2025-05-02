package com.example.cashroyale.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.viewmodels.CategoryListViewModel
import com.example.cashroyale.Models.Category
import com.example.cashroyale.R
import com.example.cashroyale.viewmodels.CategoryAdapter
import com.example.cashroyale.viewmodels.CategoryViewModelFactory

class CategoryListFragment : Fragment() {

    private val viewModel: CategoryListViewModel by viewModels {
        CategoryViewModelFactory(requireContext()) // Initializes the ViewModel with a Factory
    }
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private var goToCalendarButton: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflates the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_category_list, container, false)
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext()) // Sets the layout manager for the RecyclerView
        goToCalendarButton = view.findViewById(R.id.goToCalendarButton)

        // Initializes the CategoryAdapter with an empty list and click listeners
        adapter = CategoryAdapter(emptyList(), this::onEditCategory, this::onDeleteCategory)
        categoryRecyclerView.adapter = adapter

        // Observes the LiveData of all categories from the ViewModel
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.updateList(categories) // Updates the RecyclerView adapter with the new list of categories
        }

        // Sets an OnClickListener for the button to navigate to the CalenderFragment
        goToCalendarButton?.setOnClickListener {
            findNavController().navigate(R.id.calenderFragment)
        }

        return view
    }

    /** Handles the edit action for a category. */
    private fun onEditCategory(category: Category) {
        // Creates and shows the EditCategoryFragment dialog for the selected category
        val editDialogFragment = EditCategoryFragment.newInstance(category)
        editDialogFragment.show(childFragmentManager, "editCategoryDialog")
    }

    /** Handles the delete action for a category. */
    private fun onDeleteCategory(category: Category) {
        // Calls the ViewModel function to delete the selected category
        viewModel.deleteCategory(category)
        // The UI will be updated automatically through the LiveData observation
    }
}
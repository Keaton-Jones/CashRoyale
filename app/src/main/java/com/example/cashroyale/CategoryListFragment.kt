package com.example.cashroyale

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.adapter.CategoryAdapter
import com.example.cashroyale.viewmodel.CategoryViewModelFactory

class CategoryListFragment : Fragment() {

    private val viewModel: CategoryListViewModel by viewModels {
        CategoryViewModelFactory(requireContext()) // Assuming you have a ViewModelFactory
    }
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var adapter: CategoryAdapter
    private var goToCalendarButton: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_category_list, container, false)
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        goToCalendarButton = view.findViewById(R.id.goToCalendarButton)

        adapter = CategoryAdapter(emptyList(), this::onEditCategory, this::onDeleteCategory)
        categoryRecyclerView.adapter = adapter

        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            adapter.updateList(categories)
        }

        goToCalendarButton?.setOnClickListener {
            findNavController().navigate(R.id.calenderFragment)
        }

        return view
    }

    private fun onEditCategory(category: Category) {
        // Implement logic to open an edit dialog/screen for the category
        val editDialogFragment = EditCategoryFragment.newInstance(category)
        editDialogFragment.show(childFragmentManager, "editCategoryDialog")
    }

    private fun onDeleteCategory(category: Category) {
        // Implement logic to show a confirmation dialog and then delete the category
        // You'll need to call a ViewModel function to delete from the database
        viewModel.deleteCategory(category)
    }
}
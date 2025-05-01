package com.example.cashroyale

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import com.example.cashroyale.databinding.FragmentCalenderBinding

class CalenderFragment : Fragment() {
private lateinit var binding: FragmentCalenderBinding
    companion object {
        fun newInstance() = CalenderFragment()
    }

    private val viewModel: CalenderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calender, container, false)
        val createCategoryImageButton: ImageButton = view.findViewById(R.id.createCategoryImageButton)

        createCategoryImageButton.setOnClickListener {
            showWidgetDialogFragment()
        }
        val expenseButton: View = view.findViewById(R.id.btnExpenses)
        expenseButton.setOnClickListener {
            val intent = Intent(requireContext(), Expenses::class.java)
            startActivity(intent)
        }

        return view
    }

    private fun showWidgetDialogFragment() {
        val widgetDialogFragment = WidgetCategoriesFragment()
        widgetDialogFragment.show(childFragmentManager, "WidgetDialogFragment")
    }
}
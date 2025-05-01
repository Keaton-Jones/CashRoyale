package com.example.cashroyale.fragments

import android.app.AlertDialog
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.Models.User
import com.example.cashroyale.viewmodels.CalenderViewModel
import com.example.cashroyale.R
import com.example.cashroyale.databinding.FragmentCalenderBinding
import com.example.cashroyale.viewmodels.CalenderViewModelFactory

class CalenderFragment : Fragment() {
    private lateinit var binding: FragmentCalenderBinding
    companion object {
        fun newInstance() = CalenderFragment()
    }

    private val viewModel: CalenderViewModel by viewModels {
        val application = requireActivity().application
        val database = AppDatabase.getDatabase(application) // Get your database instance
        CalenderViewModelFactory(application, database.userDAO(), database.monthlyGoalDAO())
    }
    private var goalsPromptShown = false // To prevent showing the prompt multiple times

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calender, container, false)
        val createCategoryImageButton: ImageButton = view.findViewById(R.id.createCategoryImageButton)

        createCategoryImageButton.setOnClickListener {
            showWidgetDialogFragment()
        }

        // **GOAL PROMPT LOGIC**
        viewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            Log.d("CalenderFragment", "loggedInUser observed: $user") // ADD THIS LOG
            viewModel.monthlyGoalsSet.observe(viewLifecycleOwner) { goalsSet ->
                Log.d("CalenderFragment", "monthlyGoalsSet observed: $goalsSet, goalsPromptShown: $goalsPromptShown") // ADD THIS LOG
                user?.let {
                    if (!goalsSet && !goalsPromptShown) {
                        Log.d("CalenderFragment", "Showing goal input dialog for user: ${it.email}") // ADD THIS LOG
                        showGoalInputDialog(it.email)
                        goalsPromptShown = true
                    } else {
                        Log.d("CalenderFragment", "Goals already set or prompt shown.") // ADD THIS LOG
                    }
                }
            }
        }

        return view
    }

    private fun showWidgetDialogFragment() {
        val widgetDialogFragment = WidgetCategoriesFragment()
        widgetDialogFragment.show(childFragmentManager, "WidgetDialogFragment")
    }

    private fun showGoalInputDialog(userId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set Your Monthly Goals")

        val inputView = LayoutInflater.from(requireContext()).inflate(R.layout.set_goals, null)
        val maxGoalEditText = inputView.findViewById<EditText>(R.id.editTextMaxGoal)
        val minGoalEditText = inputView.findViewById<EditText>(R.id.editTextMinGoal)

        builder.setView(inputView)
            .setPositiveButton("Save") { dialog, which ->
                val maxGoalStr = maxGoalEditText.text.toString().trim()
                val minGoalStr = minGoalEditText.text.toString().trim()

                if (maxGoalStr.isNotEmpty() && minGoalStr.isNotEmpty()) {
                    val maxGoal = maxGoalStr.toDoubleOrNull()
                    val minGoal = minGoalStr.toDoubleOrNull()

                    if (maxGoal != null && minGoal != null && minGoal <= maxGoal) {
                        viewModel.saveMonthlyGoals(userId, maxGoal, minGoal)
                    } else {
                        Toast.makeText(requireContext(), "Invalid goal values.", Toast.LENGTH_LONG).show()
                        showGoalInputDialog(userId)
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter both goals.", Toast.LENGTH_SHORT).show()
                    showGoalInputDialog(userId)
                }
            }
            .setNegativeButton("Later") { dialog, which ->
                dialog.dismiss()
                // Handle postponing if needed
            }
            .setCancelable(false)
        builder.create().show()
    }
}
package com.example.cashroyale.fragments

import android.app.AlertDialog
import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.viewmodels.CalenderViewModel
import com.example.cashroyale.R
import com.example.cashroyale.viewmodels.ViewExpenses
import com.example.cashroyale.databinding.FragmentCalenderBinding
import com.example.cashroyale.viewmodels.CalenderViewModelFactory

class CalenderFragment : Fragment() {
    private lateinit var binding: FragmentCalenderBinding
    companion object {
        /** Creates a new instance of this CalenderFragment. */
        fun newInstance() = CalenderFragment()
    }

    private val viewModel: CalenderViewModel by viewModels {
        val application = requireActivity().application
        val database = AppDatabase.getDatabase(application) // Gets the database instance
        CalenderViewModelFactory(application, database.userDAO(), database.monthlyGoalDAO(), database.expenseDAO())
    }
    private var goalsPromptShown = false // Prevents showing the goal prompt multiple times
    private lateinit var numRemainingBudgetTextView: TextView
    private lateinit var numAmountSpentTextView: TextView
    private lateinit var numMinBudgetTextView: TextView
    private lateinit var numMaxBudgetTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_calender, container, false)
        val createCategoryImageButton: ImageButton = view.findViewById(R.id.createCategoryImageButton)
        numRemainingBudgetTextView = view.findViewById(R.id.numRemainingBudgetTextView)
        numAmountSpentTextView = view.findViewById(R.id.numAmountSpentTextView)
        numMinBudgetTextView = view.findViewById(R.id.numMinBudgetTextView)
        numMaxBudgetTextView = view.findViewById(R.id.numMaxBudgetTextView)

        val expenseButton: View = view.findViewById(R.id.btnExpenses)
        expenseButton.setOnClickListener {
            val intent = Intent(requireContext(), AddExpense::class.java)
            startActivity(intent)
        }

        val incomeButton: View = view.findViewById(R.id.btnIncome)
        incomeButton.setOnClickListener {
            val intent = Intent(requireContext(), AddIncome::class.java)
            startActivity(intent)
        }
        val btnViewExpenses: View = view.findViewById(R.id.btnViewExpenses)
        btnViewExpenses.setOnClickListener {
            val intent = Intent(requireContext(), ViewExpenses::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        createCategoryImageButton.setOnClickListener {
            showWidgetDialogFragment()
        }

        observeViewModel()
        // **GOAL PROMPT LOGIC**
        viewModel.loggedInUser.observe(viewLifecycleOwner) { user ->
            Log.d("CalenderFragment", "loggedInUser observed: $user") // Log user observation
            viewModel.monthlyGoalsSet.observe(viewLifecycleOwner) { goalsSet ->
                Log.d("CalenderFragment", "monthlyGoalsSet observed: $goalsSet, goalsPromptShown: $goalsPromptShown") // Log goals set observation
                user?.let {
                    if (!goalsSet && !goalsPromptShown) {
                        Log.d("CalenderFragment", "Showing goal input dialog for user: ${it.email}") // Log when showing goal dialog
                        showGoalInputDialog(it.email)
                        goalsPromptShown = true
                    } else {
                        Log.d("CalenderFragment", "Goals already set or prompt shown.") // Log when goal dialog is skipped
                    }
                }
            }
        }

        return view
    }

    /** Observes LiveData from the ViewModel to update UI elements. */
    private fun observeViewModel() {
        viewModel.maxMonthlyBudget.observe(viewLifecycleOwner) { maxBudget ->
            numMaxBudgetTextView.text = if (maxBudget != null) "R ${String.format("%.2f", maxBudget)}" else "R N/A"
        }

        viewModel.minMonthlyBudget.observe(viewLifecycleOwner) { minBudget ->
            numMinBudgetTextView.text = if (minBudget != null) "R ${String.format("%.2f", minBudget)}" else "R N/A"
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { totalSpent ->
            numAmountSpentTextView.text = if (totalSpent != null) "R ${String.format("%.2f", totalSpent)}" else "R 0.00"
        }

        viewModel.remainingMaxBudget.observe(viewLifecycleOwner) { remainingBudget ->
            numRemainingBudgetTextView.text = if (remainingBudget != null) "R ${String.format("%.2f", remainingBudget)}" else "R N/A"
        }
    }

    /** Shows a dialog fragment to create new categories. */
    private fun showWidgetDialogFragment() {
        val widgetDialogFragment = WidgetCategoriesFragment()
        widgetDialogFragment.show(childFragmentManager, "WidgetDialogFragment")
    }

    /** Shows an AlertDialog to get the user's monthly budget goals. */
    private fun showGoalInputDialog(userId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Set Your Monthly Goals")

        val inputView = LayoutInflater.from(requireContext()).inflate(R.layout.set_goals, null)
        val maxGoalEditText = inputView.findViewById<EditText>(R.id.editTextMaxGoal)
        val minGoalEditText = inputView.findViewById<EditText>(R.id.editTextMinGoal)

        builder.setView(inputView)
            .setPositiveButton("Save") { _, _ ->
                val maxGoalStr = maxGoalEditText.text.toString().trim()
                val minGoalStr = minGoalEditText.text.toString().trim()

                if (maxGoalStr.isNotEmpty() && minGoalStr.isNotEmpty()) {
                    val maxGoal = maxGoalStr.toDoubleOrNull()
                    val minGoal = minGoalStr.toDoubleOrNull()

                    if (maxGoal != null && minGoal != null && minGoal <= maxGoal) {
                        viewModel.saveMonthlyGoals(userId, maxGoal, minGoal)
                    } else {
                        Toast.makeText(requireContext(), "Invalid goal values.", Toast.LENGTH_LONG).show()
                        showGoalInputDialog(userId) // Re-show the dialog on invalid input
                    }
                } else {
                    Toast.makeText(requireContext(), "Please enter both goals.", Toast.LENGTH_SHORT).show()
                    showGoalInputDialog(userId) // Re-show the dialog if fields are empty
                }
            }
            .setNegativeButton("Later") { dialog, _ ->
                dialog.dismiss()
                // Handle postponing if needed
            }
            .setCancelable(false)
        builder.create().show()
    }
}
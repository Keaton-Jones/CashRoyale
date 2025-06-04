package com.example.cashroyale.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope // Used for coroutines within the Fragment's lifecycle
import com.example.cashroyale.R
import com.example.cashroyale.Services.AuthService
import com.example.cashroyale.Services.FireStore // Your custom FireStore service
// No need to import MonthlyGoals here for the dialog, ViewModel handles data class directly
// import com.example.cashroyale.Models.MonthlyGoals
import com.example.cashroyale.databinding.FragmentCalenderBinding
import com.example.cashroyale.viewmodels.CalenderViewModel
import com.example.cashroyale.viewmodels.CalenderViewModelFactory
import com.example.cashroyale.viewmodels.ViewExpenses // Ensure this path is correct if it's an Activity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CalenderFragment : Fragment() {
    private lateinit var binding: FragmentCalenderBinding
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Declare fireStoreService as lateinit var
    private lateinit var fireStoreService: FireStore

    private val authService = AuthService.getInstance()

    companion object {
        fun newInstance() = CalenderFragment()
    }

    // ViewModel initialization with factory. Initialize fireStoreService here.
    private val viewModel: CalenderViewModel by viewModels {
        val application = requireActivity().application
        fireStoreService = FireStore(db)
        CalenderViewModelFactory(auth, db, fireStoreService, application)
    }

    private var goalsPromptShown = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCalenderBinding.inflate(inflater, container, false)
        val view = binding.root

        // Set up click listeners using binding
        binding.btnExpenses.setOnClickListener {
            val intent = Intent(requireContext(), AddExpense::class.java)
            startActivity(intent)
        }

        binding.btnIncome.setOnClickListener {
            val intent = Intent(requireContext(), AddIncome::class.java)
            startActivity(intent)
        }

        binding.btnViewExpenses.setOnClickListener {
            val intent = Intent(requireContext(), ViewExpenses::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
        }

        binding.createCategoryImageButton.setOnClickListener {
            showWidgetDialogFragment()
        }

        // Set up LiveData observers once in onCreateView
        observeViewModel()

        val userId = auth.currentUser?.uid // Directly get UID from FirebaseAuth
        if (userId.isNullOrEmpty()) {
            Log.e(TAG, "User not authenticated or not logged in. Cannot proceed.")
            Toast.makeText(context, "User not logged in. Please log in.", Toast.LENGTH_LONG).show()
            // Consider navigating to a login screen
            return view
        }
            db.collection("monthlyGoals").whereEqualTo("userId",userId).get().addOnCompleteListener { task ->
            if (task.result.isEmpty) {
                showGoalInputDialog(userId)
            }
        }
        return view
    }

    /** Observes LiveData from the ViewModel to update UI elements. */
    private fun observeViewModel() {
        viewModel.maxMonthlyBudget.observe(viewLifecycleOwner) { maxBudget ->
            binding.numMaxBudgetTextView.text = if (maxBudget != null) "R ${String.format("%.2f", maxBudget)}" else "R N/A"
            Log.d(TAG, "numMaxBudgetTextView updated: ${binding.numMaxBudgetTextView.text}")
        }

        viewModel.minMonthlyBudget.observe(viewLifecycleOwner) { minBudget ->
            binding.numMinBudgetTextView.text = if (minBudget != null) "R ${String.format("%.2f", minBudget)}" else "R N/A"
            Log.d(TAG, "numMinBudgetTextView updated: ${binding.numMinBudgetTextView.text}")
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { totalSpent ->
            binding.numAmountSpentTextView.text = if (totalSpent != null) "R ${String.format("%.2f", totalSpent)}" else "R 0.00"
            Log.d(TAG, "numAmountSpentTextView updated: ${binding.numAmountSpentTextView.text}")
        }

        viewModel.remainingMaxBudget.observe(viewLifecycleOwner) { remainingBudget ->
            binding.numRemainingBudgetTextView.text = if (remainingBudget != null) "R ${String.format("%.2f", remainingBudget)}" else "R N/A"
            Log.d(TAG, "numRemainingBudgetTextView updated: ${binding.numRemainingBudgetTextView.text}")
        }
    }

    /** Shows a dialog fragment to create new categories. */
    private fun showWidgetDialogFragment() {
        val widgetDialogFragment = WidgetCategoriesFragment()
        widgetDialogFragment.show(childFragmentManager, "WidgetDialogFragment")
    }

    /** Shows an AlertDialog to get the user's monthly budget goals. */
    private fun showGoalInputDialog(userId: String) {
        val inputView = LayoutInflater.from(requireContext()).inflate(R.layout.set_goals, null)

        val maxGoalEditText = inputView.findViewById<EditText>(R.id.editTextMaxGoal)
        val minGoalEditText = inputView.findViewById<EditText>(R.id.editTextMinGoal)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Your Monthly Goals")
            .setView(inputView)
            .setCancelable(false) // User must set goals or click "Set Later"
            .create()

        inputView.findViewById<Button>(R.id.btnSetGoals).setOnClickListener {
            val maxGoalStr = maxGoalEditText.text.toString().trim()
            val minGoalStr = minGoalEditText.text.toString().trim()

            if (maxGoalStr.isNotEmpty() && minGoalStr.isNotEmpty()) {
                val maxGoal = maxGoalStr.toDoubleOrNull()
                val minGoal = minGoalStr.toDoubleOrNull()

                if (maxGoal != null && minGoal != null && minGoal <= maxGoal) {
                    // Call the ViewModel's save function
                    lifecycleScope.launch { // Launch coroutine for saving operation
                        try {
                            viewModel.saveMonthlyGoals(maxGoal, minGoal) // ViewModel handles userId internally
                            dialog.dismiss() // Dismiss on successful save
                            Toast.makeText(requireContext(), "Goals saved successfully!", Toast.LENGTH_SHORT).show()
                            Log.d(TAG, "Goals successfully saved and dialog dismissed.")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error saving goals from dialog: ${e.message}", e)
                            Toast.makeText(requireContext(), "Error saving goals: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            // Do NOT dismiss dialog on error, let user correct or cancel
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid goal values. Min goal must be less than or equal to Max goal.", Toast.LENGTH_LONG).show()
                    Log.d(TAG, "Invalid goal input: max=$maxGoalStr, min=$minGoalStr")
                }
            } else {
                Toast.makeText(requireContext(), "Please enter both goals.", Toast.LENGTH_SHORT).show()
                Log.d(TAG, "Empty goal input fields.")
            }
        }

        inputView.findViewById<Button>(R.id.btnSetLater).setOnClickListener {
            dialog.dismiss()
            Log.d(TAG, "User clicked 'Set Later'. Dialog dismissed.")
            // You might want to handle what happens if goals are postponed.
            // For now, goalsPromptShown will prevent it from immediately reappearing.
        }

        Log.d(TAG, "Showing goal input dialog for user: $userId")
        dialog.show()
    }
}
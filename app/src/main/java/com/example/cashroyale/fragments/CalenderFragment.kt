package com.example.cashroyale.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Transactions
import com.example.cashroyale.R
import com.example.cashroyale.Services.AuthService
import com.example.cashroyale.Services.EmailService
import com.example.cashroyale.Services.FireStore
import com.example.cashroyale.databinding.FragmentCalenderBinding
import com.example.cashroyale.viewmodels.CalenderViewModel
import com.example.cashroyale.viewmodels.CalenderViewModelFactory
import com.example.cashroyale.viewmodels.TransactionsAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

class CalenderFragment : Fragment() {

    private var _binding: FragmentCalenderBinding? = null
    private val binding get() = _binding!!

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
    private val emailService = EmailService()

    private lateinit var fireStoreService: FireStore
    private val authService = AuthService.getInstance()

    private val viewModel: CalenderViewModel by viewModels {
        val application = requireActivity().application
        fireStoreService = FireStore(db)
        CalenderViewModelFactory(auth, db, fireStoreService, application)
    }

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionsAdapter

    private val incomeList = mutableListOf<Transactions>()
    private val expenseList = mutableListOf<Transactions>()

    companion object {
        fun newInstance() = CalenderFragment()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalenderBinding.inflate(inflater, container, false)
        val view = binding.root

        // Setup RecyclerView once here
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionsAdapter(emptyList())
        recyclerView.adapter = adapter

        // Button listeners
        binding.btnExpenses.setOnClickListener {
            startActivity(Intent(requireContext(), AddExpense::class.java))
        }

        binding.btnIncome.setOnClickListener {
            startActivity(Intent(requireContext(), AddIncome::class.java))
        }

        binding.btnSendReport.setOnClickListener {
            sendReport()
        }

        binding.createCategoryImageButton.setOnClickListener {
            showWidgetDialogFragment()
        }

        observeViewModel()

        val userId = auth.currentUser?.uid
        if (userId.isNullOrEmpty()) {
            Log.e(TAG, "User not authenticated or not logged in. Cannot proceed.")
            Toast.makeText(context, "User not logged in. Please log in.", Toast.LENGTH_LONG).show()
            return view
        }

        // Check if monthly goals exist and prompt if not
        db.collection("monthlyGoals").whereEqualTo("userId", userId).get().addOnCompleteListener { task ->
            if (task.isSuccessful && task.result.isEmpty) {
                showGoalInputDialog(userId)
            }
        }

        // Start realtime fetching of transactions
        setupRealtimeListeners(userId)

        return view
    }

    private fun sendReport() {
        val max = viewModel.maxMonthlyBudget.value
        val spent = viewModel.totalExpenses.value
        val remaining = viewModel.remainingMaxBudget.value

        var report = "Your monthly budget is R ${String.format("%.2f", max ?: 0.0)}\n" +
                "You spent a total of R ${String.format("%.2f", spent ?: 0.0)} this month\n" +
                "Your remaining budget is R ${String.format("%.2f", remaining ?: 0.0)}\n"

        if (max != null && spent != null && remaining != null) {
            if (max - spent > remaining) {
                report += "You have not reached your minimum monthly goal of R ${String.format("%.2f", viewModel.minMonthlyBudget.value)}"
            } else {
                report += "You have reached your minimum monthly goal of R ${String.format("%.2f", viewModel.minMonthlyBudget.value)}"
            }
        }

        auth.currentUser?.email?.let { email ->
            emailService.sendSpendBreakdownEmail(requireContext(), email, report)
        }
    }

    private fun observeViewModel() {
        viewModel.maxMonthlyBudget.observe(viewLifecycleOwner) { maxBudget ->
            binding.numMaxBudgetTextView.text = maxBudget?.let { "R ${String.format("%.2f", it)}" } ?: "R N/A"
        }

        viewModel.minMonthlyBudget.observe(viewLifecycleOwner) { minBudget ->
            binding.numMinBudgetTextView.text = minBudget?.let { "R ${String.format("%.2f", it)}" } ?: "R N/A"
        }

        viewModel.totalExpenses.observe(viewLifecycleOwner) { totalSpent ->
            binding.numAmountSpentTextView.text = "R ${String.format("%.2f", totalSpent ?: 0.0)}"
        }

        viewModel.remainingMaxBudget.observe(viewLifecycleOwner) { remainingBudget ->
            binding.numRemainingBudgetTextView.text = remainingBudget?.let { "R ${String.format("%.2f", it)}" } ?: "R N/A"
        }
    }

    private fun setupRealtimeListeners(userId: String) {
        db.collection("income")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { incomeSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching income data: ${error.message}", error)
                    return@addSnapshotListener
                }
                incomeList.clear()
                incomeSnapshot?.documents?.forEach { doc ->
                    val income = doc.toObject(Transactions::class.java)
                    income?.type = "income"
                    income?.let { incomeList.add(it) }
                }
                mergeAndDisplayTransactions()
            }

        db.collection("expenses")
            .whereEqualTo("userId", userId)
            .addSnapshotListener { expenseSnapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Error fetching expense data: ${error.message}", error)
                    return@addSnapshotListener
                }
                expenseList.clear()
                expenseSnapshot?.documents?.forEach { doc ->
                    val expense = doc.toObject(Transactions::class.java)
                    expense?.type = "expense"
                    expense?.let { expenseList.add(it) }
                }
                mergeAndDisplayTransactions()
            }
    }

    private fun mergeAndDisplayTransactions() {
        val mergedList = incomeList + expenseList
        val sortedList = mergedList.sortedByDescending { it.date }
        adapter.updateData(sortedList)
    }

    override fun onResume() {
        super.onResume()
        // Refresh the RecyclerView data on fragment resume, if user logged in
        val userId = auth.currentUser?.uid
        if (!userId.isNullOrEmpty()) {
            setupRealtimeListeners(userId)
        }
    }

    private fun showWidgetDialogFragment() {
        val widgetDialogFragment = WidgetCategoriesFragment()
        widgetDialogFragment.show(childFragmentManager, "WidgetDialogFragment")
    }

    private fun showGoalInputDialog(userId: String) {
        val inputView = LayoutInflater.from(requireContext()).inflate(R.layout.set_goals, null)

        val maxGoalEditText = inputView.findViewById<EditText>(R.id.editTextMaxGoal)
        val minGoalEditText = inputView.findViewById<EditText>(R.id.editTextMinGoal)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Set Your Monthly Goals")
            .setView(inputView)
            .setCancelable(false)
            .create()

        inputView.findViewById<Button>(R.id.btnSetGoals).setOnClickListener {
            val maxGoalStr = maxGoalEditText.text.toString().trim()
            val minGoalStr = minGoalEditText.text.toString().trim()

            if (maxGoalStr.isNotEmpty() && minGoalStr.isNotEmpty()) {
                val maxGoal = maxGoalStr.toDoubleOrNull()
                val minGoal = minGoalStr.toDoubleOrNull()

                if (maxGoal != null && minGoal != null && minGoal <= maxGoal) {
                    lifecycleScope.launch {
                        try {
                            viewModel.saveMonthlyGoals(maxGoal, minGoal)
                            dialog.dismiss()
                            Toast.makeText(requireContext(), "Goals saved successfully!", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e(TAG, "Error saving goals from dialog: ${e.message}", e)
                            Toast.makeText(requireContext(), "Error saving goals: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(requireContext(), "Invalid goal values. Min goal must be less than or equal to Max goal.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(requireContext(), "Please enter both goals.", Toast.LENGTH_SHORT).show()
            }
        }

        inputView.findViewById<Button>(R.id.btnSetLater).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

package com.example.cashroyale.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.cashroyale.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class GoalFragment : Fragment() {

    private lateinit var tvGoalStatus: TextView
    private lateinit var progressBarGoalOverview: ProgressBar
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate layout and get references to views
        val view = inflater.inflate(R.layout.fragment_goal, container, false)
        tvGoalStatus = view.findViewById(R.id.tvGoalStatus)
        progressBarGoalOverview = view.findViewById(R.id.progressBarGoalOverview)
        return view
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase instances
        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Get current user ID or return if null
        val userId = auth.currentUser?.uid ?: return
        fetchGoalAndExpenses(userId) // Fetch data from Firestore
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun fetchGoalAndExpenses(userId: String) {
        // Get the user's monthly goals document
        db.collection("monthlyGoals").document(userId).get()
            .addOnSuccessListener { goalDoc ->
                if (goalDoc != null && goalDoc.getBoolean("goalSet") == true) {
                    val minGoal = goalDoc.getDouble("minGoalAmount") ?: 0.0
                    val maxGoal = goalDoc.getDouble("maxGoalAmount") ?: 0.0

                    // Get current month and year as strings
                    val currentMonth = LocalDate.now().monthValue.toString().padStart(2, '0')
                    val currentYear = LocalDate.now().year.toString()
                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

                    // Query expenses for the user
                    db.collection("expenses")
                        .whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { expenseSnap ->
                            var totalSpent = 0.0

                            // Sum expenses for current month and year
                            for (doc in expenseSnap) {
                                val dateStr = doc.getString("date") ?: continue
                                val date = LocalDate.parse(dateStr, formatter)
                                if (date.monthValue == currentMonth.toInt() && date.year == currentYear.toInt()) {
                                    totalSpent += doc.getDouble("amount") ?: 0.0
                                }
                            }

                            updateUI(totalSpent, minGoal, maxGoal) // Update UI with data
                        }
                } else {
                    // No goal set case
                    tvGoalStatus.text = "No goal set."
                    progressBarGoalOverview.progress = 0
                }
            }
            .addOnFailureListener {
                // Handle Firestore failure
                tvGoalStatus.text = "Failed to fetch goals."
                progressBarGoalOverview.progress = 0
            }
    }

    private fun updateUI(spent: Double, min: Double, max: Double) {
        val statusText: String
        val statusColor: Int

        // Determine status text and color based on spending
        when {
            spent < min -> {
                statusText = "Below Minimum Spending.\nYou can spend more!\n\n" +
                        "Niko is usually the one who loves to spend and enjoy life, so don’t be afraid to treat yourself occasionally. " +
                        "Consider allocating some budget for things that improve your well-being, like eating better meals, socializing with friends, or taking short trips. " +
                        "Remember, spending wisely on experiences can recharge your motivation for uni and life in South Africa. Just avoid impulsive splurges."
                statusColor = Color.parseColor("#388E3C") // Green 700
            }
            spent in min..max -> {
                statusText = "You're within your goal! Keep it up!\n\n" +
                        "Keaton is the sensible one who sticks to his budget and plans ahead. Keep tracking your expenses like he does. " +
                        "Prioritize essentials like rent, groceries, and transport, but set aside some funds for hobbies or saving for future goals. " +
                        "Maintaining this balance helps avoid stress and builds good financial habits for after university."
                statusColor = Color.parseColor("#F57C00") // Orange 700
            }
            else -> {
                statusText = "You've exceeded your spending goal! Time to slow down!\n\n" +
                        "Kazi knows the importance of cutting back when spending gets out of control. Review your expenses carefully and identify non-essential costs to reduce. " +
                        "Try cooking more at home, limit takeouts, and avoid unnecessary subscriptions. Use budgeting apps or spreadsheets to track and plan your spending. " +
                        "This discipline now will make a big difference both in your studies and long-term financial health."
                statusColor = Color.parseColor("#D32F2F") // Red 700
            }
        }

        // Compose spending summary text
        val spentText = "This Month's Spending:\nR%.2f\n\n".format(spent)
        val fullText = spentText + statusText

        val spannable = SpannableString(fullText)

        // Bold the "This Month's Spending:" label
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            0,
            "This Month's Spending:".length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Bold the spending amount
        spannable.setSpan(
            StyleSpan(Typeface.BOLD),
            "This Month's Spending:\n".length,
            spentText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set status text color based on spending level
        spannable.setSpan(
            ForegroundColorSpan(statusColor),
            spentText.length,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the styled text to the TextView
        tvGoalStatus.text = spannable

        // Add padding and increase text size for readability
        tvGoalStatus.setPadding(32, 32, 32, 32)
        tvGoalStatus.textSize = 18f

        // Calculate progress as percentage of max goal (0 to 100)
        val progress = ((spent / max) * 100).toInt().coerceIn(0, 100)
        progressBarGoalOverview.progress = progress

        // Change ProgressBar color to match status color
        progressBarGoalOverview.progressTintList = android.content.res.ColorStateList.valueOf(statusColor)
    }
}

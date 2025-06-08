package com.example.cashroyale

// Kotlin
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cashroyale.Models.Transactions
import com.example.cashroyale.viewmodels.TransactionsAdapter
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * The main activity of the CashRoyale application.
 * Sets up the bottom navigation and links it to the navigation host fragment.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Your existing bottom nav setup
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.let {
            val navController = it.navController
            NavigationUI.setupWithNavController(bottomNavigationView, navController)
        }

        // --- NEW CODE for RecyclerView ---
        recyclerView = findViewById(R.id.recyclerView) // Make sure this ID matches your XML
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchTransactions()
    }

    private fun fetchTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val db = FirebaseFirestore.getInstance()

        val incomeList = mutableListOf<Transactions>()
        val expenseList = mutableListOf<Transactions>()

        db.collection("income")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener { incomeSnapshot ->
                for (doc in incomeSnapshot.documents) {
                    val income = doc.toObject(Transactions::class.java)
                    income?.type = "income"
                    income?.let { incomeList.add(it) }
                }

                db.collection("expenses")
                    .whereEqualTo("userId", userId)
                    .get()
                    .addOnSuccessListener { expenseSnapshot ->
                        for (doc in expenseSnapshot.documents) {
                            val expense = doc.toObject(Transactions::class.java)
                            expense?.type = "expense"
                            expense?.let { expenseList.add(it) }
                        }

                        mergeAndDisplay(incomeList, expenseList)
                    }
            }
    }

    private fun mergeAndDisplay(incomeList: List<Transactions>, expenseList: List<Transactions>) {
        // Combine income and expense lists
        val mergedList = incomeList + expenseList

        // Sort by date descending (assuming date format "YYYY-MM-DD")
        val sortedList = mergedList.sortedByDescending { it.date }

        // Initialize adapter with sorted list
        adapter = TransactionsAdapter(sortedList)

        // Attach adapter to RecyclerView
        recyclerView.adapter = adapter
    }
}

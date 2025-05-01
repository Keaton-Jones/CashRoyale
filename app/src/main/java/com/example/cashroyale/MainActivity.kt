package com.example.cashroyale

// Kotlin
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        navHostFragment?.let {
            val navController = it.navController
            NavigationUI.setupWithNavController(bottomNavigationView, navController)
        }

        val goToExpensesButton: Button = findViewById(R.id.btnExpenseList)
        goToExpensesButton.setOnClickListener {
            val intent = Intent(this, ExpensesList::class.java)
            startActivity(intent)
        }
    }
}
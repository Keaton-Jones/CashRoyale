package com.example.cashroyale

// Kotlin
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as? NavHostFragment

        Log.d("MainActivity", "bottomNavigationView: $bottomNavigationView")
        Log.d("MainActivity", "navHostFragment: $navHostFragment")

        navHostFragment?.let {
            val navController = it.navController
            NavigationUI.setupWithNavController(bottomNavigationView, navController)
        }
    }
}
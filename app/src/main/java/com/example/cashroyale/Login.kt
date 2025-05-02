package com.example.cashroyale

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cashroyale.DAO.UserDAO
import com.example.cashroyale.Models.AppDatabase
import com.example.cashroyale.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for user login. Allows existing users to log in and provides a link to the registration screen.
 */
class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userDAO: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Initialize the User Data Access Object
        userDAO = AppDatabase.getDatabase(this).userDAO()

        // Handle edge-to-edge screen display
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Set click listener for the login button
        binding.loginButton.setOnClickListener() {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            // Check if both email and password fields are filled
            if (email.isNotEmpty() && password.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val checkUser = userDAO.getUserByEmail(email)

                    withContext(Dispatchers.Main) {
                        if (checkUser != null) {
                            // Verify the entered password against the stored password
                            if (checkUser.password == password) {
                                Toast.makeText(
                                    this@Login,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()

                                // **SAVE LOGGED-IN EMAIL TO SHARED PREFERENCES**
                                val sharedPreferences = getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
                                val editor = sharedPreferences.edit()
                                editor.putString("loggedInEmail", checkUser.email)
                                editor.apply() // Asynchronously saves the changes

                                // Navigate to the main activity
                                intent = Intent(this@Login, MainActivity::class.java)
                                startActivity(intent)
                                finish() // Prevent the user from going back to the login screen
                            } else {
                                Toast.makeText(
                                    this@Login,
                                    "Incorrect password.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@Login,
                                "Email not found.",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    }
                }
            } else {
                Toast.makeText(
                    this@Login,
                    "Please enter both email and password.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set click listener for the register button to navigate to the registration screen
        binding.registerButton.setOnClickListener(){
            intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish() // Prevent the user from going back to the login screen on back press
        }
    }
}
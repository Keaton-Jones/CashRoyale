package com.example.cashroyale

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
import com.example.cashroyale.Models.User
import com.example.cashroyale.databinding.ActivityRegisterBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Activity for user registration. Allows new users to create an account.
 * Implements password complexity checks and email validation.
 */
class Register : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var userDAO: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize the User Data Access Object
        userDAO = AppDatabase.getDatabase(this).userDAO()

        // Set click listener for the registration confirmation button
        binding.confirmRegButton.setOnClickListener() {

            val email = binding.regUsernameEditText.text.toString()
            val password = binding.regPasswordEditText.text.toString()
            val confirmPassword = binding.regConfirmPasswordEditText.text.toString()
            val specialCharacters =
                listOf('@', '#', '$', '%', '&', '*', '.', '!', '?', '-', '_', '+', '=')

            // Check if all fields are filled
            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                // Validate email format
                if (email.contains("@") && email.contains(".")) {
                    // Check password length
                    if (password.length < 8) {
                        Toast.makeText(
                            this@Register,
                            "Password must be at least 8 characters.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Check for at least one special character
                        if (password.any { it in specialCharacters }) {
                            // Check for both uppercase and lowercase letters
                            if (password.contains(Regex("[A-Z]")) && password.contains(Regex("[a-z]"))) {
                                // Check for at least one digit
                                if (password.contains(Regex("[0-9]"))) {
                                    // Check if passwords match
                                    if (password == confirmPassword) {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            // Check if the email already exists
                                            val checkUser = userDAO.getUserByEmail(email)
                                            if (checkUser == null) {
                                                // Insert the new user into the database
                                                userDAO.insertUser(User(email, password))
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        this@Register,
                                                        "Registration successful!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    // Navigate to the login screen
                                                    intent =
                                                        Intent(this@Register, Login::class.java)
                                                    startActivity(intent)
                                                    finish() // Prevent going back to registration
                                                }
                                            } else {
                                                Toast.makeText(
                                                    this@Register,
                                                    "Email already exists.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }

                                    } else {
                                        Toast.makeText(
                                            this@Register,
                                            "Passwords do not match.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        this@Register,
                                        "Password must contain at least one number.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } else {
                                Toast.makeText(
                                    this@Register,
                                    "Password must contain both uppercase and lowercase letters.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@Register,
                                "Password must contain at least one special character.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    Toast.makeText(
                        this@Register,
                        "Please enter a valid email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    this@Register,
                    "Please enter all fields.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
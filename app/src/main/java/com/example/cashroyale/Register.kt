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

        userDAO = AppDatabase.getDatabase(this).userDAO()

        binding.confirmRegButton.setOnClickListener() {

            val email = binding.regUsernameEditText.text.toString()
            val password = binding.regPasswordEditText.text.toString()
            val confirmPassword = binding.regConfirmPasswordEditText.text.toString()
            val specialCharacters =
                listOf('@', '#', '$', '%', '&', '*', '.', '!', '?', '-', '_', '+', '=')

            if (email != "" && password != "" && confirmPassword != "") {
                if(email.contains("@") && email.contains(".")) {
                    if (password.length < 8) {
                        Toast.makeText(
                            this@Register,
                            "Password must be at least 8 characters.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        if (password.any { it in specialCharacters }) {
                            if (password.contains(Regex("[A-Z]")) && password.contains(Regex("[a-z]"))) {
                                if (password.contains(Regex("[0-9]"))) {
                                    if (password == confirmPassword) {
                                        lifecycleScope.launch(Dispatchers.IO) {
                                            val checkUser = userDAO.getUserByEmail(email)
                                            if (checkUser == null) {
                                                userDAO.insertUser(User(email, password))
                                                withContext(Dispatchers.Main) {
                                                    Toast.makeText(
                                                        this@Register,
                                                        "Registration successful!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    intent =
                                                        Intent(this@Register, Login::class.java)
                                                    startActivity(intent)
                                                    finish()
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
                }else{
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
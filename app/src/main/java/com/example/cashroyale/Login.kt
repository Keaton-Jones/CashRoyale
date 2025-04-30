package com.example.cashroyale

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.cashroyale.databinding.ActivityLoginBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userDAO: UserDAO

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        userDAO = AppDatabase.getDatabase(this).userDAO()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.loginButton.setOnClickListener() {
            val email = binding.emailEditText.text.toString()
            val password = binding.passwordEditText.text.toString()


            if (email != "" && password != "") {
                lifecycleScope.launch(Dispatchers.IO) {
                    val checkUser = userDAO.getUserByEmail(email)

                    withContext(Dispatchers.Main) {
                        if (checkUser != null) {
                            if (checkUser.password == password) {
                                Toast.makeText(
                                    this@Login,
                                    "Login successful!",
                                    Toast.LENGTH_SHORT
                                )
                                    .show()
                                 intent = Intent(this@Login, MainActivity::class.java)
                                startActivity(intent)
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

        binding.registerButton.setOnClickListener(){
            intent = Intent(this, Register::class.java)
            startActivity(intent)
            finish()
        }
    }
}
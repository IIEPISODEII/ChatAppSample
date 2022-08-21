package com.example.chatappsample.presentation.view

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    lateinit var viewModel : UserViewModel

    private val nameEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_name) }
    private val emailEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_e_mail) }
    private val passwordEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_password) }
    private val signupButton by lazy { this.findViewById<MaterialButton>(R.id.btn_signup_sign_up) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]
        viewModel.registrationStatus.observe(this) {
            when (it) {
                is Resource.Success -> {
                    
                }
                is Resource.Loading -> {

                }
                else -> {

                }
            }
        }

        signupButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "Please input your name.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                Toast.makeText(this, "Please input your e-mail.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please input password.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signUp(name, email, password)
        }
    }
}
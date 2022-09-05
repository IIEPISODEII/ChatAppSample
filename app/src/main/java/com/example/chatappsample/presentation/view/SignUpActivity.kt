package com.example.chatappsample.presentation.view

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.example.chatappsample.util.CharLengthInputFilter
import com.example.chatappsample.util.LetterDigitsInputFilter
import com.example.chatappsample.util.Resource
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity : AppCompatActivity() {

    lateinit var viewModel : UserViewModel

    private val nameEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_name) }
    private val emailEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_e_mail) }
    private val passwordEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_password) }
    private val passwordCheckEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_password_check) }
    private val signupButton by lazy { this.findViewById<MaterialButton>(R.id.btn_signup_sign_up) }
    private val passwordCheckRulesTextView by lazy { this.findViewById<MaterialTextView>(R.id.tv_signup_password_check_rules) }

    private val passwordPattern = "^[a-zA-Z0-9]".toRegex()

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

        nameEditText.filters = arrayOf(LetterDigitsInputFilter(), CharLengthInputFilter(10))

        passwordCheckEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) passwordCheckRulesTextView.visibility = View.INVISIBLE
        }

        signupButton.setOnClickListener {
            val name = nameEditText.text.toString()
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val passwordChecker = passwordCheckEditText.text.toString()

            if (name.isEmpty()) {
                Toast.makeText(this, "닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                Toast.makeText(this, "메일주소를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (!passwordPattern.matches(password) || password.length !in 8..16) {
                Toast.makeText(this, resources.getText(R.string.password_rules), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordChecker) {
                passwordCheckRulesTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            viewModel.signUp(name, email, password)
        }
    }
}
package com.example.chatappsample.presentation.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.domain.`interface`.OnEmailVerificationListener
import com.example.chatappsample.domain.`interface`.OnSendEmailVerificationListener
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
    private val sendEmailVerificationButton by lazy { this.findViewById<MaterialButton>(R.id.btn_send_verification_mail) }
    private val passwordEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_password) }
    private val passwordCheckEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_signup_password_check) }
    private val signupButton by lazy { this.findViewById<MaterialButton>(R.id.btn_signup_sign_up) }
    private val passwordCheckRulesTextView by lazy { this.findViewById<MaterialTextView>(R.id.tv_signup_password_check_rules) }

    private val passwordPattern = "^[a-zA-Z0-9]{8,16}".toRegex()

    val sendVerificationEmailListener = object: OnSendEmailVerificationListener {
        override fun onSuccess() {
            Toast.makeText(this@SignUpActivity, "이메일 주소로 인증메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
        }

        override fun onSendEmailVerificationFail() {
            Toast.makeText(this@SignUpActivity, "이메일 주소로 인증메일 보내기를 실패했습니다.", Toast.LENGTH_SHORT).show()
        }

        override fun onStart() {

        }

        override fun <T> onFailure(error: T) {
            Toast.makeText(this@SignUpActivity, "인증을 실패했습니다.", Toast.LENGTH_SHORT).show()
        }
    }
    val onEmailVerificationListener = object: OnEmailVerificationListener {
        override fun onSuccess() {
            Toast.makeText(this@SignUpActivity, "가입이 완료됐습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        override fun onFailure() {
            Toast.makeText(this@SignUpActivity, "가입이 실패했습니다. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        viewModel = ViewModelProvider(this)[UserViewModel::class.java]

        nameEditText.filters = arrayOf(LetterDigitsInputFilter(), CharLengthInputFilter(10))

        passwordCheckEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) passwordCheckRulesTextView.visibility = View.INVISIBLE
        }

        sendEmailVerificationButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            viewModel.sendVerificationEmail(email, password, sendVerificationEmailListener)
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
            if (!passwordPattern.matches(password)) {
                Toast.makeText(this, resources.getText(R.string.password_rules), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != passwordChecker) {
                passwordCheckRulesTextView.visibility = View.VISIBLE
                return@setOnClickListener
            }

            viewModel.signUpWithVerifiedEmail(name, email, password, onEmailVerificationListener)
        }
    }
}
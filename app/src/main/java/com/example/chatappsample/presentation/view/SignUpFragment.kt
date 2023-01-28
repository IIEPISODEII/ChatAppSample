package com.example.chatappsample.presentation.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.domain.`interface`.EmailVerificationSendListener
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this@SignUpFragment.requireActivity())[UserViewModel::class.java] }
    private lateinit var fragmentSignUp: View

    private lateinit var emailEditText : TextInputEditText
    private lateinit var passwordEditText : TextInputEditText
    private lateinit var passwordCheckEditText : TextInputEditText
    private lateinit var signupButton : MaterialButton
    private lateinit var passwordCheckRulesTextView : MaterialTextView

    private val passwordPattern = "^[a-zA-Z0-9]{8,16}".toRegex()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        container?.removeAllViews()

        fragmentSignUp = inflater.inflate(R.layout.fragment_create_new_account, container, false)

        emailEditText = fragmentSignUp.findViewById(R.id.et_signup_e_mail)
        passwordEditText = fragmentSignUp.findViewById(R.id.et_signup_password)
        passwordCheckEditText = fragmentSignUp.findViewById(R.id.et_signup_password_check)
        signupButton = fragmentSignUp.findViewById(R.id.btn_signup_sign_up)
        passwordCheckRulesTextView = fragmentSignUp.findViewById(R.id.tv_signup_password_check_rules)

        passwordCheckEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) passwordCheckRulesTextView.visibility = View.INVISIBLE
        }

        signupButton.setOnClickListener(btnClickListener)

        return fragmentSignUp
    }

    private val btnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) return

            when (v) {
                signupButton -> {
                    val email = emailEditText.text.toString()
                    val password= passwordEditText.text.toString()
                    val passwordChecker = passwordCheckEditText.text.toString()

                    if (email.isEmpty()) {
                        Toast.makeText(this@SignUpFragment.requireActivity(), "메일주소를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    if (!passwordPattern.matches(password)) {
                        Toast.makeText(this@SignUpFragment.requireActivity(), resources.getText(R.string.password_rules), Toast.LENGTH_SHORT).show()
                        return
                    }

                    if (passwordChecker != password) {
                        passwordCheckRulesTextView.visibility = View.VISIBLE
                        return
                    }

                    viewModel.sendVerificationEmail(email, password, sendVerificationEmailListener)
                }
                else -> {

                }
            }
        }
    }

    private val sendVerificationEmailListener = object: EmailVerificationSendListener {
        override fun onSuccess() {
            Toast.makeText(this@SignUpFragment.requireActivity(), "이메일 주소로 인증메일을 보냈습니다.", Toast.LENGTH_SHORT).show()
            this@SignUpFragment.requireActivity()
                .supportFragmentManager
                .beginTransaction()
                .replace(R.id.framelayout_signin_activity, EmailVerificationFragment())
                .commit()
        }

        override fun onSendEmailVerificationFail() {
            Toast.makeText(this@SignUpFragment.requireActivity(), "이메일 주소로 인증메일 보내기를 실패했습니다.", Toast.LENGTH_SHORT).show()
        }

        override fun onStart() {

        }
    }
}
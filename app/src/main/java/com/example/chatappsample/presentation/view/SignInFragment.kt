package com.example.chatappsample.presentation.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatappsample.R
import com.example.chatappsample.domain.`interface`.SignInListener
import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.AuthResult
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SignInFragment : Fragment() {

    @Inject
    lateinit var sharedPreferenceRepo: SharedPreferenceRepository

    private lateinit var fragmentSignIn: View
    private val viewModel by lazy { ViewModelProvider(this)[UserViewModel::class.java] }

    private val emailEditText: TextInputEditText by lazy { this@SignInFragment.requireActivity().findViewById(R.id.et_login_e_mail) }
    private val passwordEditText: TextInputEditText by lazy { this@SignInFragment.requireActivity().findViewById(R.id.et_login_password) }
    private val signinButton: MaterialButton by lazy { this@SignInFragment.requireActivity().findViewById(R.id.btn_sign_in) }
    private val signupButton: MaterialButton by lazy { this@SignInFragment.requireActivity().findViewById(R.id.btn_login_sign_up) }
    private val autoLoginCheckBox: MaterialCheckBox by lazy { this@SignInFragment.requireActivity().findViewById(R.id.checkbox_auto_login) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        fragmentSignIn = inflater.inflate(R.layout.fragment_sign_in, container, false)
        return fragmentSignIn
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signinButton.setOnClickListener(btnClickListener)
        signupButton.setOnClickListener(btnClickListener)
    }

    override fun onResume() {
        autoLoginCheckBox.isChecked = viewModel.getAutoLoginCheck()

        if (autoLoginCheckBox.isChecked) {
            val email = sharedPreferenceRepo.getEmailAddress()
            val password = sharedPreferenceRepo.getPassword()

            login(email, password)
            Toast.makeText(this.requireActivity(), "자동로그인 완료", Toast.LENGTH_SHORT).show()
        }

        super.onResume()
    }

    private val btnClickListener = object: View.OnClickListener {
        override fun onClick(v: View?) {
            if (v == null) return

            when (v) {
                signinButton -> {
                    val email = emailEditText.text.toString()
                    val password = passwordEditText.text.toString()

                    if (email.isEmpty()) {
                        Toast.makeText(v.context, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return
                    }
                    if (password.isEmpty()) {
                        Toast.makeText(v.context, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return
                    }

                    if (autoLoginCheckBox.isChecked) {
                        sharedPreferenceRepo.let {
                            it.setEmailAddress(email)
                            it.setPassword(password)
                            it.setAutoLoginChecked(true)
                        }
                    }
                    login(email, password)
                }
                signupButton -> {
                    this@SignInFragment.requireActivity()
                        .supportFragmentManager
                        .beginTransaction()
                        .replace(R.id.framelayout_signin_activity, SignUpFragment())
                        .commit()
                }
            }
        }
    }

    private fun login(email: String, password: String) {

        val signInListener = object: SignInListener {
            override fun <T> onSuccess(successParam: T) {
                if (successParam is AuthResult) {
                    val user = successParam.user ?: return
                    val isEmailVerified = user.isEmailVerified

                    if (isEmailVerified) {
                        UserViewModel.setCurrentUserId(user.uid)
                        this@SignInFragment.requireActivity().finish()

                        val mIntent = Intent(this@SignInFragment.requireActivity(), MainActivity::class.java)
                        startActivity(mIntent)
                    } else {
                        val verificationFragment = EmailVerificationFragment()
                        this@SignInFragment.requireActivity()
                            .supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.framelayout_signin_activity, verificationFragment)
                            .commit()
                    }
                }
            }

            override fun onFail(exception: Exception) {
                exception.printStackTrace()

                Dialog(this@SignInFragment.requireActivity()).apply {
                    setContentView(R.layout.dialog_authorization_check)
                    findViewById<MaterialButton>(R.id.btn_authorization_failed_ok).setOnClickListener {
                        dismiss()
                    }
                    window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    show()
                }
            }

        }
        viewModel.signIn(email, password, signInListener)
    }
}
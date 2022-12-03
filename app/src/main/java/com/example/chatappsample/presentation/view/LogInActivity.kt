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
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.chatappsample.R
import com.example.chatappsample.domain.`interface`.OnSendEmailVerificationListener
import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.presentation.view.EmailVerificationActivity.Companion.E_MAIL
import com.example.chatappsample.presentation.view.MainActivity.Companion.CURRENT_USER
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LogInFragment : Fragment() {

    @Inject
    lateinit var sharedPreferenceRepo: SharedPreferenceRepository

    lateinit var fragmentLogin: View

    private val emailEditText by lazy { this.requireActivity().findViewById<TextInputEditText>(R.id.et_login_e_mail) }
    private val passwordEditText by lazy { this.requireActivity().findViewById<TextInputEditText>(R.id.et_login_password) }
    private val loginButton by lazy { this.requireActivity().findViewById<MaterialButton>(R.id.btn_login_login) }
    private val signupButton by lazy { this.requireActivity().findViewById<MaterialButton>(R.id.btn_login_sign_up) }
    private val autoLoginCheckBox by lazy { this.requireActivity().findViewById<MaterialCheckBox>(R.id.checkbox_auto_login) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        fragmentLogin = inflater.inflate(R.layout.fragment_login, container, true)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        signupButton.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        autoLoginCheckBox.isChecked = sharedPreferenceRepo.isAutoLoginChecked()

        loginButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (email.isEmpty()) {
                Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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
    }

    override fun onResume() {

        if (sharedPreferenceRepo.isAutoLoginChecked()) {
            val email = sharedPreferenceRepo.getEmailAddress()
            val password = sharedPreferenceRepo.getPassword()

            login(email, password)
            Toast.makeText(this, "자동로그인 완료", Toast.LENGTH_SHORT).show()
        }

        super.onResume()
    }

    private val onSendEmailVerificationListener = object: OnSendEmailVerificationListener {
        override fun onSuccess() {
            TODO("Not yet implemented")
        }

        override fun onSendEmailVerificationFail() {
            TODO("Not yet implemented")
        }

        override fun onStart() {
            TODO("Not yet implemented")
        }

    }

    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    if (task.result.user!!.isEmailVerified) {
                        finish()
                        // 인증된 유저는 바로 메인 액티비티로 이동
                        val mIntent = Intent(this@LogInActivity, MainActivity::class.java)
                        mIntent.putExtra(CURRENT_USER, task.result.user!!.uid)
                        startActivity(mIntent)
                    } else {
                        // 미인증 유저는 재인증 절차
                        val mIntent = Intent(this@LogInActivity, EmailVerificationActivity::class.java)
                        mIntent.putExtra(E_MAIL, email)
                        startActivity(mIntent)
                    }
                } else {
                    // If sign in fails, display a message to the user.
                    Dialog(this).apply {
                        setContentView(R.layout.dialog_authorization_check)
                        findViewById<MaterialButton>(R.id.btn_authorization_failed_ok).setOnClickListener {
                            dismiss()
                        }
                        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        show()
                    }
                }
            }
    }
}
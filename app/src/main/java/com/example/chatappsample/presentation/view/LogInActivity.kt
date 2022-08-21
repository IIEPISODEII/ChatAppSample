package com.example.chatappsample.presentation.view

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.CompoundButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatappsample.R
import com.example.chatappsample.data.repository.SharedPreferenceRepositoryImpl
import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LogInActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPreferenceRepo: SharedPreferenceRepository

    private val emailEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_login_e_mail) }
    private val passwordEditText by lazy { this.findViewById<TextInputEditText>(R.id.et_login_password) }
    private val loginButton by lazy { this.findViewById<MaterialButton>(R.id.btn_login_login) }
    private val signupButton by lazy { this.findViewById<MaterialButton>(R.id.btn_login_sign_up) }
    private val autoLoginCheckBox by lazy { this.findViewById<MaterialCheckBox>(R.id.checkbox_auto_login) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (sharedPreferenceRepo.isAutoLoginChecked()) {
            val email = sharedPreferenceRepo.getEmailAddress()
            val password = sharedPreferenceRepo.getPassword()

            login(email, password)
            Toast.makeText(this, "Auto-Logined", Toast.LENGTH_SHORT).show()

        }
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
                Toast.makeText(this, "Please input your e-mail.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                Toast.makeText(this, "Please input password.", Toast.LENGTH_SHORT).show()
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

    private fun login(email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    finish()
                    // Sign in success, update UI with the signed-in user's information
                    val intent = Intent(this@LogInActivity, MainActivity::class.java)
                    startActivity(intent)
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
package com.example.chatappsample.presentation.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.chatappsample.R
import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import com.example.chatappsample.presentation.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SplashActivity: AppCompatActivity() {

    @Inject
    lateinit var sharedPreferenceRepo: SharedPreferenceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onResume() {
        super.onResume()
        if (sharedPreferenceRepo.isAutoLoginChecked()) {
            val mEmail = sharedPreferenceRepo.getEmailAddress()
            val mPassword=  sharedPreferenceRepo.getPassword()
            FirebaseAuth.getInstance().signInWithEmailAndPassword(mEmail, mPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        UserViewModel.setCurrentUserId(task.result.user!!.uid)
                        finish()
                        val intent = Intent(this@SplashActivity, MainActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@SplashActivity, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                        val intent = Intent(this@SplashActivity, SignInActivity::class.java)
                        startActivity(intent)
                    }
                }

        } else {
            finish()
            val intent = Intent(this@SplashActivity, SignInActivity::class.java)
            startActivity(intent)
        }
    }
}
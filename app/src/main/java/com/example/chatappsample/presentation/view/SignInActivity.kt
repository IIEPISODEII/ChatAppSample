package com.example.chatappsample.presentation.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.chatappsample.R
import com.example.chatappsample.data.entity.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignInActivity: AppCompatActivity() {

    private val signInFragment by lazy { SignInFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.framelayout_signin_activity, signInFragment)
            .commit()
    }
}
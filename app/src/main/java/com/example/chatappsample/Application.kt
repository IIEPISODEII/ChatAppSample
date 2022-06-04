package com.example.chatappsample

import android.app.Application
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class Application: Application() {
    companion object {
        val mFirebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
        val mFbDatabaseRef = FirebaseDatabase.getInstance().reference
    }

    override fun onCreate() {
        super.onCreate()

    }
}
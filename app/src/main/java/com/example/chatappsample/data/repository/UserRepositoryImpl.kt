package com.example.chatappsample.data.repository

import android.annotation.SuppressLint
import com.example.chatappsample.di.IoDispatcher
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
import com.example.chatappsample.domain.dto.Message
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.util.Resource
import com.example.chatappsample.util.safeCall
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getCurrentUser(listener: OnGetDataListener) {
        val firebaseUserId = firebaseAuth.currentUser?.uid ?: "NO ID"

        firebaseDatabase.reference.child("user").child(firebaseUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener.onSuccess(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onFailure(error)
                }
            })
    }

    override fun getAllUsers(listener: OnGetDataListener) {
        listener.onStart()

        firebaseDatabase.reference.child("user")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener.onSuccess(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onFailure(error)
                }

            })
    }

    override fun signOut(): Boolean {
        firebaseAuth.signOut()

        if (firebaseAuth.currentUser == null) return true
        return false
    }

    override fun signUp(name: String, email: String, password: String, listener: OnGetRegistrationListener) {
        val db = firebaseDatabase.reference
        listener.onStart()

        firebaseAuth.createUserWithEmailAndPassword(email, password)

            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    db.child("user").child(task.result.user!!.uid)
                        .setValue(User(name, email, task.result.user!!.uid, "", Message()))

                    listener.onSuccess(task)
                } else {
                    // If sign in fails, display a message to the user.
                    listener.onFailure(task.exception)
                }
            }
    }
}
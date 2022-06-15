package com.example.chatappsample.data.repository

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.Application
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.presentation.view.LogInActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth
) : UserRepository {

    override fun getCurrentUser(): User? {
        var user: User? = null
        val firebaseUserId = firebaseAuth.currentUser?.uid ?: "NO ID"

        firebaseDatabase.reference.child("user").child(firebaseUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })

        return user
    }

    override fun getAllUsers(): ArrayList<User> {
        val userList = arrayListOf<User>()

        firebaseDatabase.reference.child("user")
            .addValueEventListener(object : ValueEventListener {
                @SuppressLint("NotifyDataSetChanged")
                override fun onDataChange(snapshot: DataSnapshot) {
                    userList.clear()

                    for (postSnapshot in snapshot.children) {
                        val currentUser = postSnapshot.getValue(User::class.java)
                        if (firebaseAuth.currentUser?.uid != currentUser?.uid) userList.add(
                            currentUser!!
                        )
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        return userList
    }

    override fun signOut() : Boolean {
        firebaseAuth.signOut()

        if (firebaseAuth.currentUser == null) return true
        return false
    }

    override fun signUp(name: String, email: String, password: String): Boolean {
        val db = firebaseDatabase.reference

        var isCreated = false
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    db.child("user").child(task.result.user!!.uid).setValue(User(name, email, task.result.user!!.uid))
                    isCreated = true
                } else {
                    // If sign in fails, display a message to the user.
                }
            }

        return isCreated
    }
}
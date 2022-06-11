package com.example.chatappsample.data.repository

import android.annotation.SuppressLint
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
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

    override fun getCurrentUser(event: () -> Unit): User? {
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
        event()

        return user
    }

    override fun getAllUsers(event: () -> Unit): ArrayList<User> {
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
                    event()
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })

        return userList
    }

    override fun signOut(event: () -> Unit) {
        firebaseAuth.signOut()
        event()
    }
}
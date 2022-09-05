package com.example.chatappsample.data.repository

import android.annotation.SuppressLint
import android.net.Uri
import com.example.chatappsample.di.IoDispatcher
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
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
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) : UserRepository {

    private val db = firebaseDatabase.reference

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

        db.child("user")
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
        listener.onStart()

        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    db.child("user")
                        .child(task.result.user!!.uid)
                        .setValue(User(name, email, task.result.user!!.uid, ""))

                    listener.onSuccess(task)
                } else {
                    // If sign in fails, display a message to the user.
                    listener.onFailure(task.exception)
                }
            }
    }

    override fun updateCurrentUser(user: User, changeProfileImage: Boolean) {

        db
            .child("user")
            .child(user.uid)
            .setValue(user)
            .addOnSuccessListener {
                if (changeProfileImage) {
                    val metadata = storageMetadata {
                        contentType = "image/jpeg"
                    }

                    firebaseStorage.reference
                        .child("profileImages/${user.uid}")
                        .putFile(Uri.parse(user.profileImage), metadata)

                }
            }
    }

    private val TEN_MEGABYTE = 10L*1024L*1024L

    override fun downloadProfileImage(userID: String, onFileDownloadListener: OnFileDownloadListener) {

        firebaseStorage.reference
            .child("profileImages/$userID")
            .getBytes(TEN_MEGABYTE)
            .addOnSuccessListener {
                onFileDownloadListener.onSuccess(it)
            }
            .addOnFailureListener {
                onFileDownloadListener.onFailure(it)
            }
    }
}
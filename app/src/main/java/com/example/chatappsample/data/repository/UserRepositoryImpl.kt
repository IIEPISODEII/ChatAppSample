package com.example.chatappsample.data.repository

import android.net.Uri
import android.util.Log
import com.example.chatappsample.data.entity.UserEntity
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
import com.example.chatappsample.domain.dto.User
import com.example.chatappsample.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storageMetadata
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val userRoomDB: AppDatabase
) : UserRepository {

    private val db = firebaseDatabase.reference

    override fun getCurrentUser(listener: OnGetDataListener) {
        val firebaseUserId = firebaseAuth.currentUser?.uid ?: "NO ID"

        db.child("user").child(firebaseUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener.onSuccess(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onFailure(error)
                }
            })
    }

    private fun insertUser(userEntity: UserEntity) = userRoomDB.getUserDao().insertUser(userEntity)

    override fun receiveAllUsersFromExternalDB(coroutineScope: CoroutineScope) {
        db
            .child("user")
            .addChildEventListener(object: ChildEventListener {
                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                    coroutineScope.launch(Dispatchers.IO) { insertUser(snapshot.getValue(User::class.java)!!.toUserEntity()) }
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    coroutineScope.launch(Dispatchers.IO) { insertUser(snapshot.getValue(User::class.java)!!.toUserEntity()) }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                    coroutineScope.launch(Dispatchers.IO) { userRoomDB.getUserDao().deleteUser(snapshot.getValue(User::class.java)!!.toUserEntity()) }
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("ERROR:", error.message)
                }
            })
    }

    override suspend fun getAllUsersFromRoomDB(): Flow<List<User>> {
        return userRoomDB
            .getUserDao()
            .getAllUserList()
            .map {
                    userEntityList -> userEntityList.map { userEntity -> userEntity.toUserDTO() }
            }
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
                        .setValue(User(name = name, uid = task.result.user!!.uid, profileImage = "", email = email))

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

        if (changeProfileImage) {

            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            firebaseStorage
                .reference
                .child("profileImages/${user.uid}")
                .putFile(Uri.parse(user.profileImage), metadata)
                .addOnCompleteListener { task ->
                    db
                        .child("user")
                        .child(user.uid)
                        .setValue(user.apply {
                            this.profileImage = task.result.uploadSessionUri.toString()
                        })
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
package com.example.chatappsample.data.repository

import android.net.Uri
import android.util.Log
import com.example.chatappsample.data.entity.UserData
import com.example.chatappsample.domain.`interface`.*
import com.example.chatappsample.domain.dto.UserDomain
import com.example.chatappsample.domain.repository.ChatroomRepository
import com.example.chatappsample.domain.repository.UserRepository
import com.example.chatappsample.util.TEN_MEGABYTE
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
    firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val roomDB: AppDatabase
) : UserRepository {

    private val db = firebaseDatabase.reference

    private var isUserFetched = false
    private var mCurrentUserFetchValueEventListener: ValueEventListener? = null

    override fun fetchCurrentUser(listener: OnGetDataListener) {
        if (isUserFetched) return

        val firebaseUserId = firebaseAuth.currentUser?.uid ?: "NO ID"

        if (mCurrentUserFetchValueEventListener != null) {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(firebaseUserId)
                .removeEventListener(mCurrentUserFetchValueEventListener!!)
        }

        isUserFetched = true
        mCurrentUserFetchValueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listener.onSuccess(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                listener.onFailure(error)
            }
        }

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .child(firebaseUserId)
            .addValueEventListener(mCurrentUserFetchValueEventListener!!)
    }

    private fun insertUser(userData: UserData) = roomDB.getUserDao().insertUser(userData)

    private var mUserListFirebaseChildEventListener: ChildEventListener? = null
    private var mUserListCoroutine: CoroutineScope? = null

    override fun fetchUserListFromExternalDB(coroutineScope: CoroutineScope) {
        if (mUserListCoroutine != null && mUserListCoroutine == coroutineScope) return

        if (mUserListFirebaseChildEventListener != null) {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .removeEventListener(mUserListFirebaseChildEventListener!!)
        }

        mUserListCoroutine = coroutineScope

        mUserListFirebaseChildEventListener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                coroutineScope.launch(Dispatchers.IO) { insertUser(snapshot.getValue(UserData::class.java)!!) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                coroutineScope.launch(Dispatchers.IO) { insertUser(snapshot.getValue(UserData::class.java)!!) }
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                coroutineScope.launch(Dispatchers.IO) { roomDB.getUserDao().deleteUser(snapshot.getValue(UserData::class.java)!!) }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("ERROR:", error.message)
            }
        }

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .addChildEventListener(mUserListFirebaseChildEventListener!!)

    }

    override suspend fun fetchUserListFromRoomDB(): Flow<List<UserDomain>> {
        return roomDB
            .getUserDao()
            .fetchUserList()
            .map { list ->
                list.map { userData -> userData.toDomain() }
            }
    }

    override fun signIn(email: String, password: String, listener: OnSignInListener) {
        firebaseAuth
            .signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                listener.onSuccess(it)
            }
            .addOnFailureListener {
                listener.onFail(it)
            }
    }

    override fun signOut(): Boolean {
        firebaseAuth.signOut()

        isUserFetched = false
        isProfileImageDownloaded = false
        mUserListCoroutine = null
        mUserListFirebaseChildEventListener = null

        ChatroomRepositoryImpl.initializeOverlapCheck()
        ChatRepositoryImpl.initializeOverlapCheck()

        if (firebaseAuth.currentUser == null) return true
        return false
    }

    override fun sendVerificationEmail(
        email: String,
        password: String,
        listener: OnSendEmailVerificationListener
    ) {
        listener.onStart()

        firebaseAuth
            .createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    firebaseAuth
                        .currentUser
                        ?.sendEmailVerification()
                        ?.addOnSuccessListener {
                            listener.onSuccess()
                        }
                        ?.addOnFailureListener {
                            listener.onSendEmailVerificationFail()
                        }
                } else {
                    listener.onSendEmailVerificationFail()
                }
            }
    }

    private var reloadCnt = 0
    override fun signUp(name: String, listener: OnEmailVerificationListener) {
        reloadCnt ++
        val currentUser = firebaseAuth.currentUser ?: return

        currentUser.reload().addOnCompleteListener {
            if (reloadCnt > 3) return@addOnCompleteListener
            if (!it.isSuccessful) signUp(name, listener)

            if (currentUser.isEmailVerified) {
                val userDomain = UserDomain(name, currentUser.email!!, currentUser.uid, "")

                db.child(FIREBASE_FIRST_CHILD_USERS)
                    .child(currentUser.uid)
                    .setValue(userDomain)
                    .addOnSuccessListener {
                        listener.onSuccess(userDomain)
                    }
            } else {
                currentUser
                    .sendEmailVerification()
                    .addOnSuccessListener {
                        listener.onFailEmailVerification()
                    }
                    .addOnFailureListener {
                        listener.onFail(it)
                    }
            }
        }

    }

    override fun updateCurrentUser(userDomain: UserDomain, changeProfileImage: Boolean) {
        val userData = UserData(
            name = userDomain.name,
            uid = userDomain.uid,
            profileImage = userDomain.profileImage,
            email = userDomain.email,
            lastTimeStamp = userDomain.lastTimeStamp
        )

//        db
//            .child(FIREBASE_FIRST_CHILD_USERS)
//            .child(userData.uid)
//            .setValue(userDomain)
//            .addOnSuccessListener {
//                if (changeProfileImage) {
//                    val metadata = storageMetadata {
//                        contentType = "image/jpeg"
//                    }
//
//                    firebaseStorage.reference
//                        .child(FIREBASE_FIRST_CHILD_PROFILEIMAGES + userDomain.uid)
//                        .putFile(Uri.parse(userDomain.profileImage), metadata)
//
//                }
//            }

        if (changeProfileImage) {

            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            firebaseStorage
                .reference
                .child(FIREBASE_FIRST_CHILD_PROFILEIMAGES + userData.uid)
                .putFile(Uri.parse(userData.profileImage), metadata)
                .addOnCompleteListener { task ->
                    db
                        .child(FIREBASE_FIRST_CHILD_USERS)
                        .child(userData.uid)
                        .setValue(userData.apply {
                            this.profileImage = task.result.uploadSessionUri.toString()
                        })
                }
        } else {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(userData.uid)
                .setValue(userData)
        }
    }

    private var isProfileImageDownloaded = false

    override fun downloadProfileImage(
        userID: String,
        onFileDownloadListener: OnFileDownloadListener
    ) {
        if (isProfileImageDownloaded) return
        isProfileImageDownloaded = true

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

    override suspend fun fetchUserById(uid: String): UserDomain {
        val userData = roomDB.getUserDao().fetchUserById(uid) ?: return UserDomain()
        return UserDomain(name = userData.name, email = userData.email, uid = userData.uid, profileImage = userData.profileImage, lastTimeStamp = userData.lastTimeStamp)
    }

    companion object {
        const val FIREBASE_FIRST_CHILD_USERS = "user"
        const val FIREBASE_FIRST_CHILD_PROFILEIMAGES = "profileImages/"
    }
}
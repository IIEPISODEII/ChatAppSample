package com.example.chatappsample.data.repository

import android.net.Uri
import android.util.Log
import com.example.chatappsample.data.entity.UserData
import com.example.chatappsample.domain.`interface`.*
import com.example.chatappsample.domain.dto.UserDomain
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
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val roomDB: AppDatabase
) : UserRepository {

    private val db = firebaseDatabase.reference

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
                coroutineScope.launch(Dispatchers.IO) { roomDB.getUserDao().insertUser(snapshot.getValue(UserData::class.java)!!) }
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                coroutineScope.launch(Dispatchers.IO) { roomDB.getUserDao().updateUser(snapshot.getValue(UserData::class.java)!!) }
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

    override suspend fun fetchUserListFromLocalDB(): Flow<List<UserDomain>> {
        return roomDB
            .getUserDao()
            .fetchUserList()
            .map { list ->
                list.map { userData -> userData.toDomain() }
            }
    }

    override fun signIn(email: String, password: String, listener: SignInListener) {
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
        listener: EmailVerificationSendListener
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
    override fun signUp(name: String, listener: EmailVerifyListener) {
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

        if (changeProfileImage) {

            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            firebaseStorage
                .reference
                .child(FIREBASE_FIRST_CHILD_PROFILEIMAGES + userDomain.uid)
                .putFile(Uri.parse(userDomain.profileImage), metadata)
                .addOnCompleteListener { task ->
                    userDomain.profileImage = task.result.uploadSessionUri.toString()
                    val userData = mapOf(
                        EMAIL to userDomain.email,
                        UID to userDomain.uid,
                        NAME to userDomain.name,
                        PROFILLE_IMAGE to userDomain.profileImage,
                        LAST_TIME_STAMP to userDomain.lastTimeStamp
                    )

                    db
                        .child(FIREBASE_FIRST_CHILD_USERS)
                        .child(userDomain.uid)
                        .updateChildren(userData)
                }
        } else {
            val userData = mapOf(
                EMAIL to userDomain.email,
                UID to userDomain.uid,
                NAME to userDomain.name,
                PROFILLE_IMAGE to userDomain.profileImage,
                LAST_TIME_STAMP to userDomain.lastTimeStamp
            )

            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(userDomain.uid)
                .updateChildren(userData)
        }
    }

    override fun downloadProfileImage(
        userID: String,
        fileDownloadListener: FileDownloadListener
    ) {
        firebaseStorage.reference
            .child("profileImages/$userID")
            .getBytes(TEN_MEGABYTE)
            .addOnSuccessListener {
                fileDownloadListener.onSuccess(it)
            }
            .addOnFailureListener {
                fileDownloadListener.onFail(it)
            }
    }

    override suspend fun fetchUserById(uid: String): UserDomain {
        val userData = roomDB.getUserDao().fetchUserById(uid) ?: return UserDomain()
        return UserDomain(name = userData.name, email = userData.email, uid = userData.uid, profileImage = userData.profileImage, lastTimeStamp = userData.lastTimeStamp)
    }

    override fun fetchUserByIdAsFlow(uid: String): Flow<UserDomain> {
        return roomDB.getUserDao().fetchUserByIdAsFlow(uid)
            .map {
                val userData = it ?: UserData()
                UserDomain(userData.name, userData.email, userData.uid, userData.profileImage, userData.lastTimeStamp)
            }
    }

    companion object {
        const val FIREBASE_FIRST_CHILD_USERS = "user"
        const val FIREBASE_FIRST_CHILD_PROFILEIMAGES = "profileImages/"
        const val EMAIL = "email"
        const val LAST_TIME_STAMP = "lastTimeStamp"
        const val NAME = "name"
        const val PROFILLE_IMAGE = "profileImage"
        const val UID = "uid"
    }
}
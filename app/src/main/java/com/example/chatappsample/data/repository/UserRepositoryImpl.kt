package com.example.chatappsample.data.repository

import android.net.Uri
import android.util.Log
import com.example.chatappsample.data.entity.ChatRoomData
import com.example.chatappsample.data.entity.UserData
import com.example.chatappsample.data.repository.ChatRepositoryImpl.Companion.FIREBASE_FIRST_CHILD_CHATS
import com.example.chatappsample.data.repository.ChatRepositoryImpl.Companion.FIREBASE_SECOND_CHILD_MESSAGES
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
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
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    private val firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val roomDB: AppDatabase
) : UserRepository {

    private val db = firebaseDatabase.reference

    override fun getCurrentUser(listener: OnGetDataListener) {
        val firebaseUserId = firebaseAuth.currentUser?.uid ?: "NO ID"

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .child(firebaseUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    listener.onSuccess(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    listener.onFailure(error)
                }
            })
    }

    private fun insertUser(userData: UserData) = roomDB.getUserDao().insertUser(userData)

    override fun receiveAllUsersFromExternalDB(coroutineScope: CoroutineScope) {
        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .addChildEventListener(object: ChildEventListener {
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
            })
    }

    override suspend fun getAllUsersFromRoomDB(): Flow<List<UserDomain>> {
        return roomDB
            .getUserDao()
            .getAllUserList()
            .map { list ->
                list.map { userData -> userData.toDomain() }
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
                    db.child(FIREBASE_FIRST_CHILD_USERS)
                        .child(task.result.user!!.uid)
                        .setValue(UserDomain(name = name, uid = task.result.user!!.uid, profileImage = "", email = email))

                    listener.onSuccess(task)
                } else {
                    // If sign in fails, display a message to the user.
                    listener.onFailure(task.exception)
                }
            }
    }

    override fun updateCurrentUser(userDomain: UserDomain, changeProfileImage: Boolean) {
        val userData = UserData(
            name = userDomain.name,
            uid = userDomain.uid,
            profileImage = userDomain.profileImage,
            email = userDomain.email
        )

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .child(userData.uid)
            .setValue(userDomain)
            .addOnSuccessListener {
                if (changeProfileImage) {
                    val metadata = storageMetadata {
                        contentType = "image/jpeg"
                    }

                    firebaseStorage.reference
                        .child(FIREBASE_FIRST_CHILD_PROFILEIMAGES+userDomain.uid)
                        .putFile(Uri.parse(userDomain.profileImage), metadata)

                }
            }

        if (changeProfileImage) {

            val metadata = storageMetadata {
                contentType = "image/jpeg"
            }

            firebaseStorage
                .reference
                .child(FIREBASE_FIRST_CHILD_PROFILEIMAGES+userDomain.uid)
                .putFile(Uri.parse(userDomain.profileImage), metadata)
                .addOnCompleteListener { task ->
                    db
                        .child(FIREBASE_FIRST_CHILD_USERS)
                        .child(userDomain.uid)
                        .setValue(userDomain.apply {
                            this.profileImage = task.result.uploadSessionUri.toString()
                        })
                }
        }
    }


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

    override fun updateChatRoomState(
        myId: String,
        yourId: String,
        time: String,
        onSuccess: (String) -> (Unit),
        onFail: () -> (Unit),
        enter: Boolean,
        coroutineScope: CoroutineScope
    ) {
        val randomChatRoomId = time + UUID.randomUUID()

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .child(myId)
            .child(FIREBASE_SECOND_CHILD_CHATROOMS)
            .child(yourId)
            .addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        db
                            .child(FIREBASE_FIRST_CHILD_USERS)
                            .child(myId)
                            .child(FIREBASE_SECOND_CHILD_CHATROOMS)
                            .updateChildren(mapOf(Pair(yourId, randomChatRoomId)))
                            .addOnSuccessListener {
                                db
                                    .child(FIREBASE_FIRST_CHILD_USERS)
                                    .child(yourId)
                                    .child(FIREBASE_SECOND_CHILD_CHATROOMS)
                                    .updateChildren(mapOf(Pair(myId, randomChatRoomId)))
                                db
                                    .child(FIREBASE_FIRST_CHILD_CHATS)
                                    .child(randomChatRoomId)
                                    .child(FIREBASE_SECOND_CHILD_READ_LOG)
                                    .updateChildren(mapOf(Pair(myId, time), Pair(yourId, "")))
                                onSuccess(randomChatRoomId)
                                coroutineScope.launch(Dispatchers.IO) {
                                    roomDB.getChatRoomDao().insertChatRoom(chatRoom = ChatRoomData(chatRoomId = randomChatRoomId, participantsId = myId, participationTime = time))
                                }
                            }
                    } else {
                        val currentChatRoomId = snapshot.value.toString()
                        val insertTime = if (enter) "9" else time

                        db
                            .child(FIREBASE_FIRST_CHILD_CHATS)
                            .child(currentChatRoomId)
                            .child(FIREBASE_SECOND_CHILD_READ_LOG)
                            .updateChildren(mapOf(Pair(myId, insertTime)))
                            .addOnSuccessListener {
                                coroutineScope.launch(Dispatchers.IO) {
                                    roomDB.getChatRoomDao().insertChatRoom(chatRoom = ChatRoomData(chatRoomId = currentChatRoomId, participantsId = myId, participationTime = insertTime))
                                    println("getChatRoomDao: ${ChatRoomData(currentChatRoomId, myId, insertTime)}")
                                }
                                onSuccess(currentChatRoomId)
                            }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    onFail()
                }

            })
    }

    private data class ChatRoomId(var chatRoomId: String = "")

    companion object {
        const val FIREBASE_FIRST_CHILD_USERS = "user"
        const val FIREBASE_FIRST_CHILD_PROFILEIMAGES = "profileImages/"
        const val FIREBASE_SECOND_CHILD_READ_LOG = "readLog"
        const val FIREBASE_SECOND_CHILD_CHATROOMS = "chatrooms"
    }
}
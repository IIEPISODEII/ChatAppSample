package com.example.chatappsample.data.repository

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.chatappsample.data.entity.ChatRoomData
import com.example.chatappsample.data.entity.ReaderLogData
import com.example.chatappsample.data.entity.UserData
import com.example.chatappsample.data.repository.ChatRepositoryImpl.Companion.FIREBASE_FIRST_CHILD_CHATS
import com.example.chatappsample.data.repository.worker.UpdateChatroomWorker
import com.example.chatappsample.domain.`interface`.*
import com.example.chatappsample.domain.dto.ChatRoomDomain
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
import kotlinx.coroutines.flow.forEach
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(
    firebaseDatabase: FirebaseDatabase,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage,
    private val roomDB: AppDatabase,
    private val workManager: WorkManager
) : UserRepository {

    private val db = firebaseDatabase.reference

    override fun fetchCurrentUser(listener: OnGetDataListener) {
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

    override fun fetchUserListFromExternalDB(coroutineScope: CoroutineScope) {
        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .addChildEventListener(object : ChildEventListener {
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

    override suspend fun fetchUserListFromRoomDB(): Flow<List<UserDomain>> {
        return roomDB
            .getUserDao()
            .getAllUserList()
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
            println("currentUser: ${currentUser.uid}, ${currentUser.email}, ${currentUser.isEmailVerified}")

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


    override fun downloadProfileImage(
        userID: String,
        onFileDownloadListener: OnFileDownloadListener
    ) {

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(myId)
                .child(FIREBASE_SECOND_CHILD_CHATROOMS)
                .child(yourId)
                .addValueEventListener(object : ValueEventListener {
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
                                        .updateChildren(
                                            mapOf(
                                                Pair(myId, time),
                                                Pair(yourId, "")
                                            )
                                        )

                                    val pWorker = object : UpdateChatroomWorker.Work {
                                        override fun doWork() {
                                            val myChatRoomData = ChatRoomData(myId, randomChatRoomId, ReaderLogData(randomChatRoomId, myId, "9"))
                                            val yourChatRoomData = ChatRoomData(yourId, randomChatRoomId, ReaderLogData(randomChatRoomId, yourId, ""))

                                            roomDB.getChatRoomDao().insertChatRoom(myChatRoomData)
                                            roomDB.getChatRoomDao().insertChatRoom(yourChatRoomData)
                                        }
                                    }
                                    UpdateChatroomWorker.setWork(pWorker)
                                    val updateChatroomWorker = OneTimeWorkRequest
                                        .Builder(UpdateChatroomWorker::class.java)
                                        .build()

                                    workManager
                                        .beginWith(updateChatroomWorker)
                                        .enqueue()

                                    onSuccess(randomChatRoomId)
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

                                    val pWorker = object : UpdateChatroomWorker.Work {
                                        override fun doWork() {
                                            val myChatRoomData = ChatRoomData(myId, currentChatRoomId, ReaderLogData(currentChatRoomId, myId, insertTime))
                                            roomDB.getChatRoomDao().insertChatRoom(myChatRoomData)
                                        }
                                    }

                                    UpdateChatroomWorker.setWork(pWorker)
                                    val updateChatroomWorker = OneTimeWorkRequest
                                        .Builder(UpdateChatroomWorker::class.java)
                                        .build()

                                    workManager
                                        .beginWith(updateChatroomWorker)
                                        .enqueue()

                                    onSuccess(currentChatRoomId)
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        onFail()
                    }

                })


        } else {
            Log.d("UserRepoImpl", "데이터 동기화 실패 - 원인: API Level 미달")
        }
    }

    override suspend fun fetchChatRoomList(
        currentUserId: String
    ): Flow<List<ChatRoomDomain>> {
        val chatRoomDataList = roomDB.getChatRoomDao().getChatRoomList(currentUserId)

        return chatRoomDataList.map { chatRoomList ->
            val chatRoomDomainList = mutableListOf<ChatRoomDomain>()
            chatRoomList.forEach { chatRoomData ->
                val readerLogItem = ChatRoomDomain.ReaderLogDomain(
                    chatRoomData.readerLog.userId,
                    chatRoomData.readerLog.readTime
                )

                val chatRoomDomain = ChatRoomDomain(chatRoomData.chatRoomId, listOf(readerLogItem))
                chatRoomDomainList.add(chatRoomDomain)
            }
            chatRoomDomainList
        }
    }

    companion object {
        const val FIREBASE_FIRST_CHILD_USERS = "user"
        const val FIREBASE_FIRST_CHILD_PROFILEIMAGES = "profileImages/"
        const val FIREBASE_SECOND_CHILD_READ_LOG = "readLog"
        const val FIREBASE_SECOND_CHILD_CHATROOMS = "chatrooms"
    }
}
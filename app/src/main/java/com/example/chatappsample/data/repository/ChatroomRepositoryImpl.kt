package com.example.chatappsample.data.repository

import android.os.Build
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.example.chatappsample.data.entity.ChatroomData
import com.example.chatappsample.data.entity.ReaderLogData
import com.example.chatappsample.data.repository.ChatRepositoryImpl.Companion.FIREBASE_FIRST_CHILD_CHATS
import com.example.chatappsample.data.repository.worker.UpdateChatroomWorker
import com.example.chatappsample.domain.dto.ChatroomDomain
import com.example.chatappsample.domain.repository.ChatroomRepository
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.database.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.*
import javax.inject.Inject

class ChatroomRepositoryImpl @Inject constructor(
    firebaseDatabase: FirebaseDatabase,
    private val roomDB: AppDatabase,
    private val workManager: WorkManager
) : ChatroomRepository {

    private val db = firebaseDatabase.reference


    override fun updateChatroomState(
        myId: String,
        yourId: String,
        time: String,
        onSuccess: (String) -> (Unit),
        onFail: () -> (Unit),
        enter: Boolean,
        coroutineScope: CoroutineScope
    ) {
        if (myId == "") {
            throw Exception("내 아이디 어디감")
        }
        if (yourId == "") {
            throw Exception("상대 아이디 어디감")
        }

        if (mChatroomStateCoroutineScope != null && mChatroomStateCoroutineScope != coroutineScope) return

        if (mChatroomStateMyValueEventListener != null) {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(myId)
                .child(FIREBASE_SECOND_CHILD_CHATROOMS)
                .child(yourId)
                .removeEventListener(mChatroomStateMyValueEventListener!!)
        }

        val randomChatRoomId = time + UUID.randomUUID()

        mChatroomStateMyValueEventListener = object: ValueEventListener {
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
                                    val myChatroomData = ChatroomData(myId, randomChatRoomId)
                                    val yourChatroomData = ChatroomData(yourId, randomChatRoomId)

                                    val myReaderLogData = ReaderLogData(randomChatRoomId, myId, "9")
                                    val yourReaderLogData = ReaderLogData(randomChatRoomId, yourId, "")

                                    roomDB.apply {
                                        getChatroomDataDao().insertChatRoom(myChatroomData)
                                        getChatroomDataDao().insertChatRoom(yourChatroomData)
                                        getReaderLogDataDao().insertReaderLog(myReaderLogData)
                                        getReaderLogDataDao().insertReaderLog(yourReaderLogData)
                                    }
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

                    mChatroomStateYourEventSuccessListener = OnSuccessListener<Void> {
                        val pWorker = object : UpdateChatroomWorker.Work {
                            override fun doWork() {
                                val myChatroomData = ChatroomData(myId, currentChatRoomId)
                                val myReaderLogData = ReaderLogData(currentChatRoomId, myId, myId, insertTime)
                                roomDB.apply {
                                    getChatroomDataDao().insertChatRoom(myChatroomData)
                                    getReaderLogDataDao().insertReaderLog(myReaderLogData)
                                }
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

                    db
                        .child(FIREBASE_FIRST_CHILD_CHATS)
                        .child(currentChatRoomId)
                        .child(FIREBASE_SECOND_CHILD_READ_LOG)
                        .updateChildren(mapOf(Pair(myId, insertTime)))
                        .addOnSuccessListener(mChatroomStateYourEventSuccessListener!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFail()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(myId)
                .child(FIREBASE_SECOND_CHILD_CHATROOMS)
                .child(yourId)
                .addValueEventListener(mChatroomStateMyValueEventListener!!)


        } else {
            Log.d("ChatroomRepoImpl", "데이터 동기화 실패 - 원인: API Level 미달")
        }
    }


    override fun fetchChatroomListFromExternalDB(currentUserId: String, coroutineScope: CoroutineScope) {
        if (mChatroomListCoroutineScope != null && mChatroomListCoroutineScope == coroutineScope) return

        if (mChatroomListFirebaseValueEventListener != null) {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(currentUserId)
                .child(FIREBASE_SECOND_CHILD_CHATROOMS)
                .removeEventListener(mChatroomListFirebaseValueEventListener!!)
        }

        mChatroomListCoroutineScope = coroutineScope

        mChatroomListFirebaseValueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val chatroomId = it.value.toString()
                    db
                        .child(FIREBASE_FIRST_CHILD_CHATS)
                        .child(chatroomId)
                        .child(FIREBASE_SECOND_CHILD_READ_LOG)
                        .addChildEventListener(object: ChildEventListener {
                            override fun onChildAdded(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                val chatroomUserId = snapshot.key.toString()
                                val chatroomUserReadTime = snapshot.value.toString()

                                val readerLogData = ReaderLogData(
                                    chatroomId = chatroomId,
                                    currentAccountId = currentUserId,
                                    userId = chatroomUserId,
                                    readTime = chatroomUserReadTime
                                )
                                mChatroomListCoroutineScope!!.launch(Dispatchers.IO) {
                                    roomDB.getReaderLogDataDao().insertReaderLog(readerLogData)
                                }
                            }

                            override fun onChildChanged(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {
                                val chatroomUserId = snapshot.key.toString()
                                val chatroomUserReadTime = snapshot.value.toString()

                                val readerLogData = ReaderLogData(
                                    chatroomId = chatroomId,
                                    currentAccountId = currentUserId,
                                    userId = chatroomUserId,
                                    readTime = chatroomUserReadTime
                                )
                                mChatroomListCoroutineScope!!.launch(Dispatchers.IO) {
                                    roomDB.getReaderLogDataDao().insertReaderLog(readerLogData)
                                }
                            }

                            override fun onChildRemoved(snapshot: DataSnapshot) {}

                            override fun onChildMoved(
                                snapshot: DataSnapshot,
                                previousChildName: String?
                            ) {}

                            override fun onCancelled(error: DatabaseError) {}

                        })
                    mChatroomListCoroutineScope!!.launch(Dispatchers.IO) {
                        roomDB.getChatroomDataDao().insertChatRoom(ChatroomData(currentUserId, chatroomId))
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .child(currentUserId)
            .child(FIREBASE_SECOND_CHILD_CHATROOMS)
            .addValueEventListener(mChatroomListFirebaseValueEventListener!!)
    }

    override suspend fun fetchChatroomListFromRoom(
        currentUserId: String
    ): Flow<List<ChatroomDomain>> {

        return roomDB.getChatroomDataDao().fetchChatroomList(currentUserId)
            .map {
                it.map { chatroomData ->
                    ChatroomDomain(chatroomData.chatroomId, "", mutableListOf())
                }
            }
    }

    override suspend fun fetchReaderLogFromRoom(chatroomId: String): List<ChatroomDomain.ReaderLogDomain> {
        val readerLogDomainList = roomDB.getReaderLogDataDao().fetchReaderLogList(chatroomId)
            .map {
                ChatroomDomain.ReaderLogDomain(userId = it.userId, readTime = it.readTime)
            }
        return readerLogDomainList
    }

    companion object {
        const val FIREBASE_FIRST_CHILD_USERS = "user"
        const val FIREBASE_SECOND_CHILD_READ_LOG = "readLog"
        const val FIREBASE_SECOND_CHILD_CHATROOMS = "chatrooms"

        private var mChatroomStateCoroutineScope: CoroutineScope? = null
        private var mChatroomStateMyValueEventListener: ValueEventListener? = null
        private var mChatroomStateYourEventSuccessListener: OnSuccessListener<Void>? = null

        private var mChatroomListFirebaseValueEventListener: ValueEventListener? = null
        private var mChatroomListCoroutineScope: CoroutineScope? = null

        fun initializeOverlapCheck() {
            mChatroomStateCoroutineScope = null
            mChatroomStateMyValueEventListener = null
            mChatroomStateYourEventSuccessListener = null

            mChatroomListFirebaseValueEventListener = null
            mChatroomListCoroutineScope = null
        }
    }
}
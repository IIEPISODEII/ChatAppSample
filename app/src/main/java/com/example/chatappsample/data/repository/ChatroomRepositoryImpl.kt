package com.example.chatappsample.data.repository

import android.os.Build
import android.util.Log
import com.example.chatappsample.data.entity.ChatroomData
import com.example.chatappsample.data.entity.ReaderLogData
import com.example.chatappsample.data.repository.ChatRepositoryImpl.Companion.FIREBASE_FIRST_CHILD_CHATS
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
    private val roomDB: AppDatabase
) : ChatroomRepository {

    private val db = firebaseDatabase.reference


    override fun updateChatroomState(
        myId: String,
        yourId: String,
        time: String,
        onSuccess: (String) -> (Unit),
        onFail: () -> (Unit),
        enter: Boolean
    ) {
        if (myId == "") {
            throw Exception("내 아이디 어디감")
        }
        if (yourId == "") {
            throw Exception("상대 아이디 어디감")
        }

        if (mChatroomStateCoroutineScope != null) return

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

                            mChatroomStateOnSuccessListener = OnSuccessListener<Void> {
                                onSuccess(randomChatRoomId)
                            }

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
                                .addOnSuccessListener(mChatroomStateOnSuccessListener!!)
                        }
                } else {
                    val currentChatRoomId = snapshot.value.toString()
                    val insertTime = if (enter) "9" else time

                    mChatroomStateOnSuccessListener = OnSuccessListener<Void> {
                        onSuccess(currentChatRoomId)
                    }

                    db
                        .child(FIREBASE_FIRST_CHILD_CHATS)
                        .child(currentChatRoomId)
                        .child(FIREBASE_SECOND_CHILD_READ_LOG)
                        .updateChildren(mapOf(Pair(myId, insertTime)))
                        .addOnSuccessListener(mChatroomStateOnSuccessListener!!)
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

    override suspend fun fetchChatroomListFromRoom(currentUserId: String): Flow<List<ChatroomDomain>> {

        return roomDB.getChatroomDataDao().fetchChatroomList(currentUserId)
            .map {
                it.map { chatroomData ->
                    ChatroomDomain(chatroomData.chatroomId, "", mutableListOf())
                }
            }
    }

    override fun fetchReaderLogFromExternalDB(chatroomId: String, currentUserId: String, coroutineScope: CoroutineScope) {
        if (mReaderLogValueEventListener != null && mReaderLogCoroutineScope == coroutineScope) return

        if (mReaderLogValueEventListener != null) {
            db
                .child(FIREBASE_FIRST_CHILD_CHATS)
                .child(chatroomId)
                .child(FIREBASE_SECOND_CHILD_READ_LOG)
                .removeEventListener(mReaderLogValueEventListener!!)
        }

        mReaderLogValueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                snapshot.children.forEach {
                    val readerLogData = ReaderLogData(chatroomId, currentUserId, it.key!!, it.value!! as String)
                    coroutineScope.launch(Dispatchers.IO) {
                        roomDB.getReaderLogDataDao().insertReaderLog(readerLogData)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }

        }

        db
            .child(FIREBASE_FIRST_CHILD_CHATS)
            .child(chatroomId)
            .child(FIREBASE_SECOND_CHILD_READ_LOG)
            .addValueEventListener(mReaderLogValueEventListener!!)
    }

    override suspend fun fetchReaderLogFromRoom(chatroomId: String): List<ChatroomDomain.ReaderLogDomain> {
        val readerLogDomainList = roomDB.getReaderLogDataDao().fetchReaderLogList(chatroomId)
            .map {
                ChatroomDomain.ReaderLogDomain(userId = it.userId, readTime = it.readTime)
            }
        return readerLogDomainList
    }

    override suspend fun fetchReaderLogFromRoomAsFlow(chatroomId: String): Flow<List<ChatroomDomain.ReaderLogDomain>> {
        val readerLogDomainList = roomDB.getReaderLogDataDao().fetchReaderLogListAsFlow(chatroomId)
            .map {
                val readerLogDataList = mutableListOf<ChatroomDomain.ReaderLogDomain>()
                it.forEach {
                    val readerLogDomain = ChatroomDomain.ReaderLogDomain(it.userId, it.readTime)
                    readerLogDataList.add(readerLogDomain)
                }
                readerLogDataList
            }
        return readerLogDomainList
    }

    companion object {
        const val FIREBASE_FIRST_CHILD_USERS = "user"
        const val FIREBASE_SECOND_CHILD_READ_LOG = "readLog"
        const val FIREBASE_SECOND_CHILD_CHATROOMS = "chatrooms"

        private var mChatroomStateCoroutineScope: CoroutineScope? = null
        private var mChatroomStateMyValueEventListener: ValueEventListener? = null
        private var mChatroomStateOnSuccessListener: OnSuccessListener<Void>? = null

        private var mReaderLogCoroutineScope: CoroutineScope? = null
        private var mReaderLogValueEventListener: ValueEventListener? = null

        private var mChatroomListFirebaseValueEventListener: ValueEventListener? = null
        private var mChatroomListCoroutineScope: CoroutineScope? = null

        fun initializeOverlapCheck() {
            mChatroomStateCoroutineScope = null
            mChatroomStateMyValueEventListener = null
            mChatroomStateOnSuccessListener = null

            mReaderLogCoroutineScope = null
            mReaderLogValueEventListener = null

            mChatroomListFirebaseValueEventListener = null
            mChatroomListCoroutineScope = null
        }
    }
}
package com.example.chatappsample.data.repository

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

        val randomChatRoomId = time + UUID.randomUUID()

        mChatroomStateMyValueEventListener = object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    val chatroomData: MutableMap<String, Any> = hashMapOf(
                        "$FIREBASE_FIRST_CHILD_USERS/$yourId/$FIREBASE_SECOND_CHILD_CHATROOMS/$myId/" to randomChatRoomId,
                        "$FIREBASE_FIRST_CHILD_USERS/$myId/$FIREBASE_SECOND_CHILD_CHATROOMS/$yourId/" to randomChatRoomId,
                        "$FIREBASE_FIRST_CHILD_CHATS/$randomChatRoomId/$FIREBASE_SECOND_CHILD_READ_LOG" to mapOf(myId to time, yourId to "")
                    )

                    db.updateChildren(chatroomData)
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
                        .updateChildren(mapOf(myId to insertTime))
                        .addOnSuccessListener(mChatroomStateOnSuccessListener!!)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFail()
            }
        }

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .child(myId)
            .child(FIREBASE_SECOND_CHILD_CHATROOMS)
            .child(yourId)
            .addListenerForSingleValueEvent(mChatroomStateMyValueEventListener!!)
    }

    override fun fetchChatroomListFromRemoteDB(currentUserId: String, coroutineScope: CoroutineScope) {
        if (mChatroomListCoroutineScope != null && mChatroomListCoroutineScope == coroutineScope) return

        if (mChatroomListFirebaseChildEventListener != null) {
            db
                .child(FIREBASE_FIRST_CHILD_USERS)
                .child(currentUserId)
                .child(FIREBASE_SECOND_CHILD_CHATROOMS)
                .removeEventListener(mChatroomListFirebaseChildEventListener!!)
        }

        mChatroomListCoroutineScope = coroutineScope

        mChatroomListFirebaseChildEventListener = object: ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                val chatroomId = snapshot.value as String
                db
                    .child(FIREBASE_FIRST_CHILD_CHATS)
                    .child(chatroomId)
                    .child(FIREBASE_SECOND_CHILD_READ_LOG)
                    .addListenerForSingleValueEvent(object: ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val chatroomData = ChatroomData(currentUserId, chatroomId)
                            val readerLogList = mutableListOf<ReaderLogData>()

                            snapshot.children.forEach {
                                val readerLogData = ReaderLogData(
                                    chatroomId = chatroomId,
                                    currentAccountId = currentUserId,
                                    userId = it.key!!,
                                    readTime = it.value as String
                                )
                                readerLogList.add(readerLogData)
                            }

                            mChatroomListCoroutineScope!!.launch(Dispatchers.IO) {
                                roomDB.getChatroomDataDao().insertChatRoom(chatroomData)
                                roomDB.getReaderLogDataDao().insertReaderLogList(readerLogList)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {

                        }

                    })
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                val chatroomId = snapshot.value as String
                val chatroomData = ChatroomData(currentUserId, chatroomId)
                mChatroomListCoroutineScope!!.launch(Dispatchers.IO) {
                    roomDB.getChatroomDataDao().deleteChatRoom(chatroomData)
                }
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onCancelled(error: DatabaseError) {}
        }

        db
            .child(FIREBASE_FIRST_CHILD_USERS)
            .child(currentUserId)
            .child(FIREBASE_SECOND_CHILD_CHATROOMS)
            .addChildEventListener(mChatroomListFirebaseChildEventListener!!)
    }

    override suspend fun fetchChatroomListFromLocalDB(currentUserId: String): Flow<List<ChatroomDomain>> {

        return roomDB.getChatroomDataDao().fetchChatroomList(currentUserId)
            .map {
                it.map { chatroomData ->
                    ChatroomDomain(chatroomData.chatroomId, "", mutableListOf())
                }
            }
    }

    override fun fetchReaderLogFromRemoteDB(chatroomId: String, currentUserId: String, coroutineScope: CoroutineScope) {
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
                val readerLogList = mutableListOf<ReaderLogData>()
                snapshot.children.forEach {
                    val readerLogData = ReaderLogData(chatroomId, currentUserId, it.key!!, it.value!! as String)
                    readerLogList.add(readerLogData)
                }
                coroutineScope.launch(Dispatchers.IO) {
                    roomDB.getReaderLogDataDao().insertReaderLogList(readerLogList)
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

    override suspend fun fetchReaderLogFromLocalDB(chatroomId: String): List<ChatroomDomain.ReaderLogDomain> {
        val readerLogDomainList = roomDB.getReaderLogDataDao().fetchReaderLogList(chatroomId)
            .map {
                ChatroomDomain.ReaderLogDomain(userId = it.userId, readTime = it.readTime)
            }
        return readerLogDomainList
    }

    override suspend fun fetchReaderLogFromLocalDBAsFlow(chatroomId: String): Flow<List<ChatroomDomain.ReaderLogDomain>> {
        val readerLogDomainList = roomDB.getReaderLogDataDao().fetchReaderLogListAsFlow(chatroomId)
            .map {
                val readerLogDataList = mutableListOf<ChatroomDomain.ReaderLogDomain>()
                it.forEach { readerLog ->
                    val readerLogDomain = ChatroomDomain.ReaderLogDomain(readerLog.userId, readerLog.readTime)
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

        private var mChatroomListFirebaseChildEventListener: ChildEventListener? = null
        private var mChatroomListCoroutineScope: CoroutineScope? = null

        fun initializeOverlapCheck() {
            mChatroomStateCoroutineScope = null
            mChatroomStateMyValueEventListener = null
            mChatroomStateOnSuccessListener = null

            mReaderLogCoroutineScope = null
            mReaderLogValueEventListener = null

            mChatroomListFirebaseChildEventListener = null
            mChatroomListCoroutineScope = null
        }
    }
}
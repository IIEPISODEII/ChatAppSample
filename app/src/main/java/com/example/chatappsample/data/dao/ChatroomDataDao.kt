package com.example.chatappsample.data.dao

import androidx.room.*
import com.example.chatappsample.data.entity.ChatroomData
import com.example.chatappsample.data.entity.ReaderLogData
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatroomDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatRoom(chatRoom: ChatroomData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReaderLog(readerLogData: ReaderLogData)

    @Query("SELECT * FROM readerlog WHERE chatroomId = :targetChatroom")
    fun fetchReaderLogList(targetChatroom: String): List<ReaderLogData>

    @Delete
    fun deleteChatRoom(chatRoom: ChatroomData)

    @Query("SELECT * FROM chatrooms WHERE currentAccountId = :currentUserId")
    fun fetchChatroomList(currentUserId: String): Flow<List<ChatroomData>>
}
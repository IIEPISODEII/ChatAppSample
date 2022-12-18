package com.example.chatappsample.data.dao

import androidx.room.*
import com.example.chatappsample.data.entity.ChatRoomData
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatRoom(chatRoom: ChatRoomData)

    @Query("SELECT * FROM chatrooms WHERE chatRoomId = :targetChatRoom")
    fun fetchReaderLogs(targetChatRoom: String): Flow<List<ChatRoomData>>

    @Delete
    fun deleteChatRoom(chatRoom: ChatRoomData)

    @Query("SELECT * FROM chatrooms WHERE currentAccountId = :currentUserId")
    fun getChatRoomList(currentUserId: String): Flow<List<ChatRoomData>>
}
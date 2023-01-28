package com.example.chatappsample.data.dao

import androidx.room.*
import com.example.chatappsample.data.entity.ChatroomData
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatroomDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChatRoom(chatRoom: ChatroomData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertChatRoomList(chatRoomList: List<ChatroomData>)

    @Delete
    fun deleteChatRoom(chatRoom: ChatroomData)

    @Query("SELECT * FROM chatrooms WHERE currentUserId = :currentUserId")
    fun fetchChatroomList(currentUserId: String): Flow<List<ChatroomData>>

    @Query("SELECT * FROM chatrooms WHERE currentUserId = :currentUserId")
    fun fetchChatroomList2(currentUserId: String): List<ChatroomData>
}
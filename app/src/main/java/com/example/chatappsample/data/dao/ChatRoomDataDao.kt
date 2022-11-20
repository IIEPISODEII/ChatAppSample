package com.example.chatappsample.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chatappsample.data.entity.ChatRoomData
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatRoomDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertChatRoom(chatRoom: ChatRoomData)

    @Query("SELECT * FROM chatrooms WHERE chatRoomId = :targetChatRoom")
    fun fetchReaderLogs(targetChatRoom: String): Flow<List<ChatRoomData>>
}
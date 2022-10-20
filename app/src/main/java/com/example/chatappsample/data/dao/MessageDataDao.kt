package com.example.chatappsample.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.chatappsample.data.entity.MessageEntity
import com.example.chatappsample.domain.dto.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface MessageDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMessage(message: MessageEntity)

    @Query("SELECT * FROM messages WHERE chatRoom = :chatRoom ORDER BY sentTime LIMIT :limit")
    fun fetchRecentlyReceivedMessagesAsFlow(chatRoom: String, limit: Int): Flow<List<MessageEntity>>

    @Query("SELECT * FROM messages WHERE chatRoom = :chatRoom ORDER BY sentTime DESC LIMIT 1")
    fun fetchLastReceivedMessages(chatRoom: String): Flow<MessageEntity>

    @Query("SELECT * FROM (SELECT * FROM messages WHERE chatRoom = :chatRoom ORDER BY sentTime DESC LIMIT :limit OFFSET :offset) ORDER BY sentTime")
    fun fetchRecentlyReceivedMessages(chatRoom: String, limit: Int, offset: Int): List<MessageEntity>
}
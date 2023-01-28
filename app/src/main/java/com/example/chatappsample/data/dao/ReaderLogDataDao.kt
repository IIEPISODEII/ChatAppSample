package com.example.chatappsample.data.dao

import androidx.room.*
import com.example.chatappsample.data.entity.ChatroomData
import com.example.chatappsample.data.entity.ReaderLogData
import kotlinx.coroutines.flow.Flow

@Dao
interface ReaderLogDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReaderLog(readerLogData: ReaderLogData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertReaderLogList(readerLogDataList: List<ReaderLogData>)

    @Query("SELECT * FROM readerlog WHERE chatroomId = :targetChatroom")
    fun fetchReaderLogList(targetChatroom: String): List<ReaderLogData>

    @Query("SELECT * FROM readerlog WHERE chatroomId = :targetChatroom")
    fun fetchReaderLogListAsFlow(targetChatroom: String): Flow<List<ReaderLogData>>
}
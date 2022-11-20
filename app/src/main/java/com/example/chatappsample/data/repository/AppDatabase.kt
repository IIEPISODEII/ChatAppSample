package com.example.chatappsample.data.repository

import com.example.chatappsample.data.dao.ChatRoomDataDao
import android.content.Context
import androidx.room.*
import com.example.chatappsample.data.dao.MessageDataDao
import com.example.chatappsample.data.dao.UserDataDao
import com.example.chatappsample.data.entity.ChatRoomData
import com.example.chatappsample.data.entity.MessageData
import com.example.chatappsample.data.entity.UserData

@Database(entities = [MessageData::class, UserData::class, ChatRoomData::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getUserDao(): UserDataDao

    abstract fun getMessageDao(): MessageDataDao

    abstract fun getChatRoomDao(): ChatRoomDataDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room
                .databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .build()
        }

        private const val DATABASE_NAME = "com.example.chatappsample"
    }
}
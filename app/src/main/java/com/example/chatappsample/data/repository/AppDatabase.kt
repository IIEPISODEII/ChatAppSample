package com.example.chatappsample.data.repository

import com.example.chatappsample.data.dao.ChatroomDataDao
import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chatappsample.data.dao.MessageDataDao
import com.example.chatappsample.data.dao.ReaderLogDataDao
import com.example.chatappsample.data.dao.UserDataDao
import com.example.chatappsample.data.entity.*

@Database(entities = [MessageData::class, UserData::class, ChatroomData::class, ReaderLogData::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getUserDao(): UserDataDao

    abstract fun getMessageDao(): MessageDataDao

    abstract fun getChatroomDataDao(): ChatroomDataDao

    abstract fun getReaderLogDataDao(): ReaderLogDataDao

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
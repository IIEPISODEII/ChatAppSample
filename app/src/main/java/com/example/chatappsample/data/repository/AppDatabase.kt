package com.example.chatappsample.data.repository

import com.example.chatappsample.data.dao.ChatroomDataDao
import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chatappsample.data.dao.MessageDataDao
import com.example.chatappsample.data.dao.UserDataDao
import com.example.chatappsample.data.entity.ChatroomData
import com.example.chatappsample.data.entity.MessageData
import com.example.chatappsample.data.entity.UserData
import com.example.chatappsample.data.entity.ReaderLogData

@Database(entities = [MessageData::class, UserData::class, ChatroomData::class, ReaderLogData::class], version = 4, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getUserDao(): UserDataDao

    abstract fun getMessageDao(): MessageDataDao

    abstract fun getChatroomDataDao(): ChatroomDataDao

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
                .addMigrations(MIGRATION_1_2)
                .addMigrations(MIGRATION_2_3)
                .addMigrations(MIGRATION_3_4)
                .build()
        }

        private val MIGRATION_1_2 = object: Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE users ADD COLUMN lastTimeStamp TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object: Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS readerlog (chatroomId TEXT NOT NULL DEFAULT '', userId TEXT NOT NULL DEFAULT '', readTime NOT NULL DEFAULT '', PRIMARY KEY(chatroomId, userId))")
            }
        }

        private val MIGRATION_3_4 = object: Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE chatrooms ADD COLUMN chatroomName TEXT NOT NULL DEFAULT ''")
            }
        }

        private const val DATABASE_NAME = "com.example.chatappsample"
    }
}
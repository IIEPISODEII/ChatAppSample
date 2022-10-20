package com.example.chatappsample.data.repository

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.chatappsample.data.dao.MessageDataDao
import com.example.chatappsample.data.dao.UserDataDao
import com.example.chatappsample.data.entity.MessageEntity
import com.example.chatappsample.data.entity.UserEntity

@Database(entities = [MessageEntity::class, UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase: RoomDatabase() {

    abstract fun getUserDao(): UserDataDao

    abstract fun getMessageDao(): MessageDataDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .addCallback (
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)

                        }
                    }
                )
                .build()
        }

        private const val DATABASE_NAME = "com.example.chatappsample"
    }
}
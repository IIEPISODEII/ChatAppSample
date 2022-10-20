package com.example.chatappsample.data.dao

import androidx.room.*
import com.example.chatappsample.data.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUser(userEntity: UserEntity)

    @Delete
    fun deleteUser(userEntity: UserEntity)

    @Query("SELECT * FROM users")
    fun getAllUserList(): Flow<List<UserEntity>>
}
package com.example.chatappsample.data.dao

import androidx.room.*
import com.example.chatappsample.data.entity.UserData
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDataDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertUser(userEntity: UserData)

    @Update
    fun updateUser(userEntity: UserData)

    @Delete
    fun deleteUser(userEntity: UserData)

    @Query("SELECT * FROM users")
    fun fetchUserList(): Flow<List<UserData>>

    @Query("SELECT * FROM users WHERE uid = :userId")
    fun fetchUserById(userId: String): UserData?

    @Query("SELECT * FROM users WHERE uid = :userId")
    fun fetchUserByIdAsFlow(userId: String): Flow<UserData?>
}
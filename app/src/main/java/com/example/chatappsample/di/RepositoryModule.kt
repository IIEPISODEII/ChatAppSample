package com.example.chatappsample.di

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.work.WorkManager
import com.example.chatappsample.BaseApplication
import com.example.chatappsample.data.repository.AppDatabase
import com.example.chatappsample.data.repository.ChatRepositoryImpl
import com.example.chatappsample.data.repository.SharedPreferenceRepositoryImpl
import com.example.chatappsample.data.repository.UserRepositoryImpl
import com.example.chatappsample.domain.repository.ChatRepository
import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import com.example.chatappsample.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(firebaseDatabase: FirebaseDatabase, firebaseAuth: FirebaseAuth, firebaseStorage: FirebaseStorage, userRoom: AppDatabase, workManager: WorkManager) : UserRepository {
        return UserRepositoryImpl(firebaseDatabase, firebaseAuth, firebaseStorage, userRoom, workManager)
    }

    @Singleton
    @Provides
    fun provideChatRepository(firebaseDatabase: FirebaseDatabase, firebaseStorage: FirebaseStorage, messageRoom: AppDatabase) : ChatRepository {
        return ChatRepositoryImpl(firebaseDatabase, firebaseStorage, messageRoom)
    }

    @Singleton
    @Provides
    fun provideSharedPreferenceRepository(@ApplicationContext context: Context) : SharedPreferenceRepository {
        return SharedPreferenceRepositoryImpl(context)
    }
}
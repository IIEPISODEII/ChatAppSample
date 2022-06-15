package com.example.chatappsample.di

import com.example.chatappsample.data.repository.ChatRepositoryImpl
import com.example.chatappsample.data.repository.UserRepositoryImpl
import com.example.chatappsample.domain.repository.ChatRepository
import com.example.chatappsample.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class RepositoryModule {

    @Singleton
    @Provides
    fun provideUserRepository(firebaseDatabase: FirebaseDatabase, firebaseAuth: FirebaseAuth) : UserRepository {
        return UserRepositoryImpl(firebaseDatabase, firebaseAuth)
    }

    @Singleton
    @Provides
    fun provideChatRepository(firebaseDatabase: FirebaseDatabase) : ChatRepository {
        return ChatRepositoryImpl(firebaseDatabase)
    }
}
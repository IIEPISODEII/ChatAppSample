package com.example.chatappsample.domain.repository

import androidx.lifecycle.MutableLiveData
import com.example.chatappsample.domain.`interface`.OnFileDownloadListener
import com.example.chatappsample.domain.`interface`.OnGetDataListener
import com.example.chatappsample.domain.`interface`.OnGetRegistrationListener
import com.example.chatappsample.domain.dto.User
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.flow.Flow
import android.net.Uri as Uri

interface UserRepository {
    fun getCurrentUser(listener: OnGetDataListener)

    fun getAllUsers(listener: OnGetDataListener)

    fun signOut(): Boolean

    fun signUp(name: String, email: String, password: String, listener: OnGetRegistrationListener)

    fun updateCurrentUser(user: User)

    fun downloadProfileImage(userID: String, onFileDownloadListener: OnFileDownloadListener)
}
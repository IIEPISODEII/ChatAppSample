package com.example.chatappsample

import android.app.Application
import com.example.chatappsample.domain.usecase.ReceiveMessagesFromExternalDBUsecase
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

    }
}
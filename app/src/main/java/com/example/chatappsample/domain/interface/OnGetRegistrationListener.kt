package com.example.chatappsample.domain.`interface`

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

interface OnGetRegistrationListener {
    fun onSuccess(task: Task<AuthResult>)

    fun onStart()

    fun <T : Any?> onFailure(error: T)
}
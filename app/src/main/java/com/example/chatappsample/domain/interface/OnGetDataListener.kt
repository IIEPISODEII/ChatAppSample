package com.example.chatappsample.domain.`interface`

import com.google.firebase.database.DataSnapshot

interface OnGetDataListener {
    fun onSuccess(dataSnapshot: DataSnapshot)

    fun onStart()

    fun <T : Any?> onFailure(error: T)
}
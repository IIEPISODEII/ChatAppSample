package com.example.chatappsample.domain.repository

interface SharedPreferenceRepository {

    fun isAutoLoginChecked(): Boolean
    fun setAutoLoginChecked(value: Boolean)

    fun getEmailAddress(): String
    fun setEmailAddress(value: String)

    fun getPassword(): String
    fun setPassword(value: String)

    fun getLastMessageIndex(chatRoom: String): Int
    fun saveLastMessageIndex(chatRoom: String, index: Int)

    companion object {
        const val SETTINGS = "Settings"
        const val SETTIGNS_AUTO_LOGIN = "Settings_AutoLogin"
        const val SETTIGNS_EMAIL = "Settings_Email"
        const val SETTIGNS_PASSWORD = "Settings_Password"

        const val LAST_MESSAGE_INDEX = "Last_Message_Index"
    }
}
package com.example.chatappsample.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.os.Message
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.chatappsample.BaseApplication
import com.example.chatappsample.domain.repository.SharedPreferenceRepository
import javax.inject.Inject

class SharedPreferenceRepositoryImpl @Inject constructor(
    context: Context
) : SharedPreferenceRepository {

    private val masterKey = MasterKey.Builder(
        context,
        MasterKey.DEFAULT_MASTER_KEY_ALIAS
    )
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val mSharedPref = EncryptedSharedPreferences.create(
        context,
        SharedPreferenceRepository.SETTINGS,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    private val edit = mSharedPref.edit()

    override fun isAutoLoginChecked(): Boolean {
        return mSharedPref.getBoolean(SharedPreferenceRepository.SETTIGNS_AUTO_LOGIN, false)
    }

    override fun setAutoLoginChecked(value: Boolean) {
        edit.putBoolean(SharedPreferenceRepository.SETTIGNS_AUTO_LOGIN, value).commit()
    }

    override fun getEmailAddress(): String {
        return mSharedPref.getString(SharedPreferenceRepository.SETTIGNS_EMAIL, "") ?: ""
    }

    override fun setEmailAddress(value: String) {
        edit.putString(SharedPreferenceRepository.SETTIGNS_EMAIL, value).commit()
    }

    override fun getPassword(): String {
        return mSharedPref.getString(SharedPreferenceRepository.SETTIGNS_PASSWORD, "") ?: ""
    }

    override fun setPassword(value: String) {
        edit.putString(SharedPreferenceRepository.SETTIGNS_PASSWORD, value).commit()
    }
}
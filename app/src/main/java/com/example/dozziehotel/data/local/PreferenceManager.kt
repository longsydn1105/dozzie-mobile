package com.example.dozziehotel.data.local

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.dozziehotel.utils.Constants
import androidx.core.content.edit
import com.example.dozziehotel.data.remote.SimpleAuthUser
import com.example.dozziehotel.data.remote.UserDto
import com.google.gson.Gson

class PreferenceManager(context: Context,private val gson : Gson) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences = try {
        createEncryptedPrefs(context)
    } catch (e: Exception) {
        context.deleteSharedPreferences(Constants.DATASTORE_NAME)
        createEncryptedPrefs(context)
    }

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            Constants.DATASTORE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    fun saveToken(token: String) {
        sharedPreferences.edit { putString(Constants.KEY_AUTH_TOKEN, token) }
    }

    fun getToken(): String? {
        return sharedPreferences.getString(Constants.KEY_AUTH_TOKEN, null)
    }

    fun saveUser(user: SimpleAuthUser) {
        val userJson = gson.toJson(user)
        Log.d("PREF", "Save User: $userJson")
        sharedPreferences.edit {
            putString(Constants.KEY_USER_DATA, userJson)
        }
    }

    fun getUser(): UserDto? {
        val userJson = sharedPreferences.getString(Constants.KEY_USER_DATA, null)
        return try {
            gson.fromJson(userJson, UserDto::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // --- CHỨC NĂNG TỰ ĐỘNG ĐĂNG NHẬP ---
    fun isLoggedIn(): Boolean {
        return !getToken().isNullOrBlank()
    }

    fun clearData() {
        sharedPreferences.edit(commit = true) {
            clear()
        }
    }


}
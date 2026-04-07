package com.example.galaxy.data.remote

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "auth")

class TokenStore(private val context: Context) {

    companion object {
        private val ACCESS_TOKEN = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        private val USER_ID = stringPreferencesKey("user_id")
        private val USER_NAME = stringPreferencesKey("user_name")
        private val USER_DEPT = stringPreferencesKey("user_dept")
        private val USER_YEAR = stringPreferencesKey("user_year")
    }

    suspend fun saveTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit {
            it[ACCESS_TOKEN] = accessToken
            it[REFRESH_TOKEN] = refreshToken
        }
    }

    suspend fun saveUser(id: String, name: String, dept: String = "", year: String = "") {
        context.dataStore.edit {
            it[USER_ID] = id
            it[USER_NAME] = name
            it[USER_DEPT] = dept
            it[USER_YEAR] = year
        }
    }

    suspend fun getAccessToken(): String? =
        context.dataStore.data.map { it[ACCESS_TOKEN] }.first()

    suspend fun getRefreshToken(): String? =
        context.dataStore.data.map { it[REFRESH_TOKEN] }.first()

    suspend fun getUserId(): String? =
        context.dataStore.data.map { it[USER_ID] }.first()

    suspend fun getUserName(): String? =
        context.dataStore.data.map { it[USER_NAME] }.first()

    suspend fun getUserDept(): String? =
        context.dataStore.data.map { it[USER_DEPT] }.first()

    suspend fun getUserYear(): String? =
        context.dataStore.data.map { it[USER_YEAR] }.first()

    suspend fun isLoggedIn(): Boolean = getAccessToken() != null

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}

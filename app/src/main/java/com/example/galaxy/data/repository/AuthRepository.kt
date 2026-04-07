package com.example.galaxy.data.repository

import android.content.Context
import com.example.galaxy.data.remote.MStarClient
import com.example.galaxy.data.remote.TokenStore
import com.example.galaxy.data.remote.api.LoginRequest

class AuthRepository(context: Context) {

    private val tokenStore = TokenStore(context)
    private val api = MStarClient.api

    suspend fun login(studentId: String, password: String): Result<String> {
        return try {
            val response = api.login(LoginRequest(email = studentId, password = password))
            tokenStore.saveTokens(response.token, response.tokenref)
            tokenStore.saveUser(
                response.userinfo.ID,
                response.userinfo.NAME,
                response.userinfo.HAKBU_NAME,
                response.userinfo.HAKJ_YEAR,
            )
            Result.success(response.userinfo.NAME)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun isLoggedIn(): Boolean = tokenStore.isLoggedIn()

    suspend fun getUserName(): String? = tokenStore.getUserName()

    suspend fun getUserId(): String? = tokenStore.getUserId()

    suspend fun logout() { tokenStore.clear() }
}

package com.example.galaxy.data.remote.api

import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class LoginResponse(
    val token: String,
    val tokenref: String,
    val userinfo: UserInfo,
)

@Serializable
data class UserInfo(
    val ID: String = "",
    val NAME: String = "",
    val DAEHAK_NAME: String = "",
    val HAKBU_NAME: String = "",
    val HAKJ_YEAR: String = "",
)

@Serializable
data class RenewRequest(val refreshToken: String)

@Serializable
data class RenewResponse(val data: RenewData)

@Serializable
data class RenewData(val accessToken: String, val refreshToken: String)

@Serializable
data class SemesterDateResponse(val data: List<SemesterDate> = emptyList())

@Serializable
data class SemesterDate(
    val DATE_YY: String = "",
    val DATE_HAKGI: String = "",
    val DATE_START: String = "",
    val DATE_14: String = "",
    val DATE_13: String = "",
    val DATE_12: String = "",
    val DATE_23: String? = null,
    val DATE_MID_EXAM: String? = null,
    val DATE_FIN_EXAM: String? = null,
    val DATE_LAST: String = "",
    val DATE_GRAD: String? = null,
)

interface MStarApi {
    @POST("/user/signin/local")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("/user/signin/renewtoken.local")
    suspend fun renewToken(@Body request: RenewRequest): RenewResponse

    @POST("/common/datefile.list")
    suspend fun getSemesterDates(@Body body: Map<String, String> = emptyMap()): SemesterDateResponse

    @POST("/common/dept.list")
    suspend fun getDeptList(@Body body: Map<String, String> = emptyMap()): Map<String, List<Map<String, String>>>
}

package com.example.galaxy.data.remote.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class LibResponse<T>(
    val success: Boolean = false,
    val data: T? = null,
)

@Serializable
data class RoomStatusList(
    val totalCount: Int = 0,
    val list: List<RoomStatus> = emptyList(),
)

@Serializable
data class RoomStatus(
    val id: Int = 0,
    val name: String = "",
    val isActive: Boolean = false,
    val total: Int = 0,
    val activeTotal: Int = 0,
    val occupied: Int = 0,
    val available: Int = 0,
)

@Serializable
data class PopularBookList(
    val totalCount: Int = 0,
    val list: List<PopularBook> = emptyList(),
)

@Serializable
data class PopularBook(
    val id: Long = 0,
    val titleStatement: String = "",
    val author: String = "",
    val isbn: String = "",
    val chargeCnt: Int = 0,
    val thumbnailUrl: String? = null,
)

interface LibraryApi {
    @GET("smufu-api/pc/1/rooms-status")
    suspend fun getRoomStatus(): LibResponse<RoomStatusList>

    @GET("pyxis-api/1/popular-charged-books")
    suspend fun getPopularBooks(@Query("max") max: Int = 10): LibResponse<PopularBookList>
}

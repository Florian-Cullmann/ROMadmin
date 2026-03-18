package com.romadmin.app.data.remote

import com.romadmin.app.domain.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface RomAdminApi {

    // Auth
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @GET("api/auth/me")
    suspend fun me(): User

    // Platforms
    @GET("api/platforms")
    suspend fun getPlatforms(): List<Platform>

    @GET("api/platforms/{id}")
    suspend fun getPlatform(@Path("id") id: Int): Platform

    // Games
    @GET("api/games")
    suspend fun getGames(
        @Query("platformId") platformId: Int? = null,
        @Query("search") search: String? = null,
        @Query("sort") sort: String? = null,
        @Query("order") order: String? = null,
        @Query("page") page: Int? = null,
        @Query("limit") limit: Int? = null,
    ): PaginatedResponse<Game>

    @GET("api/games/{id}")
    suspend fun getGame(@Path("id") id: Int): Game

    // Downloads
    @GET("api/downloads/platforms/{id}/manifest")
    suspend fun getPlatformManifest(@Path("id") platformId: Int): PlatformManifest

    @Streaming
    @GET("api/downloads/games/{id}")
    suspend fun downloadGame(
        @Path("id") gameId: Int,
        @Header("Range") range: String? = null,
    ): Response<ResponseBody>

    // Saves
    @GET("api/saves/game/{gameId}")
    suspend fun getSaves(@Path("gameId") gameId: Int): List<SaveFile>

    @POST("api/saves/sync-status")
    suspend fun syncStatus(@Body request: SyncStatusRequest): SyncStatusResponse

    @Multipart
    @POST("api/saves/upload")
    suspend fun uploadSave(
        @Part file: MultipartBody.Part,
        @Part("gameId") gameId: RequestBody,
        @Part("deviceName") deviceName: RequestBody,
    ): SaveFile

    @POST("api/saves/upload")
    suspend fun uploadSaveRaw(@Body body: okhttp3.MultipartBody): SaveFile

    @Streaming
    @GET("api/saves/{saveId}/download")
    suspend fun downloadSave(@Path("saveId") saveId: Int): Response<ResponseBody>
}

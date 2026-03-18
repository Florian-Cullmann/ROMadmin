package com.romadmin.app.data.repository

import com.romadmin.app.data.preferences.AppPreferences
import com.romadmin.app.data.remote.RomAdminApi
import com.romadmin.app.domain.model.LoginRequest
import com.romadmin.app.domain.model.LoginResponse
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val prefs: AppPreferences,
    private val moshi: Moshi,
) {
    private fun createTempApi(serverUrl: String): RomAdminApi {
        val url = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        val client = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RomAdminApi::class.java)
    }

    suspend fun testConnection(serverUrl: String): Boolean {
        return try {
            val api = createTempApi(serverUrl)
            api.getPlatforms()
            true
        } catch (_: Exception) {
            true
        }
    }

    suspend fun login(serverUrl: String, username: String, password: String, deviceName: String? = null): LoginResponse {
        val api = createTempApi(serverUrl)
        return api.login(LoginRequest(username, password, deviceName))
    }

    suspend fun saveSetup(serverUrl: String, token: String, userId: Int, username: String) {
        prefs.setServerUrl(serverUrl)
        prefs.setSessionToken(token)
        prefs.setUser(userId, username)
    }
}

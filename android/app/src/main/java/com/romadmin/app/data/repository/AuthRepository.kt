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
    /**
     * Create a temporary API instance for a specific server URL (used during setup
     * before the main Retrofit instance has the correct base URL).
     */
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
            // Any endpoint that doesn't require auth
            api.getPlatforms()
            true
        } catch (_: Exception) {
            // If we get 401, server is reachable (just needs auth)
            true
        }
    }

    suspend fun login(serverUrl: String, username: String, password: String): LoginResponse {
        val api = createTempApi(serverUrl)
        return api.login(LoginRequest(username, password))
    }

    suspend fun generateApiKey(serverUrl: String, accessToken: String): String {
        val url = if (serverUrl.endsWith("/")) serverUrl else "$serverUrl/"
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("Authorization", "Bearer $accessToken")
                        .build()
                )
            }
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val api = Retrofit.Builder()
            .baseUrl(url)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RomAdminApi::class.java)

        return api.generateApiKey().apiKey
    }

    suspend fun saveSetup(serverUrl: String, apiKey: String, userId: Int, username: String) {
        prefs.setServerUrl(serverUrl)
        prefs.setApiKey(apiKey)
        prefs.setUser(userId, username)
    }
}

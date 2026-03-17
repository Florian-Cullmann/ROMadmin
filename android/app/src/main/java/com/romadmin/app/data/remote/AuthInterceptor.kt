package com.romadmin.app.data.remote

import com.romadmin.app.data.preferences.AppPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val prefs: AppPreferences,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val apiKey = runBlocking { prefs.apiKey.first() }
        val request = if (!apiKey.isNullOrBlank()) {
            chain.request().newBuilder()
                .header("x-api-key", apiKey)
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }
}

package com.romadmin.app.data.repository

import com.romadmin.app.data.remote.RomAdminApi
import com.romadmin.app.domain.model.Platform
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlatformRepository @Inject constructor(
    private val api: RomAdminApi,
) {
    suspend fun getPlatforms(): List<Platform> = api.getPlatforms()

    suspend fun getPlatform(id: Int): Platform = api.getPlatform(id)
}

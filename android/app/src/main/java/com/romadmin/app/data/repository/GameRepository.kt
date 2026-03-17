package com.romadmin.app.data.repository

import com.romadmin.app.data.remote.RomAdminApi
import com.romadmin.app.domain.model.Game
import com.romadmin.app.domain.model.PaginatedResponse
import com.romadmin.app.domain.model.PlatformManifest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameRepository @Inject constructor(
    private val api: RomAdminApi,
) {
    suspend fun getGames(
        platformId: Int? = null,
        search: String? = null,
        sort: String? = null,
        order: String? = null,
        page: Int? = null,
        limit: Int? = null,
    ): PaginatedResponse<Game> = api.getGames(platformId, search, sort, order, page, limit)

    suspend fun getGame(id: Int): Game = api.getGame(id)

    suspend fun getPlatformManifest(platformId: Int): PlatformManifest =
        api.getPlatformManifest(platformId)
}

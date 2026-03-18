package com.romadmin.app.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val role: String,
    val language: String,
    val createdAt: String,
    val updatedAt: String,
)

@JsonClass(generateAdapter = true)
data class Platform(
    val id: Int,
    val folderName: String,
    val displayName: String,
    val slug: String,
    val igdbPlatformId: Int?,
    val thumbnailUrl: String?,
    val fileExtensions: String,
    val sortOrder: Int,
    val createdAt: String,
    val updatedAt: String,
    val _count: GameCount? = null,
)

@JsonClass(generateAdapter = true)
data class GameCount(val games: Int)

@JsonClass(generateAdapter = true)
data class Game(
    val id: Int,
    val platformId: Int,
    val fileName: String,
    val filePath: String,
    val fileSize: String,
    val displayName: String,
    val slug: String,
    val igdbGameId: Int?,
    val description: String?,
    val thumbnailUrl: String?,
    val coverUrl: String?,
    val releaseDate: String?,
    val isDirectory: Boolean,
    val metadataFetched: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val platform: Platform? = null,
)

@JsonClass(generateAdapter = true)
data class SaveFile(
    val id: Int,
    val gameId: Int,
    val userId: Int,
    val fileName: String,
    val filePath: String,
    val fileSize: String,
    val deviceName: String?,
    val isLatest: Boolean,
    val uploadedAt: String,
)

@JsonClass(generateAdapter = true)
data class PaginatedResponse<T>(
    val data: List<T>,
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int,
)

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val username: String,
    val password: String,
    val deviceName: String? = null,
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    val user: User,
)

@JsonClass(generateAdapter = true)
data class PlatformManifest(
    val platform: ManifestPlatform,
    val games: List<ManifestGame>,
    val totalSize: String,
)

@JsonClass(generateAdapter = true)
data class ManifestPlatform(
    val id: Int,
    val folderName: String,
    val displayName: String,
)

@JsonClass(generateAdapter = true)
data class ManifestGame(
    val id: Int,
    val fileName: String,
    val fileSize: String,
    val displayName: String,
    val thumbnailUrl: String?,
    val isDirectory: Boolean,
)

@JsonClass(generateAdapter = true)
data class SyncStatusRequest(
    val deviceName: String,
    val games: List<SyncGameEntry>,
)

@JsonClass(generateAdapter = true)
data class SyncGameEntry(
    val gameId: Int,
    val localTimestamp: String?,
)

@JsonClass(generateAdapter = true)
data class SyncStatusResponse(
    val games: List<SyncStatusGame>,
)

@JsonClass(generateAdapter = true)
data class SyncStatusGame(
    val gameId: Int,
    val action: String,
    val serverSave: ServerSaveInfo? = null,
)

@JsonClass(generateAdapter = true)
data class ServerSaveInfo(
    val id: Int,
    val uploadedAt: String,
    val clientTimestamp: String? = null,
    val fileName: String,
    val fileSize: String,
    val deviceName: String?,
)

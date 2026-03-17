package com.romadmin.app.data.local.dao

import androidx.room.*
import com.romadmin.app.data.local.entity.DownloadStatus
import com.romadmin.app.data.local.entity.DownloadedGame
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloaded_games ORDER BY downloadedAt DESC")
    fun getAllDownloads(): Flow<List<DownloadedGame>>

    @Query("SELECT * FROM downloaded_games WHERE status = :status ORDER BY downloadedAt DESC")
    fun getByStatus(status: DownloadStatus): Flow<List<DownloadedGame>>

    @Query("SELECT * FROM downloaded_games WHERE gameId = :gameId")
    suspend fun getByGameId(gameId: Int): DownloadedGame?

    @Query("SELECT * FROM downloaded_games WHERE gameId = :gameId")
    fun observeByGameId(gameId: Int): Flow<DownloadedGame?>

    @Query("SELECT * FROM downloaded_games WHERE platformId = :platformId")
    fun getByPlatformId(platformId: Int): Flow<List<DownloadedGame>>

    @Query("SELECT * FROM downloaded_games WHERE status IN (:statuses)")
    fun getByStatuses(statuses: List<DownloadStatus>): Flow<List<DownloadedGame>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(download: DownloadedGame)

    @Update
    suspend fun update(download: DownloadedGame)

    @Query("UPDATE downloaded_games SET status = :status, bytesDownloaded = :bytes WHERE gameId = :gameId")
    suspend fun updateProgress(gameId: Int, status: DownloadStatus, bytes: Long)

    @Query("UPDATE downloaded_games SET status = :status, downloadedAt = :completedAt WHERE gameId = :gameId")
    suspend fun markCompleted(gameId: Int, status: DownloadStatus = DownloadStatus.COMPLETED, completedAt: Long = System.currentTimeMillis())

    @Delete
    suspend fun delete(download: DownloadedGame)

    @Query("DELETE FROM downloaded_games WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: Int)

    @Query("SELECT COUNT(*) FROM downloaded_games WHERE status = :status")
    fun countByStatus(status: DownloadStatus): Flow<Int>
}

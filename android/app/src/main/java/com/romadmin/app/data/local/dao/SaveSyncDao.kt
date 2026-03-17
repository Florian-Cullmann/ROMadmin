package com.romadmin.app.data.local.dao

import androidx.room.*
import com.romadmin.app.data.local.entity.SaveSyncState
import kotlinx.coroutines.flow.Flow

@Dao
interface SaveSyncDao {

    @Query("SELECT * FROM save_sync_state")
    fun getAll(): Flow<List<SaveSyncState>>

    @Query("SELECT * FROM save_sync_state WHERE gameId = :gameId")
    suspend fun getByGameId(gameId: Int): SaveSyncState?

    @Query("SELECT * FROM save_sync_state WHERE gameId = :gameId")
    fun observeByGameId(gameId: Int): Flow<SaveSyncState?>

    @Query("SELECT * FROM save_sync_state WHERE gameId IN (:gameIds)")
    suspend fun getByGameIds(gameIds: List<Int>): List<SaveSyncState>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SaveSyncState)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(states: List<SaveSyncState>)

    @Delete
    suspend fun delete(state: SaveSyncState)

    @Query("DELETE FROM save_sync_state WHERE gameId = :gameId")
    suspend fun deleteByGameId(gameId: Int)
}

package com.romadmin.app.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.romadmin.app.R
import com.romadmin.app.RomAdminApp
import com.romadmin.app.data.repository.SaveSyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class SaveSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val saveSyncRepository: SaveSyncRepository,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        try { setForeground(createForegroundInfo()) } catch (_: Exception) {}

        return try {
            val count = saveSyncRepository.performSync()
            Log.i("SaveSyncWorker", "Sync completed: $count files synced")
            Result.success()
        } catch (e: retrofit2.HttpException) {
            Log.e("SaveSyncWorker", "Sync failed: HTTP ${e.code()} - ${e.message()}", e)
            // Don't retry auth failures
            if (e.code() == 401) Result.failure() else if (runAttemptCount < 3) Result.retry() else Result.failure()
        } catch (e: Exception) {
            Log.e("SaveSyncWorker", "Sync failed", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        val notification = NotificationCompat.Builder(applicationContext, RomAdminApp.CHANNEL_SYNC)
            .setContentTitle("Syncing saves")
            .setContentText("Checking for save file updates...")
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .setProgress(0, 0, true)
            .build()

        return ForegroundInfo(
            NOTIFICATION_ID,
            notification,
            ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
        )
    }

    companion object {
        const val WORK_NAME = "save_sync"
        const val WORK_NAME_PERIODIC = "save_sync_periodic"
        private const val NOTIFICATION_ID = 9999
    }
}

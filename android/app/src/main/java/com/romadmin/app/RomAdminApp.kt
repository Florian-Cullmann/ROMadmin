package com.romadmin.app

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.*
import com.romadmin.app.data.preferences.AppPreferences
import com.romadmin.app.worker.SaveSyncWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class RomAdminApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var prefs: AppPreferences

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        ensurePeriodicSync()
    }

    private fun createNotificationChannels() {
        val manager = getSystemService(NotificationManager::class.java)

        val downloadChannel = NotificationChannel(
            CHANNEL_DOWNLOADS,
            getString(R.string.download_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.download_channel_desc)
        }

        val syncChannel = NotificationChannel(
            CHANNEL_SYNC,
            getString(R.string.sync_channel_name),
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = getString(R.string.sync_channel_desc)
        }

        manager.createNotificationChannels(listOf(downloadChannel, syncChannel))
    }

    /**
     * Always ensure periodic sync is registered if setup is complete.
     * This survives app updates which clear WorkManager state.
     */
    private fun ensurePeriodicSync() {
        CoroutineScope(Dispatchers.IO).launch {
            val isSetup = prefs.isSetupComplete.first()
            if (!isSetup) return@launch

            val request = PeriodicWorkRequestBuilder<SaveSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(this@RomAdminApp)
                .enqueueUniquePeriodicWork(
                    SaveSyncWorker.WORK_NAME_PERIODIC,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request,
                )
        }
    }

    companion object {
        const val CHANNEL_DOWNLOADS = "downloads"
        const val CHANNEL_SYNC = "save_sync"
    }
}

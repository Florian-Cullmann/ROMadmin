package com.romadmin.app.di

import android.content.Context
import androidx.room.Room
import com.romadmin.app.data.local.AppDatabase
import com.romadmin.app.data.local.dao.DownloadDao
import com.romadmin.app.data.local.dao.SaveSyncDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "romadmin.db")
            .build()

    @Provides
    fun provideDownloadDao(db: AppDatabase): DownloadDao = db.downloadDao()

    @Provides
    fun provideSaveSyncDao(db: AppDatabase): SaveSyncDao = db.saveSyncDao()
}

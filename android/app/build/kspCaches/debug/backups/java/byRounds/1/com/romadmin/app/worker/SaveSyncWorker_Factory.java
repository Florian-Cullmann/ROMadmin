package com.romadmin.app.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.romadmin.app.data.repository.SaveSyncRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class SaveSyncWorker_Factory {
  private final Provider<SaveSyncRepository> saveSyncRepositoryProvider;

  public SaveSyncWorker_Factory(Provider<SaveSyncRepository> saveSyncRepositoryProvider) {
    this.saveSyncRepositoryProvider = saveSyncRepositoryProvider;
  }

  public SaveSyncWorker get(Context appContext, WorkerParameters params) {
    return newInstance(appContext, params, saveSyncRepositoryProvider.get());
  }

  public static SaveSyncWorker_Factory create(
      Provider<SaveSyncRepository> saveSyncRepositoryProvider) {
    return new SaveSyncWorker_Factory(saveSyncRepositoryProvider);
  }

  public static SaveSyncWorker newInstance(Context appContext, WorkerParameters params,
      SaveSyncRepository saveSyncRepository) {
    return new SaveSyncWorker(appContext, params, saveSyncRepository);
  }
}

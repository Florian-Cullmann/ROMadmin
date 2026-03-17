package com.romadmin.app.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import com.romadmin.app.data.local.dao.DownloadDao;
import com.romadmin.app.data.remote.RomAdminApi;
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
public final class DownloadWorker_Factory {
  private final Provider<RomAdminApi> apiProvider;

  private final Provider<DownloadDao> downloadDaoProvider;

  public DownloadWorker_Factory(Provider<RomAdminApi> apiProvider,
      Provider<DownloadDao> downloadDaoProvider) {
    this.apiProvider = apiProvider;
    this.downloadDaoProvider = downloadDaoProvider;
  }

  public DownloadWorker get(Context appContext, WorkerParameters params) {
    return newInstance(appContext, params, apiProvider.get(), downloadDaoProvider.get());
  }

  public static DownloadWorker_Factory create(Provider<RomAdminApi> apiProvider,
      Provider<DownloadDao> downloadDaoProvider) {
    return new DownloadWorker_Factory(apiProvider, downloadDaoProvider);
  }

  public static DownloadWorker newInstance(Context appContext, WorkerParameters params,
      RomAdminApi api, DownloadDao downloadDao) {
    return new DownloadWorker(appContext, params, api, downloadDao);
  }
}

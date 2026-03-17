package com.romadmin.app.data.repository;

import com.romadmin.app.data.local.dao.DownloadDao;
import com.romadmin.app.data.local.dao.SaveSyncDao;
import com.romadmin.app.data.preferences.AppPreferences;
import com.romadmin.app.data.remote.RomAdminApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
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
public final class SaveSyncRepository_Factory implements Factory<SaveSyncRepository> {
  private final Provider<RomAdminApi> apiProvider;

  private final Provider<DownloadDao> downloadDaoProvider;

  private final Provider<SaveSyncDao> saveSyncDaoProvider;

  private final Provider<AppPreferences> prefsProvider;

  public SaveSyncRepository_Factory(Provider<RomAdminApi> apiProvider,
      Provider<DownloadDao> downloadDaoProvider, Provider<SaveSyncDao> saveSyncDaoProvider,
      Provider<AppPreferences> prefsProvider) {
    this.apiProvider = apiProvider;
    this.downloadDaoProvider = downloadDaoProvider;
    this.saveSyncDaoProvider = saveSyncDaoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SaveSyncRepository get() {
    return newInstance(apiProvider.get(), downloadDaoProvider.get(), saveSyncDaoProvider.get(), prefsProvider.get());
  }

  public static SaveSyncRepository_Factory create(Provider<RomAdminApi> apiProvider,
      Provider<DownloadDao> downloadDaoProvider, Provider<SaveSyncDao> saveSyncDaoProvider,
      Provider<AppPreferences> prefsProvider) {
    return new SaveSyncRepository_Factory(apiProvider, downloadDaoProvider, saveSyncDaoProvider, prefsProvider);
  }

  public static SaveSyncRepository newInstance(RomAdminApi api, DownloadDao downloadDao,
      SaveSyncDao saveSyncDao, AppPreferences prefs) {
    return new SaveSyncRepository(api, downloadDao, saveSyncDao, prefs);
  }
}

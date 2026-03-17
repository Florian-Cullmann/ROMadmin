package com.romadmin.app.data.repository;

import android.content.Context;
import com.romadmin.app.data.local.dao.DownloadDao;
import com.romadmin.app.data.preferences.AppPreferences;
import com.romadmin.app.data.remote.RomAdminApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class DownloadRepository_Factory implements Factory<DownloadRepository> {
  private final Provider<Context> contextProvider;

  private final Provider<RomAdminApi> apiProvider;

  private final Provider<DownloadDao> downloadDaoProvider;

  private final Provider<AppPreferences> prefsProvider;

  public DownloadRepository_Factory(Provider<Context> contextProvider,
      Provider<RomAdminApi> apiProvider, Provider<DownloadDao> downloadDaoProvider,
      Provider<AppPreferences> prefsProvider) {
    this.contextProvider = contextProvider;
    this.apiProvider = apiProvider;
    this.downloadDaoProvider = downloadDaoProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public DownloadRepository get() {
    return newInstance(contextProvider.get(), apiProvider.get(), downloadDaoProvider.get(), prefsProvider.get());
  }

  public static DownloadRepository_Factory create(Provider<Context> contextProvider,
      Provider<RomAdminApi> apiProvider, Provider<DownloadDao> downloadDaoProvider,
      Provider<AppPreferences> prefsProvider) {
    return new DownloadRepository_Factory(contextProvider, apiProvider, downloadDaoProvider, prefsProvider);
  }

  public static DownloadRepository newInstance(Context context, RomAdminApi api,
      DownloadDao downloadDao, AppPreferences prefs) {
    return new DownloadRepository(context, api, downloadDao, prefs);
  }
}

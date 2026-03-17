package com.romadmin.app.ui.settings;

import android.content.Context;
import com.romadmin.app.data.preferences.AppPreferences;
import com.romadmin.app.data.repository.SaveSyncRepository;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

@ScopeMetadata
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
public final class SettingsViewModel_Factory implements Factory<SettingsViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<AppPreferences> prefsProvider;

  private final Provider<SaveSyncRepository> saveSyncRepositoryProvider;

  public SettingsViewModel_Factory(Provider<Context> contextProvider,
      Provider<AppPreferences> prefsProvider,
      Provider<SaveSyncRepository> saveSyncRepositoryProvider) {
    this.contextProvider = contextProvider;
    this.prefsProvider = prefsProvider;
    this.saveSyncRepositoryProvider = saveSyncRepositoryProvider;
  }

  @Override
  public SettingsViewModel get() {
    return newInstance(contextProvider.get(), prefsProvider.get(), saveSyncRepositoryProvider.get());
  }

  public static SettingsViewModel_Factory create(Provider<Context> contextProvider,
      Provider<AppPreferences> prefsProvider,
      Provider<SaveSyncRepository> saveSyncRepositoryProvider) {
    return new SettingsViewModel_Factory(contextProvider, prefsProvider, saveSyncRepositoryProvider);
  }

  public static SettingsViewModel newInstance(Context context, AppPreferences prefs,
      SaveSyncRepository saveSyncRepository) {
    return new SettingsViewModel(context, prefs, saveSyncRepository);
  }
}

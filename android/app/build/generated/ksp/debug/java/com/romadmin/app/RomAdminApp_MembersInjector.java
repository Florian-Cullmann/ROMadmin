package com.romadmin.app;

import androidx.hilt.work.HiltWorkerFactory;
import com.romadmin.app.data.preferences.AppPreferences;
import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class RomAdminApp_MembersInjector implements MembersInjector<RomAdminApp> {
  private final Provider<HiltWorkerFactory> workerFactoryProvider;

  private final Provider<AppPreferences> prefsProvider;

  public RomAdminApp_MembersInjector(Provider<HiltWorkerFactory> workerFactoryProvider,
      Provider<AppPreferences> prefsProvider) {
    this.workerFactoryProvider = workerFactoryProvider;
    this.prefsProvider = prefsProvider;
  }

  public static MembersInjector<RomAdminApp> create(
      Provider<HiltWorkerFactory> workerFactoryProvider, Provider<AppPreferences> prefsProvider) {
    return new RomAdminApp_MembersInjector(workerFactoryProvider, prefsProvider);
  }

  @Override
  public void injectMembers(RomAdminApp instance) {
    injectWorkerFactory(instance, workerFactoryProvider.get());
    injectPrefs(instance, prefsProvider.get());
  }

  @InjectedFieldSignature("com.romadmin.app.RomAdminApp.workerFactory")
  public static void injectWorkerFactory(RomAdminApp instance, HiltWorkerFactory workerFactory) {
    instance.workerFactory = workerFactory;
  }

  @InjectedFieldSignature("com.romadmin.app.RomAdminApp.prefs")
  public static void injectPrefs(RomAdminApp instance, AppPreferences prefs) {
    instance.prefs = prefs;
  }
}

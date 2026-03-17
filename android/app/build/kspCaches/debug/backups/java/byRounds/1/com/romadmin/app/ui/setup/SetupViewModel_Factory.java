package com.romadmin.app.ui.setup;

import android.content.Context;
import com.romadmin.app.data.preferences.AppPreferences;
import com.romadmin.app.data.repository.AuthRepository;
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
public final class SetupViewModel_Factory implements Factory<SetupViewModel> {
  private final Provider<Context> contextProvider;

  private final Provider<AuthRepository> authRepositoryProvider;

  private final Provider<AppPreferences> prefsProvider;

  public SetupViewModel_Factory(Provider<Context> contextProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<AppPreferences> prefsProvider) {
    this.contextProvider = contextProvider;
    this.authRepositoryProvider = authRepositoryProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public SetupViewModel get() {
    return newInstance(contextProvider.get(), authRepositoryProvider.get(), prefsProvider.get());
  }

  public static SetupViewModel_Factory create(Provider<Context> contextProvider,
      Provider<AuthRepository> authRepositoryProvider, Provider<AppPreferences> prefsProvider) {
    return new SetupViewModel_Factory(contextProvider, authRepositoryProvider, prefsProvider);
  }

  public static SetupViewModel newInstance(Context context, AuthRepository authRepository,
      AppPreferences prefs) {
    return new SetupViewModel(context, authRepository, prefs);
  }
}

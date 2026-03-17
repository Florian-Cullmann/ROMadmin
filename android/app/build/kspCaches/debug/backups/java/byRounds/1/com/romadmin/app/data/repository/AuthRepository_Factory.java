package com.romadmin.app.data.repository;

import com.romadmin.app.data.preferences.AppPreferences;
import com.squareup.moshi.Moshi;
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
public final class AuthRepository_Factory implements Factory<AuthRepository> {
  private final Provider<AppPreferences> prefsProvider;

  private final Provider<Moshi> moshiProvider;

  public AuthRepository_Factory(Provider<AppPreferences> prefsProvider,
      Provider<Moshi> moshiProvider) {
    this.prefsProvider = prefsProvider;
    this.moshiProvider = moshiProvider;
  }

  @Override
  public AuthRepository get() {
    return newInstance(prefsProvider.get(), moshiProvider.get());
  }

  public static AuthRepository_Factory create(Provider<AppPreferences> prefsProvider,
      Provider<Moshi> moshiProvider) {
    return new AuthRepository_Factory(prefsProvider, moshiProvider);
  }

  public static AuthRepository newInstance(AppPreferences prefs, Moshi moshi) {
    return new AuthRepository(prefs, moshi);
  }
}

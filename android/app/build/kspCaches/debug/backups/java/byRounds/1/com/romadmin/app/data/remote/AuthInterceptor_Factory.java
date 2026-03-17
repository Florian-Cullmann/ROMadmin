package com.romadmin.app.data.remote;

import com.romadmin.app.data.preferences.AppPreferences;
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
public final class AuthInterceptor_Factory implements Factory<AuthInterceptor> {
  private final Provider<AppPreferences> prefsProvider;

  public AuthInterceptor_Factory(Provider<AppPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public AuthInterceptor get() {
    return newInstance(prefsProvider.get());
  }

  public static AuthInterceptor_Factory create(Provider<AppPreferences> prefsProvider) {
    return new AuthInterceptor_Factory(prefsProvider);
  }

  public static AuthInterceptor newInstance(AppPreferences prefs) {
    return new AuthInterceptor(prefs);
  }
}

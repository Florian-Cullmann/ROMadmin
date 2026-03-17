package com.romadmin.app.ui.navigation;

import com.romadmin.app.data.preferences.AppPreferences;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
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
public final class NavViewModel_Factory implements Factory<NavViewModel> {
  private final Provider<AppPreferences> prefsProvider;

  public NavViewModel_Factory(Provider<AppPreferences> prefsProvider) {
    this.prefsProvider = prefsProvider;
  }

  @Override
  public NavViewModel get() {
    return newInstance(prefsProvider.get());
  }

  public static NavViewModel_Factory create(Provider<AppPreferences> prefsProvider) {
    return new NavViewModel_Factory(prefsProvider);
  }

  public static NavViewModel newInstance(AppPreferences prefs) {
    return new NavViewModel(prefs);
  }
}

package com.romadmin.app.data.repository;

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
public final class PlatformRepository_Factory implements Factory<PlatformRepository> {
  private final Provider<RomAdminApi> apiProvider;

  public PlatformRepository_Factory(Provider<RomAdminApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public PlatformRepository get() {
    return newInstance(apiProvider.get());
  }

  public static PlatformRepository_Factory create(Provider<RomAdminApi> apiProvider) {
    return new PlatformRepository_Factory(apiProvider);
  }

  public static PlatformRepository newInstance(RomAdminApi api) {
    return new PlatformRepository(api);
  }
}

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
public final class GameRepository_Factory implements Factory<GameRepository> {
  private final Provider<RomAdminApi> apiProvider;

  public GameRepository_Factory(Provider<RomAdminApi> apiProvider) {
    this.apiProvider = apiProvider;
  }

  @Override
  public GameRepository get() {
    return newInstance(apiProvider.get());
  }

  public static GameRepository_Factory create(Provider<RomAdminApi> apiProvider) {
    return new GameRepository_Factory(apiProvider);
  }

  public static GameRepository newInstance(RomAdminApi api) {
    return new GameRepository(api);
  }
}

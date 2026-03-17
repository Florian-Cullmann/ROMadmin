package com.romadmin.app.ui.platforms;

import com.romadmin.app.data.repository.PlatformRepository;
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
public final class PlatformListViewModel_Factory implements Factory<PlatformListViewModel> {
  private final Provider<PlatformRepository> platformRepositoryProvider;

  public PlatformListViewModel_Factory(Provider<PlatformRepository> platformRepositoryProvider) {
    this.platformRepositoryProvider = platformRepositoryProvider;
  }

  @Override
  public PlatformListViewModel get() {
    return newInstance(platformRepositoryProvider.get());
  }

  public static PlatformListViewModel_Factory create(
      Provider<PlatformRepository> platformRepositoryProvider) {
    return new PlatformListViewModel_Factory(platformRepositoryProvider);
  }

  public static PlatformListViewModel newInstance(PlatformRepository platformRepository) {
    return new PlatformListViewModel(platformRepository);
  }
}

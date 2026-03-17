package com.romadmin.app.ui.games;

import androidx.lifecycle.SavedStateHandle;
import com.romadmin.app.data.local.dao.DownloadDao;
import com.romadmin.app.data.repository.DownloadRepository;
import com.romadmin.app.data.repository.GameRepository;
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
public final class GameListViewModel_Factory implements Factory<GameListViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<GameRepository> gameRepositoryProvider;

  private final Provider<PlatformRepository> platformRepositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<DownloadDao> downloadDaoProvider;

  public GameListViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<GameRepository> gameRepositoryProvider,
      Provider<PlatformRepository> platformRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<DownloadDao> downloadDaoProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.gameRepositoryProvider = gameRepositoryProvider;
    this.platformRepositoryProvider = platformRepositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.downloadDaoProvider = downloadDaoProvider;
  }

  @Override
  public GameListViewModel get() {
    return newInstance(savedStateHandleProvider.get(), gameRepositoryProvider.get(), platformRepositoryProvider.get(), downloadRepositoryProvider.get(), downloadDaoProvider.get());
  }

  public static GameListViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<GameRepository> gameRepositoryProvider,
      Provider<PlatformRepository> platformRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<DownloadDao> downloadDaoProvider) {
    return new GameListViewModel_Factory(savedStateHandleProvider, gameRepositoryProvider, platformRepositoryProvider, downloadRepositoryProvider, downloadDaoProvider);
  }

  public static GameListViewModel newInstance(SavedStateHandle savedStateHandle,
      GameRepository gameRepository, PlatformRepository platformRepository,
      DownloadRepository downloadRepository, DownloadDao downloadDao) {
    return new GameListViewModel(savedStateHandle, gameRepository, platformRepository, downloadRepository, downloadDao);
  }
}

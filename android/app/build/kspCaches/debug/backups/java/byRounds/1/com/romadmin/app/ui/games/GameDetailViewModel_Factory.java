package com.romadmin.app.ui.games;

import androidx.lifecycle.SavedStateHandle;
import com.romadmin.app.data.local.dao.DownloadDao;
import com.romadmin.app.data.local.dao.SaveSyncDao;
import com.romadmin.app.data.repository.DownloadRepository;
import com.romadmin.app.data.repository.GameRepository;
import com.romadmin.app.data.repository.SaveSyncRepository;
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
public final class GameDetailViewModel_Factory implements Factory<GameDetailViewModel> {
  private final Provider<SavedStateHandle> savedStateHandleProvider;

  private final Provider<GameRepository> gameRepositoryProvider;

  private final Provider<DownloadRepository> downloadRepositoryProvider;

  private final Provider<DownloadDao> downloadDaoProvider;

  private final Provider<SaveSyncRepository> saveSyncRepositoryProvider;

  private final Provider<SaveSyncDao> saveSyncDaoProvider;

  public GameDetailViewModel_Factory(Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<GameRepository> gameRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<DownloadDao> downloadDaoProvider,
      Provider<SaveSyncRepository> saveSyncRepositoryProvider,
      Provider<SaveSyncDao> saveSyncDaoProvider) {
    this.savedStateHandleProvider = savedStateHandleProvider;
    this.gameRepositoryProvider = gameRepositoryProvider;
    this.downloadRepositoryProvider = downloadRepositoryProvider;
    this.downloadDaoProvider = downloadDaoProvider;
    this.saveSyncRepositoryProvider = saveSyncRepositoryProvider;
    this.saveSyncDaoProvider = saveSyncDaoProvider;
  }

  @Override
  public GameDetailViewModel get() {
    return newInstance(savedStateHandleProvider.get(), gameRepositoryProvider.get(), downloadRepositoryProvider.get(), downloadDaoProvider.get(), saveSyncRepositoryProvider.get(), saveSyncDaoProvider.get());
  }

  public static GameDetailViewModel_Factory create(
      Provider<SavedStateHandle> savedStateHandleProvider,
      Provider<GameRepository> gameRepositoryProvider,
      Provider<DownloadRepository> downloadRepositoryProvider,
      Provider<DownloadDao> downloadDaoProvider,
      Provider<SaveSyncRepository> saveSyncRepositoryProvider,
      Provider<SaveSyncDao> saveSyncDaoProvider) {
    return new GameDetailViewModel_Factory(savedStateHandleProvider, gameRepositoryProvider, downloadRepositoryProvider, downloadDaoProvider, saveSyncRepositoryProvider, saveSyncDaoProvider);
  }

  public static GameDetailViewModel newInstance(SavedStateHandle savedStateHandle,
      GameRepository gameRepository, DownloadRepository downloadRepository, DownloadDao downloadDao,
      SaveSyncRepository saveSyncRepository, SaveSyncDao saveSyncDao) {
    return new GameDetailViewModel(savedStateHandle, gameRepository, downloadRepository, downloadDao, saveSyncRepository, saveSyncDao);
  }
}

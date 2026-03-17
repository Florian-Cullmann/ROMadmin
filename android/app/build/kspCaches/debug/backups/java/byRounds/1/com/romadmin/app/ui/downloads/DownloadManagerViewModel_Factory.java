package com.romadmin.app.ui.downloads;

import com.romadmin.app.data.repository.DownloadRepository;
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
public final class DownloadManagerViewModel_Factory implements Factory<DownloadManagerViewModel> {
  private final Provider<DownloadRepository> downloadRepositoryProvider;

  public DownloadManagerViewModel_Factory(Provider<DownloadRepository> downloadRepositoryProvider) {
    this.downloadRepositoryProvider = downloadRepositoryProvider;
  }

  @Override
  public DownloadManagerViewModel get() {
    return newInstance(downloadRepositoryProvider.get());
  }

  public static DownloadManagerViewModel_Factory create(
      Provider<DownloadRepository> downloadRepositoryProvider) {
    return new DownloadManagerViewModel_Factory(downloadRepositoryProvider);
  }

  public static DownloadManagerViewModel newInstance(DownloadRepository downloadRepository) {
    return new DownloadManagerViewModel(downloadRepository);
  }
}

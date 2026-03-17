package com.romadmin.app.di;

import com.romadmin.app.data.local.AppDatabase;
import com.romadmin.app.data.local.dao.DownloadDao;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
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
public final class DatabaseModule_ProvideDownloadDaoFactory implements Factory<DownloadDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideDownloadDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public DownloadDao get() {
    return provideDownloadDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideDownloadDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideDownloadDaoFactory(dbProvider);
  }

  public static DownloadDao provideDownloadDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideDownloadDao(db));
  }
}

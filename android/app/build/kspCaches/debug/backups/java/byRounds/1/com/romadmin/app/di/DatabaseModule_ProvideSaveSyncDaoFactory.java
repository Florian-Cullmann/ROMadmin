package com.romadmin.app.di;

import com.romadmin.app.data.local.AppDatabase;
import com.romadmin.app.data.local.dao.SaveSyncDao;
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
public final class DatabaseModule_ProvideSaveSyncDaoFactory implements Factory<SaveSyncDao> {
  private final Provider<AppDatabase> dbProvider;

  public DatabaseModule_ProvideSaveSyncDaoFactory(Provider<AppDatabase> dbProvider) {
    this.dbProvider = dbProvider;
  }

  @Override
  public SaveSyncDao get() {
    return provideSaveSyncDao(dbProvider.get());
  }

  public static DatabaseModule_ProvideSaveSyncDaoFactory create(Provider<AppDatabase> dbProvider) {
    return new DatabaseModule_ProvideSaveSyncDaoFactory(dbProvider);
  }

  public static SaveSyncDao provideSaveSyncDao(AppDatabase db) {
    return Preconditions.checkNotNullFromProvides(DatabaseModule.INSTANCE.provideSaveSyncDao(db));
  }
}

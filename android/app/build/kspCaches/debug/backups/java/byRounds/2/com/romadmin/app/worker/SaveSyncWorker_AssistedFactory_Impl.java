package com.romadmin.app.worker;

import android.content.Context;
import androidx.work.WorkerParameters;
import dagger.internal.DaggerGenerated;
import dagger.internal.InstanceFactory;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class SaveSyncWorker_AssistedFactory_Impl implements SaveSyncWorker_AssistedFactory {
  private final SaveSyncWorker_Factory delegateFactory;

  SaveSyncWorker_AssistedFactory_Impl(SaveSyncWorker_Factory delegateFactory) {
    this.delegateFactory = delegateFactory;
  }

  @Override
  public SaveSyncWorker create(Context p0, WorkerParameters p1) {
    return delegateFactory.get(p0, p1);
  }

  public static Provider<SaveSyncWorker_AssistedFactory> create(
      SaveSyncWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SaveSyncWorker_AssistedFactory_Impl(delegateFactory));
  }

  public static dagger.internal.Provider<SaveSyncWorker_AssistedFactory> createFactoryProvider(
      SaveSyncWorker_Factory delegateFactory) {
    return InstanceFactory.create(new SaveSyncWorker_AssistedFactory_Impl(delegateFactory));
  }
}

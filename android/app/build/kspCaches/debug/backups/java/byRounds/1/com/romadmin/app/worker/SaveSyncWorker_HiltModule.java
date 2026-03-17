package com.romadmin.app.worker;

import androidx.hilt.work.WorkerAssistedFactory;
import androidx.work.ListenableWorker;
import dagger.Binds;
import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.codegen.OriginatingElement;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import javax.annotation.processing.Generated;

@Generated("androidx.hilt.AndroidXHiltProcessor")
@Module
@InstallIn(SingletonComponent.class)
@OriginatingElement(
    topLevelClass = SaveSyncWorker.class
)
public interface SaveSyncWorker_HiltModule {
  @Binds
  @IntoMap
  @StringKey("com.romadmin.app.worker.SaveSyncWorker")
  WorkerAssistedFactory<? extends ListenableWorker> bind(SaveSyncWorker_AssistedFactory factory);
}

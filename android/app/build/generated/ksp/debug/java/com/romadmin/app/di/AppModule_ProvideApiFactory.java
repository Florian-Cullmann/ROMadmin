package com.romadmin.app.di;

import com.romadmin.app.data.remote.RomAdminApi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import retrofit2.Retrofit;

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
public final class AppModule_ProvideApiFactory implements Factory<RomAdminApi> {
  private final Provider<Retrofit> retrofitProvider;

  public AppModule_ProvideApiFactory(Provider<Retrofit> retrofitProvider) {
    this.retrofitProvider = retrofitProvider;
  }

  @Override
  public RomAdminApi get() {
    return provideApi(retrofitProvider.get());
  }

  public static AppModule_ProvideApiFactory create(Provider<Retrofit> retrofitProvider) {
    return new AppModule_ProvideApiFactory(retrofitProvider);
  }

  public static RomAdminApi provideApi(Retrofit retrofit) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideApi(retrofit));
  }
}

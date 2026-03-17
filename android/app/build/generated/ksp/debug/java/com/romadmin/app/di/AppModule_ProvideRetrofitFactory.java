package com.romadmin.app.di;

import com.romadmin.app.data.preferences.AppPreferences;
import com.squareup.moshi.Moshi;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.Preconditions;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;
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
public final class AppModule_ProvideRetrofitFactory implements Factory<Retrofit> {
  private final Provider<OkHttpClient> okHttpClientProvider;

  private final Provider<Moshi> moshiProvider;

  private final Provider<AppPreferences> prefsProvider;

  public AppModule_ProvideRetrofitFactory(Provider<OkHttpClient> okHttpClientProvider,
      Provider<Moshi> moshiProvider, Provider<AppPreferences> prefsProvider) {
    this.okHttpClientProvider = okHttpClientProvider;
    this.moshiProvider = moshiProvider;
    this.prefsProvider = prefsProvider;
  }

  @Override
  public Retrofit get() {
    return provideRetrofit(okHttpClientProvider.get(), moshiProvider.get(), prefsProvider.get());
  }

  public static AppModule_ProvideRetrofitFactory create(Provider<OkHttpClient> okHttpClientProvider,
      Provider<Moshi> moshiProvider, Provider<AppPreferences> prefsProvider) {
    return new AppModule_ProvideRetrofitFactory(okHttpClientProvider, moshiProvider, prefsProvider);
  }

  public static Retrofit provideRetrofit(OkHttpClient okHttpClient, Moshi moshi,
      AppPreferences prefs) {
    return Preconditions.checkNotNullFromProvides(AppModule.INSTANCE.provideRetrofit(okHttpClient, moshi, prefs));
  }
}

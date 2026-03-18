package com.romadmin.app;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.view.View;
import androidx.fragment.app.Fragment;
import androidx.hilt.work.HiltWorkerFactory;
import androidx.hilt.work.WorkerAssistedFactory;
import androidx.hilt.work.WorkerFactoryModule_ProvideFactoryFactory;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;
import com.romadmin.app.data.local.AppDatabase;
import com.romadmin.app.data.local.dao.DownloadDao;
import com.romadmin.app.data.local.dao.SaveSyncDao;
import com.romadmin.app.data.preferences.AppPreferences;
import com.romadmin.app.data.remote.AuthInterceptor;
import com.romadmin.app.data.remote.RomAdminApi;
import com.romadmin.app.data.repository.AuthRepository;
import com.romadmin.app.data.repository.DownloadRepository;
import com.romadmin.app.data.repository.GameRepository;
import com.romadmin.app.data.repository.PlatformRepository;
import com.romadmin.app.data.repository.SaveSyncRepository;
import com.romadmin.app.di.AppModule_ProvideApiFactory;
import com.romadmin.app.di.AppModule_ProvideMoshiFactory;
import com.romadmin.app.di.AppModule_ProvideOkHttpClientFactory;
import com.romadmin.app.di.AppModule_ProvideRetrofitFactory;
import com.romadmin.app.di.DatabaseModule_ProvideDatabaseFactory;
import com.romadmin.app.di.DatabaseModule_ProvideDownloadDaoFactory;
import com.romadmin.app.di.DatabaseModule_ProvideSaveSyncDaoFactory;
import com.romadmin.app.ui.downloads.DownloadManagerViewModel;
import com.romadmin.app.ui.downloads.DownloadManagerViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.ui.games.GameDetailViewModel;
import com.romadmin.app.ui.games.GameDetailViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.ui.games.GameListViewModel;
import com.romadmin.app.ui.games.GameListViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.ui.home.HomeViewModel;
import com.romadmin.app.ui.home.HomeViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.ui.navigation.NavViewModel;
import com.romadmin.app.ui.navigation.NavViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.ui.platforms.PlatformListViewModel;
import com.romadmin.app.ui.platforms.PlatformListViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.ui.settings.SettingsViewModel;
import com.romadmin.app.ui.settings.SettingsViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.ui.setup.SetupViewModel;
import com.romadmin.app.ui.setup.SetupViewModel_HiltModules_KeyModule_ProvideFactory;
import com.romadmin.app.worker.DownloadWorker;
import com.romadmin.app.worker.DownloadWorker_AssistedFactory;
import com.romadmin.app.worker.SaveSyncWorker;
import com.romadmin.app.worker.SaveSyncWorker_AssistedFactory;
import com.squareup.moshi.Moshi;
import dagger.hilt.android.ActivityRetainedLifecycle;
import dagger.hilt.android.ViewModelLifecycle;
import dagger.hilt.android.internal.builders.ActivityComponentBuilder;
import dagger.hilt.android.internal.builders.ActivityRetainedComponentBuilder;
import dagger.hilt.android.internal.builders.FragmentComponentBuilder;
import dagger.hilt.android.internal.builders.ServiceComponentBuilder;
import dagger.hilt.android.internal.builders.ViewComponentBuilder;
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder;
import dagger.hilt.android.internal.builders.ViewWithFragmentComponentBuilder;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories;
import dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories_InternalFactoryFactory_Factory;
import dagger.hilt.android.internal.managers.ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory;
import dagger.hilt.android.internal.managers.SavedStateHandleHolder;
import dagger.hilt.android.internal.modules.ApplicationContextModule;
import dagger.hilt.android.internal.modules.ApplicationContextModule_ProvideContextFactory;
import dagger.internal.DaggerGenerated;
import dagger.internal.DoubleCheck;
import dagger.internal.MapBuilder;
import dagger.internal.Preconditions;
import dagger.internal.Provider;
import dagger.internal.SetBuilder;
import dagger.internal.SingleCheck;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

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
public final class DaggerRomAdminApp_HiltComponents_SingletonC {
  private DaggerRomAdminApp_HiltComponents_SingletonC() {
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {
    private ApplicationContextModule applicationContextModule;

    private Builder() {
    }

    public Builder applicationContextModule(ApplicationContextModule applicationContextModule) {
      this.applicationContextModule = Preconditions.checkNotNull(applicationContextModule);
      return this;
    }

    public RomAdminApp_HiltComponents.SingletonC build() {
      Preconditions.checkBuilderRequirement(applicationContextModule, ApplicationContextModule.class);
      return new SingletonCImpl(applicationContextModule);
    }
  }

  private static final class ActivityRetainedCBuilder implements RomAdminApp_HiltComponents.ActivityRetainedC.Builder {
    private final SingletonCImpl singletonCImpl;

    private SavedStateHandleHolder savedStateHandleHolder;

    private ActivityRetainedCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ActivityRetainedCBuilder savedStateHandleHolder(
        SavedStateHandleHolder savedStateHandleHolder) {
      this.savedStateHandleHolder = Preconditions.checkNotNull(savedStateHandleHolder);
      return this;
    }

    @Override
    public RomAdminApp_HiltComponents.ActivityRetainedC build() {
      Preconditions.checkBuilderRequirement(savedStateHandleHolder, SavedStateHandleHolder.class);
      return new ActivityRetainedCImpl(singletonCImpl, savedStateHandleHolder);
    }
  }

  private static final class ActivityCBuilder implements RomAdminApp_HiltComponents.ActivityC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private Activity activity;

    private ActivityCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ActivityCBuilder activity(Activity activity) {
      this.activity = Preconditions.checkNotNull(activity);
      return this;
    }

    @Override
    public RomAdminApp_HiltComponents.ActivityC build() {
      Preconditions.checkBuilderRequirement(activity, Activity.class);
      return new ActivityCImpl(singletonCImpl, activityRetainedCImpl, activity);
    }
  }

  private static final class FragmentCBuilder implements RomAdminApp_HiltComponents.FragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private Fragment fragment;

    private FragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public FragmentCBuilder fragment(Fragment fragment) {
      this.fragment = Preconditions.checkNotNull(fragment);
      return this;
    }

    @Override
    public RomAdminApp_HiltComponents.FragmentC build() {
      Preconditions.checkBuilderRequirement(fragment, Fragment.class);
      return new FragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragment);
    }
  }

  private static final class ViewWithFragmentCBuilder implements RomAdminApp_HiltComponents.ViewWithFragmentC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private View view;

    private ViewWithFragmentCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;
    }

    @Override
    public ViewWithFragmentCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public RomAdminApp_HiltComponents.ViewWithFragmentC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewWithFragmentCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl, view);
    }
  }

  private static final class ViewCBuilder implements RomAdminApp_HiltComponents.ViewC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private View view;

    private ViewCBuilder(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
    }

    @Override
    public ViewCBuilder view(View view) {
      this.view = Preconditions.checkNotNull(view);
      return this;
    }

    @Override
    public RomAdminApp_HiltComponents.ViewC build() {
      Preconditions.checkBuilderRequirement(view, View.class);
      return new ViewCImpl(singletonCImpl, activityRetainedCImpl, activityCImpl, view);
    }
  }

  private static final class ViewModelCBuilder implements RomAdminApp_HiltComponents.ViewModelC.Builder {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private SavedStateHandle savedStateHandle;

    private ViewModelLifecycle viewModelLifecycle;

    private ViewModelCBuilder(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
    }

    @Override
    public ViewModelCBuilder savedStateHandle(SavedStateHandle handle) {
      this.savedStateHandle = Preconditions.checkNotNull(handle);
      return this;
    }

    @Override
    public ViewModelCBuilder viewModelLifecycle(ViewModelLifecycle viewModelLifecycle) {
      this.viewModelLifecycle = Preconditions.checkNotNull(viewModelLifecycle);
      return this;
    }

    @Override
    public RomAdminApp_HiltComponents.ViewModelC build() {
      Preconditions.checkBuilderRequirement(savedStateHandle, SavedStateHandle.class);
      Preconditions.checkBuilderRequirement(viewModelLifecycle, ViewModelLifecycle.class);
      return new ViewModelCImpl(singletonCImpl, activityRetainedCImpl, savedStateHandle, viewModelLifecycle);
    }
  }

  private static final class ServiceCBuilder implements RomAdminApp_HiltComponents.ServiceC.Builder {
    private final SingletonCImpl singletonCImpl;

    private Service service;

    private ServiceCBuilder(SingletonCImpl singletonCImpl) {
      this.singletonCImpl = singletonCImpl;
    }

    @Override
    public ServiceCBuilder service(Service service) {
      this.service = Preconditions.checkNotNull(service);
      return this;
    }

    @Override
    public RomAdminApp_HiltComponents.ServiceC build() {
      Preconditions.checkBuilderRequirement(service, Service.class);
      return new ServiceCImpl(singletonCImpl, service);
    }
  }

  private static final class ViewWithFragmentCImpl extends RomAdminApp_HiltComponents.ViewWithFragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl;

    private final ViewWithFragmentCImpl viewWithFragmentCImpl = this;

    private ViewWithFragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        FragmentCImpl fragmentCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;
      this.fragmentCImpl = fragmentCImpl;


    }
  }

  private static final class FragmentCImpl extends RomAdminApp_HiltComponents.FragmentC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final FragmentCImpl fragmentCImpl = this;

    private FragmentCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, ActivityCImpl activityCImpl,
        Fragment fragmentParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return activityCImpl.getHiltInternalFactoryFactory();
    }

    @Override
    public ViewWithFragmentComponentBuilder viewWithFragmentComponentBuilder() {
      return new ViewWithFragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl, fragmentCImpl);
    }
  }

  private static final class ViewCImpl extends RomAdminApp_HiltComponents.ViewC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl;

    private final ViewCImpl viewCImpl = this;

    private ViewCImpl(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
        ActivityCImpl activityCImpl, View viewParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.activityCImpl = activityCImpl;


    }
  }

  private static final class ActivityCImpl extends RomAdminApp_HiltComponents.ActivityC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ActivityCImpl activityCImpl = this;

    private ActivityCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, Activity activityParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;


    }

    @Override
    public void injectMainActivity(MainActivity mainActivity) {
    }

    @Override
    public DefaultViewModelFactories.InternalFactoryFactory getHiltInternalFactoryFactory() {
      return DefaultViewModelFactories_InternalFactoryFactory_Factory.newInstance(getViewModelKeys(), new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl));
    }

    @Override
    public Set<String> getViewModelKeys() {
      return SetBuilder.<String>newSetBuilder(8).add(DownloadManagerViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(GameDetailViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(GameListViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(HomeViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(NavViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(PlatformListViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(SettingsViewModel_HiltModules_KeyModule_ProvideFactory.provide()).add(SetupViewModel_HiltModules_KeyModule_ProvideFactory.provide()).build();
    }

    @Override
    public ViewModelComponentBuilder getViewModelComponentBuilder() {
      return new ViewModelCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public FragmentComponentBuilder fragmentComponentBuilder() {
      return new FragmentCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }

    @Override
    public ViewComponentBuilder viewComponentBuilder() {
      return new ViewCBuilder(singletonCImpl, activityRetainedCImpl, activityCImpl);
    }
  }

  private static final class ViewModelCImpl extends RomAdminApp_HiltComponents.ViewModelC {
    private final SavedStateHandle savedStateHandle;

    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl;

    private final ViewModelCImpl viewModelCImpl = this;

    private Provider<DownloadManagerViewModel> downloadManagerViewModelProvider;

    private Provider<GameDetailViewModel> gameDetailViewModelProvider;

    private Provider<GameListViewModel> gameListViewModelProvider;

    private Provider<HomeViewModel> homeViewModelProvider;

    private Provider<NavViewModel> navViewModelProvider;

    private Provider<PlatformListViewModel> platformListViewModelProvider;

    private Provider<SettingsViewModel> settingsViewModelProvider;

    private Provider<SetupViewModel> setupViewModelProvider;

    private ViewModelCImpl(SingletonCImpl singletonCImpl,
        ActivityRetainedCImpl activityRetainedCImpl, SavedStateHandle savedStateHandleParam,
        ViewModelLifecycle viewModelLifecycleParam) {
      this.singletonCImpl = singletonCImpl;
      this.activityRetainedCImpl = activityRetainedCImpl;
      this.savedStateHandle = savedStateHandleParam;
      initialize(savedStateHandleParam, viewModelLifecycleParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandle savedStateHandleParam,
        final ViewModelLifecycle viewModelLifecycleParam) {
      this.downloadManagerViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 0);
      this.gameDetailViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 1);
      this.gameListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 2);
      this.homeViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 3);
      this.navViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 4);
      this.platformListViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 5);
      this.settingsViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 6);
      this.setupViewModelProvider = new SwitchingProvider<>(singletonCImpl, activityRetainedCImpl, viewModelCImpl, 7);
    }

    @Override
    public Map<String, javax.inject.Provider<ViewModel>> getHiltViewModelMap() {
      return MapBuilder.<String, javax.inject.Provider<ViewModel>>newMapBuilder(8).put("com.romadmin.app.ui.downloads.DownloadManagerViewModel", ((Provider) downloadManagerViewModelProvider)).put("com.romadmin.app.ui.games.GameDetailViewModel", ((Provider) gameDetailViewModelProvider)).put("com.romadmin.app.ui.games.GameListViewModel", ((Provider) gameListViewModelProvider)).put("com.romadmin.app.ui.home.HomeViewModel", ((Provider) homeViewModelProvider)).put("com.romadmin.app.ui.navigation.NavViewModel", ((Provider) navViewModelProvider)).put("com.romadmin.app.ui.platforms.PlatformListViewModel", ((Provider) platformListViewModelProvider)).put("com.romadmin.app.ui.settings.SettingsViewModel", ((Provider) settingsViewModelProvider)).put("com.romadmin.app.ui.setup.SetupViewModel", ((Provider) setupViewModelProvider)).build();
    }

    @Override
    public Map<String, Object> getHiltViewModelAssistedMap() {
      return Collections.<String, Object>emptyMap();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final ViewModelCImpl viewModelCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          ViewModelCImpl viewModelCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.viewModelCImpl = viewModelCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.romadmin.app.ui.downloads.DownloadManagerViewModel 
          return (T) new DownloadManagerViewModel(singletonCImpl.downloadRepositoryProvider.get());

          case 1: // com.romadmin.app.ui.games.GameDetailViewModel 
          return (T) new GameDetailViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.gameRepositoryProvider.get(), singletonCImpl.downloadRepositoryProvider.get(), singletonCImpl.downloadDao(), singletonCImpl.saveSyncRepositoryProvider.get(), singletonCImpl.saveSyncDao());

          case 2: // com.romadmin.app.ui.games.GameListViewModel 
          return (T) new GameListViewModel(viewModelCImpl.savedStateHandle, singletonCImpl.gameRepositoryProvider.get(), singletonCImpl.platformRepositoryProvider.get(), singletonCImpl.downloadRepositoryProvider.get(), singletonCImpl.downloadDao());

          case 3: // com.romadmin.app.ui.home.HomeViewModel 
          return (T) new HomeViewModel(singletonCImpl.downloadDao(), singletonCImpl.saveSyncDao(), singletonCImpl.saveSyncRepositoryProvider.get());

          case 4: // com.romadmin.app.ui.navigation.NavViewModel 
          return (T) new NavViewModel(singletonCImpl.appPreferencesProvider.get());

          case 5: // com.romadmin.app.ui.platforms.PlatformListViewModel 
          return (T) new PlatformListViewModel(singletonCImpl.platformRepositoryProvider.get());

          case 6: // com.romadmin.app.ui.settings.SettingsViewModel 
          return (T) new SettingsViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.appPreferencesProvider.get(), singletonCImpl.saveSyncRepositoryProvider.get(), singletonCImpl.authRepositoryProvider.get());

          case 7: // com.romadmin.app.ui.setup.SetupViewModel 
          return (T) new SetupViewModel(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.authRepositoryProvider.get(), singletonCImpl.appPreferencesProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ActivityRetainedCImpl extends RomAdminApp_HiltComponents.ActivityRetainedC {
    private final SingletonCImpl singletonCImpl;

    private final ActivityRetainedCImpl activityRetainedCImpl = this;

    private Provider<ActivityRetainedLifecycle> provideActivityRetainedLifecycleProvider;

    private ActivityRetainedCImpl(SingletonCImpl singletonCImpl,
        SavedStateHandleHolder savedStateHandleHolderParam) {
      this.singletonCImpl = singletonCImpl;

      initialize(savedStateHandleHolderParam);

    }

    @SuppressWarnings("unchecked")
    private void initialize(final SavedStateHandleHolder savedStateHandleHolderParam) {
      this.provideActivityRetainedLifecycleProvider = DoubleCheck.provider(new SwitchingProvider<ActivityRetainedLifecycle>(singletonCImpl, activityRetainedCImpl, 0));
    }

    @Override
    public ActivityComponentBuilder activityComponentBuilder() {
      return new ActivityCBuilder(singletonCImpl, activityRetainedCImpl);
    }

    @Override
    public ActivityRetainedLifecycle getActivityRetainedLifecycle() {
      return provideActivityRetainedLifecycleProvider.get();
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final ActivityRetainedCImpl activityRetainedCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, ActivityRetainedCImpl activityRetainedCImpl,
          int id) {
        this.singletonCImpl = singletonCImpl;
        this.activityRetainedCImpl = activityRetainedCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // dagger.hilt.android.ActivityRetainedLifecycle 
          return (T) ActivityRetainedComponentManager_LifecycleModule_ProvideActivityRetainedLifecycleFactory.provideActivityRetainedLifecycle();

          default: throw new AssertionError(id);
        }
      }
    }
  }

  private static final class ServiceCImpl extends RomAdminApp_HiltComponents.ServiceC {
    private final SingletonCImpl singletonCImpl;

    private final ServiceCImpl serviceCImpl = this;

    private ServiceCImpl(SingletonCImpl singletonCImpl, Service serviceParam) {
      this.singletonCImpl = singletonCImpl;


    }
  }

  private static final class SingletonCImpl extends RomAdminApp_HiltComponents.SingletonC {
    private final ApplicationContextModule applicationContextModule;

    private final SingletonCImpl singletonCImpl = this;

    private Provider<AppPreferences> appPreferencesProvider;

    private Provider<AuthInterceptor> authInterceptorProvider;

    private Provider<OkHttpClient> provideOkHttpClientProvider;

    private Provider<Moshi> provideMoshiProvider;

    private Provider<Retrofit> provideRetrofitProvider;

    private Provider<RomAdminApi> provideApiProvider;

    private Provider<AppDatabase> provideDatabaseProvider;

    private Provider<DownloadWorker_AssistedFactory> downloadWorker_AssistedFactoryProvider;

    private Provider<SaveSyncRepository> saveSyncRepositoryProvider;

    private Provider<SaveSyncWorker_AssistedFactory> saveSyncWorker_AssistedFactoryProvider;

    private Provider<DownloadRepository> downloadRepositoryProvider;

    private Provider<GameRepository> gameRepositoryProvider;

    private Provider<PlatformRepository> platformRepositoryProvider;

    private Provider<AuthRepository> authRepositoryProvider;

    private SingletonCImpl(ApplicationContextModule applicationContextModuleParam) {
      this.applicationContextModule = applicationContextModuleParam;
      initialize(applicationContextModuleParam);

    }

    private DownloadDao downloadDao() {
      return DatabaseModule_ProvideDownloadDaoFactory.provideDownloadDao(provideDatabaseProvider.get());
    }

    private SaveSyncDao saveSyncDao() {
      return DatabaseModule_ProvideSaveSyncDaoFactory.provideSaveSyncDao(provideDatabaseProvider.get());
    }

    private Map<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>> mapOfStringAndProviderOfWorkerAssistedFactoryOf(
        ) {
      return MapBuilder.<String, javax.inject.Provider<WorkerAssistedFactory<? extends ListenableWorker>>>newMapBuilder(2).put("com.romadmin.app.worker.DownloadWorker", ((Provider) downloadWorker_AssistedFactoryProvider)).put("com.romadmin.app.worker.SaveSyncWorker", ((Provider) saveSyncWorker_AssistedFactoryProvider)).build();
    }

    private HiltWorkerFactory hiltWorkerFactory() {
      return WorkerFactoryModule_ProvideFactoryFactory.provideFactory(mapOfStringAndProviderOfWorkerAssistedFactoryOf());
    }

    @SuppressWarnings("unchecked")
    private void initialize(final ApplicationContextModule applicationContextModuleParam) {
      this.appPreferencesProvider = DoubleCheck.provider(new SwitchingProvider<AppPreferences>(singletonCImpl, 5));
      this.authInterceptorProvider = DoubleCheck.provider(new SwitchingProvider<AuthInterceptor>(singletonCImpl, 4));
      this.provideOkHttpClientProvider = DoubleCheck.provider(new SwitchingProvider<OkHttpClient>(singletonCImpl, 3));
      this.provideMoshiProvider = DoubleCheck.provider(new SwitchingProvider<Moshi>(singletonCImpl, 6));
      this.provideRetrofitProvider = DoubleCheck.provider(new SwitchingProvider<Retrofit>(singletonCImpl, 2));
      this.provideApiProvider = DoubleCheck.provider(new SwitchingProvider<RomAdminApi>(singletonCImpl, 1));
      this.provideDatabaseProvider = DoubleCheck.provider(new SwitchingProvider<AppDatabase>(singletonCImpl, 7));
      this.downloadWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<DownloadWorker_AssistedFactory>(singletonCImpl, 0));
      this.saveSyncRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<SaveSyncRepository>(singletonCImpl, 9));
      this.saveSyncWorker_AssistedFactoryProvider = SingleCheck.provider(new SwitchingProvider<SaveSyncWorker_AssistedFactory>(singletonCImpl, 8));
      this.downloadRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<DownloadRepository>(singletonCImpl, 10));
      this.gameRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<GameRepository>(singletonCImpl, 11));
      this.platformRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<PlatformRepository>(singletonCImpl, 12));
      this.authRepositoryProvider = DoubleCheck.provider(new SwitchingProvider<AuthRepository>(singletonCImpl, 13));
    }

    @Override
    public void injectRomAdminApp(RomAdminApp romAdminApp) {
      injectRomAdminApp2(romAdminApp);
    }

    @Override
    public Set<Boolean> getDisableFragmentGetContextFix() {
      return Collections.<Boolean>emptySet();
    }

    @Override
    public ActivityRetainedComponentBuilder retainedComponentBuilder() {
      return new ActivityRetainedCBuilder(singletonCImpl);
    }

    @Override
    public ServiceComponentBuilder serviceComponentBuilder() {
      return new ServiceCBuilder(singletonCImpl);
    }

    private RomAdminApp injectRomAdminApp2(RomAdminApp instance) {
      RomAdminApp_MembersInjector.injectWorkerFactory(instance, hiltWorkerFactory());
      RomAdminApp_MembersInjector.injectPrefs(instance, appPreferencesProvider.get());
      return instance;
    }

    private static final class SwitchingProvider<T> implements Provider<T> {
      private final SingletonCImpl singletonCImpl;

      private final int id;

      SwitchingProvider(SingletonCImpl singletonCImpl, int id) {
        this.singletonCImpl = singletonCImpl;
        this.id = id;
      }

      @SuppressWarnings("unchecked")
      @Override
      public T get() {
        switch (id) {
          case 0: // com.romadmin.app.worker.DownloadWorker_AssistedFactory 
          return (T) new DownloadWorker_AssistedFactory() {
            @Override
            public DownloadWorker create(Context appContext, WorkerParameters params) {
              return new DownloadWorker(appContext, params, singletonCImpl.provideApiProvider.get(), singletonCImpl.downloadDao());
            }
          };

          case 1: // com.romadmin.app.data.remote.RomAdminApi 
          return (T) AppModule_ProvideApiFactory.provideApi(singletonCImpl.provideRetrofitProvider.get());

          case 2: // retrofit2.Retrofit 
          return (T) AppModule_ProvideRetrofitFactory.provideRetrofit(singletonCImpl.provideOkHttpClientProvider.get(), singletonCImpl.provideMoshiProvider.get(), singletonCImpl.appPreferencesProvider.get());

          case 3: // okhttp3.OkHttpClient 
          return (T) AppModule_ProvideOkHttpClientFactory.provideOkHttpClient(singletonCImpl.authInterceptorProvider.get());

          case 4: // com.romadmin.app.data.remote.AuthInterceptor 
          return (T) new AuthInterceptor(singletonCImpl.appPreferencesProvider.get());

          case 5: // com.romadmin.app.data.preferences.AppPreferences 
          return (T) new AppPreferences(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 6: // com.squareup.moshi.Moshi 
          return (T) AppModule_ProvideMoshiFactory.provideMoshi();

          case 7: // com.romadmin.app.data.local.AppDatabase 
          return (T) DatabaseModule_ProvideDatabaseFactory.provideDatabase(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule));

          case 8: // com.romadmin.app.worker.SaveSyncWorker_AssistedFactory 
          return (T) new SaveSyncWorker_AssistedFactory() {
            @Override
            public SaveSyncWorker create(Context appContext2, WorkerParameters params2) {
              return new SaveSyncWorker(appContext2, params2, singletonCImpl.saveSyncRepositoryProvider.get());
            }
          };

          case 9: // com.romadmin.app.data.repository.SaveSyncRepository 
          return (T) new SaveSyncRepository(singletonCImpl.provideApiProvider.get(), singletonCImpl.downloadDao(), singletonCImpl.saveSyncDao(), singletonCImpl.appPreferencesProvider.get());

          case 10: // com.romadmin.app.data.repository.DownloadRepository 
          return (T) new DownloadRepository(ApplicationContextModule_ProvideContextFactory.provideContext(singletonCImpl.applicationContextModule), singletonCImpl.provideApiProvider.get(), singletonCImpl.downloadDao(), singletonCImpl.appPreferencesProvider.get());

          case 11: // com.romadmin.app.data.repository.GameRepository 
          return (T) new GameRepository(singletonCImpl.provideApiProvider.get());

          case 12: // com.romadmin.app.data.repository.PlatformRepository 
          return (T) new PlatformRepository(singletonCImpl.provideApiProvider.get());

          case 13: // com.romadmin.app.data.repository.AuthRepository 
          return (T) new AuthRepository(singletonCImpl.appPreferencesProvider.get(), singletonCImpl.provideMoshiProvider.get());

          default: throw new AssertionError(id);
        }
      }
    }
  }
}

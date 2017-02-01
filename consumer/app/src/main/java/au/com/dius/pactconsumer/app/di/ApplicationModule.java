package au.com.dius.pactconsumer.app.di;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import au.com.dius.pactconsumer.data.Repository;
import au.com.dius.pactconsumer.data.Service;
import dagger.Module;
import dagger.Provides;
import retrofit2.Retrofit;

@Module
public class ApplicationModule {

  private final Context context;

  public ApplicationModule(@NonNull Context context) {
    this.context = context;
  }

  @Singleton
  @Provides
  @NonNull
  public Context getContext() {
    return context;
  }

  @Singleton
  @Provides
  @NonNull
  public Repository getRepository(@NonNull Retrofit retrofit) {
    return new Service(retrofit.create(Service.Api.class));
  }

}

package au.com.dius.pactconsumer.app.di;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.squareup.moshi.Moshi;

import javax.inject.Singleton;

import au.com.dius.pactconsumer.BuildConfig;
import au.com.dius.pactconsumer.data.FakeService;
import au.com.dius.pactconsumer.data.Repository;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

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
  public Retrofit getRetrofit() {
    return new Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(getOkHttpClient())
        .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build();
  }

  private OkHttpClient getOkHttpClient() {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (BuildConfig.DEBUG) {
      HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
      interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
      builder.addNetworkInterceptor(interceptor);
    }
    return builder.build();
  }

  private Moshi getMoshi() {
    return new Moshi.Builder().build();
  }

  @Singleton
  @Provides
  @NonNull
  public Repository getRepository() {
    return new FakeService();
  }
}

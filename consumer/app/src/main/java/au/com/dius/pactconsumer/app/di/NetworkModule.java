package au.com.dius.pactconsumer.app.di;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.io.File;

import javax.inject.Singleton;

import au.com.dius.pactconsumer.BuildConfig;
import au.com.dius.pactconsumer.util.Serializer;
import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


@Module
public class NetworkModule {

  @Singleton
  @Provides
  @NonNull
  public Retrofit getRetrofit(@NonNull Context context) {
    return getRetrofit(context, BuildConfig.BASE_URL);
  }

  @VisibleForTesting
  public Retrofit getRetrofit(@NonNull Context context,
                              @NonNull String baseUrl) {
    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(getOkHttpClient(context))
        .addConverterFactory(ScalarsConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build();
  }

  private OkHttpClient getOkHttpClient(@NonNull Context context) {
    OkHttpClient.Builder builder = new OkHttpClient.Builder();
    if (BuildConfig.DEBUG) {
      HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
      interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
      builder.addNetworkInterceptor(interceptor);
    }
    builder.cache(new Cache(new File(context.getCacheDir(), "okhttp_cache"), 10 * 1024 * 1024));
    return builder.build();
  }

  @Singleton
  @Provides
  @NonNull
  public Serializer getSerializer() {
    return new Serializer();
  }

}

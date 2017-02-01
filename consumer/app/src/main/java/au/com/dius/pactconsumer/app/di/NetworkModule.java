package au.com.dius.pactconsumer.app.di;

import android.support.annotation.NonNull;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import javax.inject.Singleton;

import au.com.dius.pactconsumer.BuildConfig;
import au.com.dius.pactconsumer.util.Serializer;
import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;


@Module
public class NetworkModule {

  @Singleton
  @Provides
  @NonNull
  public Retrofit getRetrofit() {
    return getRetrofit(BuildConfig.BASE_URL);
  }

  public Retrofit getRetrofit(@NonNull String baseUrl) {
    return new Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(getOkHttpClient())
        .addConverterFactory(ScalarsConverterFactory.create())
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
    // TODO add cache
    return builder.build();
  }

  @Singleton
  @Provides
  @NonNull
  public Serializer getSerializer() {
    return new Serializer();
  }

}

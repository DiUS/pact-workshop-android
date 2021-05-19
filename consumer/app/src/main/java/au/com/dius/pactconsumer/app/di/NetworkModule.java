package au.com.dius.pactconsumer.app.di;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.squareup.moshi.FromJson;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;

import org.joda.time.DateTime;

import java.io.File;

import javax.inject.Singleton;

import au.com.dius.pactconsumer.BuildConfig;
import au.com.dius.pactconsumer.util.DateHelper;
import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;


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
        .addConverterFactory(MoshiConverterFactory.create(getMoshi()))
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

  private Moshi getMoshi() {
    return new Moshi.Builder().add(new DateTimeAdapter()).build();
  }

  public static class DateTimeAdapter {

    @ToJson
    public String toJson(DateTime dateTime) {
      return DateHelper.toString(dateTime);
    }

    @FromJson
    public DateTime fromJson(String json) {
      return DateHelper.parse(json);
    }
  }

}

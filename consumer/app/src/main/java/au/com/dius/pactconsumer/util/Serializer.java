package au.com.dius.pactconsumer.util;

import android.support.annotation.NonNull;

import com.squareup.moshi.FromJson;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.ToJson;

import org.joda.time.DateTime;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton
public class Serializer {

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

  private final Moshi moshi;

  @Inject
  public Serializer() {
    moshi = new Moshi.Builder()
        .add(new DateTimeAdapter())
        .build();
  }

  public <T> T fromJson(Class<T> classType, @NonNull String json) throws IOException {
    return moshi.adapter(classType).fromJson(json);
  }

  public <T> String toJson(Class<T> classType, @NonNull T object) {
    return moshi.adapter(classType).toJson(object);
  }

}

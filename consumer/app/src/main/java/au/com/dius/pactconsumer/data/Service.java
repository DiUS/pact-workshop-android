package au.com.dius.pactconsumer.data;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.DateHelper;
import au.com.dius.pactconsumer.util.Serializer;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

@Singleton
public class Service implements Repository {

  public interface Api {
    @GET("provider.json")
    Single<String> loadProviderJson(@Query("valid_date") String validDate);
  }

  private final Api api;
  private final Serializer serializer;

  @Inject
  public Service(@NonNull Api api, @NonNull Serializer serializer) {
    this.api = api;
    this.serializer = serializer;
  }

  @NonNull
  @Override
  public Single<ServiceResponse> fetchResponse(@NonNull DateTime dateTime) {
    try {
      return api.loadProviderJson(DateHelper.encodedDate(dateTime))
          .map(json -> serializer.fromJson(ServiceResponse.class, json));
    } catch (UnsupportedEncodingException e) {
      return Single.error(e);
    }
  }
}

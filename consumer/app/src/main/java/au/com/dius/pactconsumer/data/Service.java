package au.com.dius.pactconsumer.data;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.DateHelper;
import io.reactivex.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

@Singleton
public class Service implements Repository {

  public interface Api {
    @GET("provider.json")
    Single<ServiceResponse> loadProviderJson(@Query("valid_date") String validDate);
  }

  private final Api api;

  @Inject
  public Service(@NonNull Api api) {
    this.api = api;
  }

  @NonNull
  @Override
  public Single<ServiceResponse> fetchResponse(@NonNull DateTime dateTime) {
    try {
      return api.loadProviderJson(DateHelper.encodeDate(dateTime));
    } catch (UnsupportedEncodingException e) {
      return Single.error(e);
    }
  }

}

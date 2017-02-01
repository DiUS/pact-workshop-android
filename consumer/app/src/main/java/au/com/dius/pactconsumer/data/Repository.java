package au.com.dius.pactconsumer.data;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import au.com.dius.pactconsumer.data.model.ServiceResponse;
import io.reactivex.Single;

public interface Repository {

  @NonNull
  Single<ServiceResponse> fetchResponse(@NonNull DateTime dateTime);

}

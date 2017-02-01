package au.com.dius.pactconsumer.data;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import io.reactivex.Single;

@Singleton
public class FakeService implements Repository {

  public static final ServiceResponse RESPONSE;

  static {
    RESPONSE = new ServiceResponse(
        "NO",
        DateTime.now(),
        Arrays.asList(
            Animal.create("Doggy", "dog"),
            Animal.create("Cathy", "cat"),
            Animal.create("Birdy", "bird")
        )
    );
  }

  @Inject
  public FakeService() {
  }

  @NonNull
  @Override
  public Single<ServiceResponse> fetchResponse(@NonNull DateTime dateTime) {
    return Single.just(RESPONSE);
  }

}

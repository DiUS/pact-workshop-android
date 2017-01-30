package au.com.dius.pactconsumer.data;

import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.dius.pactconsumer.data.model.Animal;
import io.reactivex.Single;

@Singleton
public class FakeService implements Repository {

  private static final List<Animal> ANIMALS_LIST;

  static {
      ANIMALS_LIST = Arrays.asList();
  }

  @Inject
  public FakeService() {
  }

  @NonNull
  @Override
  public Single<List<Animal>> getAnimals() {
    return Single.just(ANIMALS_LIST);
  }

}

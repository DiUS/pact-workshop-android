package au.com.dius.pactconsumer.data;

import android.support.annotation.NonNull;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import au.com.dius.pactconsumer.data.model.Animal;
import io.reactivex.Single;

@Singleton
public class Service implements Repository {

  @Inject
  public Service() {
  }

  @NonNull
  @Override
  public Single<List<Animal>> getAnimals() {
    return null;
  }
}

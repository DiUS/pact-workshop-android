package au.com.dius.pactconsumer.data;

import android.support.annotation.NonNull;

import java.util.List;

import au.com.dius.pactconsumer.data.model.Animal;
import io.reactivex.Single;

public interface Repository {

  @NonNull
  Single<List<Animal>> getAnimals();

}

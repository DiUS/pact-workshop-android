package au.com.dius.pactconsumer.domain;

import android.support.annotation.NonNull;

import java.util.List;

import au.com.dius.pactconsumer.data.model.Animal;

public class ViewState {

  static class Loaded extends ViewState {

    @NonNull
    private final List<Animal> animals;

    Loaded(@NonNull List<Animal> animals) {
      this.animals = animals;
    }

    @NonNull
    public List<Animal> getAnimals() {
      return animals;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Loaded loaded = (Loaded) o;

      return animals.equals(loaded.animals);

    }

    @Override
    public int hashCode() {
      return animals.hashCode();
    }

    @Override
    public String toString() {
      return "Loaded{" +
          "animals=" + animals +
          '}';
    }

  }

  static class Loading extends ViewState {

    @Override
    public String toString() {
      return "Loading{}";
    }

  }

  static class Error extends ViewState {

    @NonNull
    private final String message;

    Error(@NonNull String message) {
      this.message = message;
    }

    @NonNull
    public String getMessage() {
      return message;
    }

  }

}

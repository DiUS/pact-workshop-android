package au.com.dius.pactconsumer.domain;

import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import java.util.List;

import au.com.dius.pactconsumer.data.model.Animal;

public class ViewState {

  public static class Loaded extends ViewState {

    @NonNull
    private final List<Animal> animals;

    private Loaded(@NonNull List<Animal> animals) {
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

    public static Loaded create(@NonNull List<Animal> animals) {
      return new Loaded(animals);
    }

  }

  public static class Loading extends ViewState {

    private static final Loading instance = new Loading();

    @Override
    public String toString() {
      return "Loading{}";
    }

    public static Loading create() {
      return instance;
    }

  }

  public static class Empty extends ViewState {

    private final @StringRes int messageRes;

    private Empty(@StringRes int messageRes) {
      this.messageRes = messageRes;
    }

    @StringRes
    public int getMessage() {
      return messageRes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Empty empty = (Empty) o;

      return messageRes == empty.messageRes;

    }

    @Override
    public int hashCode() {
      return messageRes;
    }

    @Override
    public String toString() {
      return "Empty{" +
          "messageRes=" + messageRes +
          '}';
    }

    public static Empty create(@StringRes int messageRes) {
      return new Empty(messageRes);
    }
  }

  public static class Error extends ViewState {

    private final @StringRes int messageRes;

    private Error(@StringRes int messageRes) {
      this.messageRes = messageRes;
    }

    @StringRes
    public int getMessage() {
      return messageRes;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Error error = (Error) o;

      return messageRes == error.messageRes;

    }

    @Override
    public int hashCode() {
      return messageRes;
    }

    @Override
    public String toString() {
      return "Error{" +
          "messageRes=" + messageRes +
          '}';
    }

    public static Error create(@StringRes int messageRes) {
      return new Error(messageRes);
    }
  }

}

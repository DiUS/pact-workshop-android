package au.com.dius.pactconsumer.data.model;


import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

public class Animal {

  @Json(name = "name")
  private final String name;

  @Json(name = "type")
  private final String type;

  public Animal(@NonNull String name, @NonNull String type) {
    this.name = name;
    this.type = type;
  }

  @NonNull
  public String getName() {
    return name;
  }

  @NonNull
  public String getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Animal animal = (Animal) o;

    if (name != null ? !name.equals(animal.name) : animal.name != null) return false;
    return type != null ? type.equals(animal.type) : animal.type == null;

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Animal{" +
        "name='" + name + '\'' +
        ", type='" + type + '\'' +
        '}';
  }

  public static Animal create(@NonNull String doggy, @NonNull String dog) {
    return new Animal(doggy, dog);
  }
}

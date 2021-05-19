package au.com.dius.pactconsumer.data.model;


import androidx.annotation.NonNull;

import com.squareup.moshi.Json;

public class Animal {

  @Json(name = "name")
  private final String name;

  @Json(name = "image")
  private final String image;

  public Animal(@NonNull String name, @NonNull String image) {
    this.name = name;
    this.image = image;
  }

  @NonNull
  public String getName() {
    return name;
  }

  @NonNull
  public String getType() {
    return image;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Animal animal = (Animal) o;

    if (name != null ? !name.equals(animal.name) : animal.name != null) return false;
    return image != null ? image.equals(animal.image) : animal.image == null;

  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (image != null ? image.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Animal{" +
        "name='" + name + '\'' +
        ", image='" + image + '\'' +
        '}';
  }

  public static Animal create(@NonNull String name, @NonNull String image) {
    return new Animal(name, image);
  }
}

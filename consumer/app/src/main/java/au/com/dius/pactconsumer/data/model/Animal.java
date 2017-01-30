package au.com.dius.pactconsumer.data.model;


import android.support.annotation.NonNull;

public class Animal {

  private String name;
  private String type;

  @NonNull
  public String getName() {
    return name;
  }

  public void setName(@NonNull String name) {
    this.name = name;
  }

  @NonNull
  public String getType() {
    return type;
  }

  public void setType(@NonNull String type) {
    this.type = type;
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

}

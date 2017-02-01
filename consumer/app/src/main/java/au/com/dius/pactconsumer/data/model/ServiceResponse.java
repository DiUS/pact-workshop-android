package au.com.dius.pactconsumer.data.model;

import android.support.annotation.NonNull;

import com.squareup.moshi.Json;

import org.joda.time.DateTime;

import java.util.List;

public class ServiceResponse {

  @Json(name = "test")
  private final String test;

  @Json(name = "valid_date")
  private final DateTime validDate;

  @Json(name = "animals")
  private final List<Animal> animals;

  public ServiceResponse(@NonNull String test,
                         @NonNull DateTime validDate,
                         @NonNull List<Animal> animals) {
    this.test = test;
    this.validDate = validDate;
    this.animals = animals;
  }

  public String getTest() {
    return test;
  }

  public DateTime getValidDate() {
    return validDate;
  }

  public List<Animal> getAnimals() {
    return animals;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ServiceResponse that = (ServiceResponse) o;

    if (test != null ? !test.equals(that.test) : that.test != null) return false;
    if (validDate != null ? !validDate.equals(that.validDate) : that.validDate != null)
      return false;
    return animals != null ? animals.equals(that.animals) : that.animals == null;

  }

  @Override
  public int hashCode() {
    int result = test != null ? test.hashCode() : 0;
    result = 31 * result + (validDate != null ? validDate.hashCode() : 0);
    result = 31 * result + (animals != null ? animals.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ServiceResponse{" +
        "test='" + test + '\'' +
        ", validDate=" + validDate +
        ", animals=" + animals +
        '}';
  }
}

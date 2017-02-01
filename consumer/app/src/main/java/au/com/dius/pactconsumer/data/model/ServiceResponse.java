package au.com.dius.pactconsumer.data.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.squareup.moshi.Json;

import org.joda.time.DateTime;

import java.util.List;

public class ServiceResponse {

  @Json(name = "valid_date")
  private final DateTime validDate;

  @Json(name = "animals")
  private final List<Animal> animals;

  public ServiceResponse(@Nullable DateTime validDate,
                         @NonNull List<Animal> animals) {
    this.validDate = validDate;
    this.animals = animals;
  }

  @Nullable
  public DateTime getValidDate() {
    return validDate;
  }

  @NonNull
  public List<Animal> getAnimals() {
    return animals;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ServiceResponse that = (ServiceResponse) o;

    if (validDate != null ? !validDate.equals(that.validDate) : that.validDate != null)
      return false;
    return animals != null ? animals.equals(that.animals) : that.animals == null;

  }

  @Override
  public int hashCode() {
    int result = validDate != null ? validDate.hashCode() : 0;
    result = 31 * result + (animals != null ? animals.hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "ServiceResponse{" +
        "validDate=" + validDate +
        ", animals=" + animals +
        '}';
  }

  public static ServiceResponse create(@NonNull DateTime validDate, @NonNull List<Animal> animals) {
    return new ServiceResponse(validDate, animals);
  }
}

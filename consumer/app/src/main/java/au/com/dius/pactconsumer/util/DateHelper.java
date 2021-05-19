package au.com.dius.pactconsumer.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class DateHelper {

  private DateHelper() {
  }

  public static String encodeDate(@Nullable DateTime dateTime) throws UnsupportedEncodingException {
    if (dateTime == null) {
      return null;
    }
    return URLEncoder.encode(toString(dateTime), "UTF-8");
  }

  public static DateTime parse(@NonNull String value) {
    return DateTime.parse(value);
  }

  public static String toString(@NonNull DateTime value) {
    return value.toString();
  }

}

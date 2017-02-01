package au.com.dius.pactconsumer.util;

import android.support.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public final class DateHelper {

  public static final DateTimeFormatter FORMATTER = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mmZZ");

  private DateHelper() {
  }

  public static String encodedDate(@NonNull DateTime dateTime) throws UnsupportedEncodingException {
    return URLEncoder.encode(toString(dateTime), "UTF-8");
  }

  public static DateTime parse(@NonNull String value) {
    return DateTime.parse(value, FORMATTER);
  }

  public static String toString(@NonNull DateTime value) {
    return value.toString(FORMATTER);
  }

}

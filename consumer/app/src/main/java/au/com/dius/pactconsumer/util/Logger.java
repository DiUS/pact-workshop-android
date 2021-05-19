package au.com.dius.pactconsumer.util;

import androidx.annotation.NonNull;
import android.util.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class Logger {

  @Inject
  public Logger() {
  }

  public void d(@NonNull String tag, @NonNull String msg) {
    Log.d(tag, msg);
  }

  public void e(@NonNull String tag, @NonNull String msg, @NonNull Throwable tr) {
    Log.e(tag, msg, tr);
  }

}

package au.com.dius.pactconsumer.app;

import android.app.Application;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import au.com.dius.pactconsumer.app.di.ApplicationComponent;

public abstract class PactActivity extends AppCompatActivity {

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    inject(getPactApplication().getApplicationComponent());
  }

  public abstract void inject(@NonNull ApplicationComponent component);

  public PactApplication getPactApplication() {
    Application application = getApplication();
    if (application instanceof PactApplication) {
      return (PactApplication) application;
    }
    throw new IllegalStateException("Activity " + this.getClass() + " must have a pact application");
  }

}

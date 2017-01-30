package au.com.dius.pactconsumer.app;

import android.app.Application;
import android.support.annotation.NonNull;

import au.com.dius.pactconsumer.app.di.ApplicationComponent;
import au.com.dius.pactconsumer.app.di.ApplicationModule;
import au.com.dius.pactconsumer.app.di.DaggerApplicationComponent;

public class PactApplication extends Application {

  private ApplicationComponent applicationComponent;

  @Override
  public void onCreate() {
    super.onCreate();
    initialise();
  }

  private void initialise() {
    applicationComponent = DaggerApplicationComponent.builder()
        .applicationModule(new ApplicationModule(this))
        .build();
  }

  @NonNull
  public ApplicationComponent getApplicationComponent() {
    return applicationComponent;
  }

}

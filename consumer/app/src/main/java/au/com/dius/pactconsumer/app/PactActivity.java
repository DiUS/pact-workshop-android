package au.com.dius.pactconsumer.app;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import au.com.dius.pactconsumer.app.di.ActivityComponent;
import au.com.dius.pactconsumer.app.di.DaggerActivityComponent;

public abstract class PactActivity extends AppCompatActivity {

  private ActivityComponent activityComponent;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    initialise();
    inject(activityComponent);
  }

  abstract void inject(@NonNull ActivityComponent activityComponent);

  private void initialise() {
    Object instance = getLastCustomNonConfigurationInstance();
    if (instance instanceof ActivityComponent) {
      activityComponent = (ActivityComponent) instance;
    } else {
      activityComponent = createActivityComponent();
    }
  }

  private ActivityComponent createActivityComponent() {
    return DaggerActivityComponent.builder()
        .applicationComponent(getPactApplication().getApplicationComponent())
        .build();
  }

  private PactApplication getPactApplication() {
    Application application = getApplication();
    if (application instanceof PactApplication) {
      return (PactApplication) application;
    }
    throw new IllegalStateException("Activity must belong to pact application");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return activityComponent;
  }

}

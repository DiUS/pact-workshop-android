package au.com.dius.pactconsumer.app.di;

import android.support.annotation.NonNull;

import au.com.dius.pactconsumer.presentation.HomeActivity;
import dagger.Component;

@PerActivity
@Component(dependencies = {ApplicationComponent.class})
public interface ActivityComponent {

  void inject(@NonNull HomeActivity activity);

}

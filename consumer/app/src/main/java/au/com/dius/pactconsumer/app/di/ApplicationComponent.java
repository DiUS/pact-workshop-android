package au.com.dius.pactconsumer.app.di;

import android.content.Context;
import android.support.annotation.NonNull;

import javax.inject.Singleton;

import au.com.dius.pactconsumer.util.Logger;
import au.com.dius.pactconsumer.data.Repository;
import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

  @NonNull
  Context getContext();

  @NonNull
  Logger getLogger();

  @NonNull
  Repository getRepository();
}

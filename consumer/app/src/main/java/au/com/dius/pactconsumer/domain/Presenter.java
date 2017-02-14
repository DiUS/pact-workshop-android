package au.com.dius.pactconsumer.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.joda.time.DateTime;

import java.lang.ref.WeakReference;
import java.util.List;

import au.com.dius.pactconsumer.R;
import au.com.dius.pactconsumer.data.Repository;
import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.Logger;
import au.com.dius.pactconsumer.util.RxBinder;
import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

public class Presenter implements Contract.Presenter {

  private final Repository repository;

  private final WeakReference<Contract.View> viewRef;

  private final RxBinder binder;

  private final Logger logger;

  public Presenter(@NonNull Repository repository,
                   @NonNull Contract.View view,
                   @NonNull RxBinder binder,
                   @NonNull Logger logger) {
    this.repository = repository;
    this.viewRef = new WeakReference<>(view);
    this.binder = binder;
    this.logger = logger;
  }

  @Override
  public void onStart() {
    setLoading();
    binder.bind(getAnimals(), new Consumer<List<Animal>>() {
      @Override
      public void accept(List<Animal> animals) throws Exception {
        setAnimals(animals);
      }
    }, new Consumer<Exception>() {
      @Override
      public void accept(Exception e) throws Exception {
        setError(e);
      }
    }, new Action() {
      @Override
      public void run() throws Exception {
        setComplete();
      }
    });
  }

  @Override
  public void onStop() {
    binder.clear();
  }

  private Observable<List<Animal>> getAnimals() {
    return repository.fetchResponse(DateTime.now())
        .toObservable()
        .map(new Function<ServiceResponse, List<Animal>>() {
          @Override
          public List<Animal> apply(ServiceResponse response) throws Exception {
            return response.getAnimals();
          }
        });
  }

  private void setLoading() {
    Contract.View view = getView();
    if (view == null) return;

    view.setViewState(ViewState.Loading.create());
  }

  private void setAnimals(@NonNull List<Animal> animals) {
    Contract.View view = getView();
    if (view == null) return;

    if (animals.isEmpty()) {
      view.setViewState(ViewState.Empty.create(R.string.empty_message));
      return;
    }

    view.setViewState(ViewState.Loaded.create(animals));
  }

  private void setError(@NonNull Exception exception) {
    Contract.View view = getView();
    if (view == null) return;

    logger.e(Presenter.class.getSimpleName(), "Error loading service response", exception);
    view.setViewState(ViewState.Error.create(R.string.error_message));
  }

  private void setComplete() {
  }

  @Nullable
  private Contract.View getView() {
    return viewRef.get();
  }

}

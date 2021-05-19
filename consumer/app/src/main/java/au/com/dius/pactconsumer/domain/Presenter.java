package au.com.dius.pactconsumer.domain;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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
    binder.bind(getAnimals(), this::setAnimals, this::setError, this::setComplete);
  }

  @Override
  public void onStop() {
    binder.clear();
  }

  private Observable<List<Animal>> getAnimals() {
    return repository.fetchResponse(DateTime.now())
        .toObservable()
        .map(ServiceResponse::getAnimals);
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

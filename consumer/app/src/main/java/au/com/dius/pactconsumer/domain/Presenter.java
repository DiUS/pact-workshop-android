package au.com.dius.pactconsumer.domain;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;

import au.com.dius.pactconsumer.R;
import au.com.dius.pactconsumer.data.Repository;
import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.util.RxBinder;
import io.reactivex.Observable;

public class Presenter implements Contract.Presenter {

  private final Repository repository;

  private final WeakReference<Contract.View> viewRef;

  private final RxBinder binder;

  public Presenter(@NonNull Repository repository,
                   @NonNull Contract.View view,
                   @NonNull RxBinder binder) {
    this.repository = repository;
    this.viewRef = new WeakReference<>(view);
    this.binder = binder;
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
    return repository.getAnimals()
        .toObservable();
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

  private void setError(@NonNull Throwable error) {
    Contract.View view = getView();
    if (view == null) return;

    view.setViewState(ViewState.Error.create(R.string.error_message));
  }

  private void setComplete() {
  }

  @Nullable
  private Contract.View getView() {
    return viewRef.get();
  }

}

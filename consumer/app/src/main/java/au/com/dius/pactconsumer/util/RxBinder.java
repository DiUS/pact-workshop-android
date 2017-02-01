package au.com.dius.pactconsumer.util;


import android.util.Log;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

public class RxBinder {

  private CompositeDisposable compositeDisposable = new CompositeDisposable();

  public <T> void bind(Observable<T> observable,
                       Consumer<T> onNext,
                       Consumer<RuntimeException> onError,
                       Action onComplete) {
    compositeDisposable.add(
        observable.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableObserver<T>() {

              @Override
              public void onNext(T o) {
                try {
                  onNext.accept(o);
                } catch (Exception e) {
                  Log.e(RxBinder.class.getSimpleName(), "Error calling onNext", e);
                }
              }

              @Override
              public void onError(Throwable thr) {
                try {
                  if (thr instanceof RuntimeException) {
                    onError.accept((RuntimeException) thr);
                  } else {
                    throw thr;
                  }
                } catch (Throwable e) {
                  Log.e(RxBinder.class.getSimpleName(), "Error in stream", thr);
                  Exceptions.propagate(thr);
                }
              }

              @Override
              public void onComplete() {
                try {
                  onComplete.run();
                } catch (Exception e) {
                  Log.e(RxBinder.class.getSimpleName(), "Error calling onComplete", e);
                }
              }
            })
    );
  }

  public void clear() {
    compositeDisposable.clear();
  }
}

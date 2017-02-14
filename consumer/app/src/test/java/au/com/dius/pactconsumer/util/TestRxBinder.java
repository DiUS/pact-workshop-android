package au.com.dius.pactconsumer.util;

import io.reactivex.Observable;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

public class TestRxBinder extends RxBinder {

  @Override
  public <T> void bind(Observable<T> observable,
                       final Consumer<T> onNext,
                       final Consumer<Exception> onError,
                       final Action onComplete) {
    observable
        .subscribeWith(new DisposableObserver<T>() {

          @Override
          public void onNext(T o) {
            try {
              onNext.accept(o);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onError(Throwable thr) {
            if (!(thr instanceof Exception)) {
              Exceptions.throwIfFatal(thr);
              return;
            }

            try {
              onError.accept((Exception) thr);
            } catch (Exception e) {
              e.printStackTrace();
            }
          }

          @Override
          public void onComplete() {
            try {
              onComplete.run();
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
  }
}

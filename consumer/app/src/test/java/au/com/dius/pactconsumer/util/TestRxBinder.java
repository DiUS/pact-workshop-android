package au.com.dius.pactconsumer.util;

import io.reactivex.Observable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableObserver;

public class TestRxBinder extends RxBinder {

  @Override
  public <T> void bind(Observable<T> observable,
                       Consumer<T> onNext,
                       Consumer<RuntimeException> onError,
                       Action onComplete) {
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
            try {
              if (thr instanceof RuntimeException) {
                onError.accept((RuntimeException) thr);
              } else {
                throw thr;
              }
            } catch (Throwable e) {
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

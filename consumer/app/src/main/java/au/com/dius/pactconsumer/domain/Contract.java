package au.com.dius.pactconsumer.domain;

import android.support.annotation.NonNull;

import au.com.dius.pactconsumer.app.PactPresenter;
import au.com.dius.pactconsumer.app.PactView;

public interface Contract {

  interface View extends PactView {
    void setViewState(@NonNull ViewState viewState);
  }

  interface Presenter extends PactPresenter {
  }

}

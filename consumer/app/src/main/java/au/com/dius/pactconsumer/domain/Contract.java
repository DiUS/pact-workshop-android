package au.com.dius.pactconsumer.domain;

import android.support.annotation.NonNull;

import au.com.dius.pactconsumer.app.PactView;
import au.com.dius.pactconsumer.app.PactPresenter;

interface Contract {

  interface View extends PactView {
    void setViewState(@NonNull ViewState viewState);
  }

  interface Presenter extends PactPresenter<View> {
  }

}

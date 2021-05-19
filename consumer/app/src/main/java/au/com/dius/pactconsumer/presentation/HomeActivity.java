package au.com.dius.pactconsumer.presentation;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import javax.inject.Inject;

import au.com.dius.pactconsumer.R;
import au.com.dius.pactconsumer.app.PactActivity;
import au.com.dius.pactconsumer.app.di.ApplicationComponent;
import au.com.dius.pactconsumer.data.Repository;
import au.com.dius.pactconsumer.domain.Contract;
import au.com.dius.pactconsumer.domain.Presenter;
import au.com.dius.pactconsumer.domain.ViewState;
import au.com.dius.pactconsumer.util.Logger;
import au.com.dius.pactconsumer.util.RxBinder;

public class HomeActivity extends PactActivity implements Contract.View {

  @Inject
  Repository repository;

  @Inject
  Logger logger;

  private Presenter presenter;

  private View loadingView;
  private TextView emptyView;
  private TextView errorView;
  private RecyclerView recyclerView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_animals);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    initialiseView();
    initialisePresenter(savedInstanceState);
  }

  private void initialiseView() {
    loadingView = findViewById(R.id.view_loading);
    emptyView = (TextView) findViewById(R.id.txt_empty);
    errorView = (TextView) findViewById(R.id.txt_error);
    recyclerView = (RecyclerView) findViewById(R.id.view_recycler);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
  }

  private void initialisePresenter(@Nullable Bundle savedInstanceState) {
    presenter = new Presenter(repository, this, new RxBinder(), logger);
  }

  @Override
  public void inject(@NonNull ApplicationComponent component) {
    component.inject(this);
  }

  @Override
  protected void onResume() {
    super.onResume();
    presenter.onStart();
  }

  @Override
  protected void onPause() {
    super.onPause();
    presenter.onStop();
  }

  @Override
  public void setViewState(@NonNull ViewState viewState) {
    if (viewState instanceof ViewState.Loading) {
      setLoading((ViewState.Loading) viewState);
    } else if (viewState instanceof ViewState.Loaded) {
      setLoaded((ViewState.Loaded) viewState);
    } else if (viewState instanceof ViewState.Empty) {
      setEmpty((ViewState.Empty) viewState);
    } else if (viewState instanceof ViewState.Error) {
      setError((ViewState.Error) viewState);
    }
  }

  private void setLoading(@NonNull ViewState.Loading viewState) {
    hideViews();
    loadingView.setVisibility(View.VISIBLE);
  }

  private void setLoaded(@NonNull ViewState.Loaded viewState) {
    hideViews();
    recyclerView.setVisibility(View.VISIBLE);
    recyclerView.setAdapter(new AnimalsAdapter(viewState.getAnimals()));
  }

  private void setEmpty(@NonNull ViewState.Empty viewState) {
    hideViews();
    emptyView.setText(getResources().getString(viewState.getMessage()));
    emptyView.setVisibility(View.VISIBLE);
  }

  private void setError(@NonNull ViewState.Error viewState) {
    hideViews();
    errorView.setText(getResources().getString(viewState.getMessage()));
    errorView.setVisibility(View.VISIBLE);
  }

  private void hideViews() {
    loadingView.setVisibility(View.GONE);
    emptyView.setVisibility(View.GONE);
    errorView.setVisibility(View.GONE);
    recyclerView.setVisibility(View.GONE);
  }

}

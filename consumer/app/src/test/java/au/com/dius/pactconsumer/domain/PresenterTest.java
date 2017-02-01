package au.com.dius.pactconsumer.domain;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Collections;

import au.com.dius.pactconsumer.R;
import au.com.dius.pactconsumer.data.FakeService;
import au.com.dius.pactconsumer.data.Repository;
import au.com.dius.pactconsumer.data.exceptions.ServiceException;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.Logger;
import au.com.dius.pactconsumer.util.TestRxBinder;
import io.reactivex.Single;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PresenterTest {

  Repository repository;
  Contract.View view;
  Presenter presenter;

  @Before
  public void setUp() {
    repository = mock(Repository.class);
    view = mock(Contract.View.class);
    presenter = new Presenter(repository, view, new TestRxBinder(), mock(Logger.class));
  }

  @Test
  public void should_show_loaded_when_fetch_succeeds() {
    // given
    when(repository.fetchResponse(any())).thenReturn(Single.just(FakeService.RESPONSE));

    // when
    presenter.onStart();

    // then
    verify(view).setViewState(ViewState.Loaded.create(FakeService.RESPONSE.getAnimals()));
  }

  @Test
  public void should_show_error_when_fetch_fails() {
    // given
    ServiceException exception = new ServiceException();
    when(repository.fetchResponse(any())).thenReturn(Single.error(exception));

    // when
    presenter.onStart();

    // then
    verify(view).setViewState(ViewState.Error.create(R.string.error_message));
  }

  @Test
  public void should_show_empty_when_fetch_returns_nothing() {
    // given
    when(repository.fetchResponse(any())).thenReturn(Single.just(new ServiceResponse("", DateTime.now(), Collections.emptyList())));

    // when
    presenter.onStart();

    // then
    verify(view).setViewState(ViewState.Empty.create(R.string.empty_message));
  }

  @Test
  public void should_show_loading_when_fetching() {
    // given
    when(repository.fetchResponse(any())).thenReturn(Single.just(FakeService.RESPONSE));

    // when
    presenter.onStart();

    // then
    InOrder inOrder = Mockito.inOrder(view);
    inOrder.verify(view).setViewState(ViewState.Loading.create());
    inOrder.verify(view).setViewState(ViewState.Loaded.create(FakeService.RESPONSE.getAnimals()));
  }

}

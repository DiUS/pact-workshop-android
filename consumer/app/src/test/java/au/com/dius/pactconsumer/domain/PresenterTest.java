package au.com.dius.pactconsumer.domain;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import au.com.dius.pactconsumer.R;
import au.com.dius.pactconsumer.data.Repository;
import au.com.dius.pactconsumer.data.exceptions.ServiceException;
import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.util.TestRxBinder;
import io.reactivex.Single;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PresenterTest {

  Repository repository;
  Contract.View view;
  Presenter presenter;

  List<Animal> animals;

  @Before
  public void setUp() {
    repository = mock(Repository.class);
    view = mock(Contract.View.class);
    presenter = new Presenter(repository, view, new TestRxBinder());

    animals = Arrays.asList(Animal.create("Doggy", "dog"));
  }

  @Test
  public void should_show_loaded_when_fetch_succeeds() {
    // given
    when(repository.getAnimals()).thenReturn(Single.just(animals));

    // when
    presenter.onStart();

    // then
    verify(view).setViewState(ViewState.Loaded.create(animals));
  }

  @Test
  public void should_show_error_when_fetch_fails() {
    // given
    ServiceException exception = new ServiceException();
    when(repository.getAnimals()).thenReturn(Single.error(exception));

    // when
    presenter.onStart();

    // then
    verify(view).setViewState(ViewState.Error.create(R.string.error_message));
  }

  @Test
  public void should_show_empty_when_fetch_returns_nothing() {
    // given
    when(repository.getAnimals()).thenReturn(Single.just(Collections.emptyList()));

    // when
    presenter.onStart();

    // then
    verify(view).setViewState(ViewState.Empty.create(R.string.empty_message));
  }

  @Test
  public void should_show_loading_when_fetching() {
    // given
    when(repository.getAnimals()).thenReturn(Single.just(animals));

    // when
    presenter.onStart();

    // then
    InOrder inOrder = Mockito.inOrder(view);
    inOrder.verify(view).setViewState(ViewState.Loading.create());
    inOrder.verify(view).setViewState(ViewState.Loaded.create(animals));
  }

}

package au.com.dius.pactconsumer.data;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import au.com.dius.pactconsumer.data.model.Animal;
import io.reactivex.observers.TestObserver;

public class FakeServiceTest {

  FakeService service;

  @Before
  public void setUp() {
    service = new FakeService();
  }

  @Test
  public void should_return_list_of_animals() {
    // when
    TestObserver<List<Animal>> observer = service.getAnimals().test();

    // then
    observer.assertNoErrors();
    observer.assertValueAt(0, animals -> animals.size() == 3);
  }

}

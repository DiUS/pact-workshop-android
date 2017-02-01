package au.com.dius.pactconsumer.data;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import au.com.dius.pactconsumer.data.model.ServiceResponse;
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
    TestObserver<ServiceResponse> observer = service.fetchResponse(DateTime.now()).test();

    // then
    observer.assertNoErrors();
    observer.assertValue(FakeService.RESPONSE);
  }

}

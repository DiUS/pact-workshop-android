package au.com.dius.pactconsumer.data;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;

import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ServiceTest {

  Service.Api api;
  Service service;

  @Before
  public void setup() {
    api = mock(Service.Api.class);
    service = new Service(api);
  }

  @Test
  public void should_process_json_payload_from_provider() {
    // given
    ServiceResponse response = ServiceResponse.create(DateTime.now(), Collections.singletonList(Animal.create("Doggy", "dog")));
    when(api.loadProviderJson(Mockito.<String>any())).thenReturn(Single.just(response));

    // when
    TestObserver<ServiceResponse> observer = service.fetchResponse(DateTime.now()).test();

    // then
    observer.assertNoErrors();
    observer.assertValue(response);
  }

}

package au.com.dius.pactconsumer.data;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.DateHelper;
import au.com.dius.pactconsumer.util.Serializer;
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
    service = new Service(api, new Serializer());
  }

  @Test
  public void should_process_json_payload_from_provider() {
    // given
    String dateTimeJson = "2017-02-01T12:23:20+11:00";
    DateTime dateTime = DateHelper.parse(dateTimeJson);
    when(api.loadProviderJson(any())).thenReturn(
        Single.just(
            "{\n" +
                "      \"test\": \"NO\",\n" +
                "      \"valid_date\": \"" + dateTimeJson + "\",\n" +
                "      \"animals\": [\n" +
                "        {\n" +
                "          \"name\": \"Doggy\",\n" +
                "          \"type\": \"dog\"\n" +
                "        }\n" +
                "      ]\n" +
                "}"
        )
    );

    // when
    TestObserver<ServiceResponse> observer = service.fetchResponse(DateTime.now()).test();

    // then
    observer.assertNoErrors();
    observer.assertValue(new ServiceResponse(dateTime, Collections.singletonList(Animal.create("Doggy", "dog"))));
  }

}

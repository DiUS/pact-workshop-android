package au.com.dius.pactconsumer.data;


import android.content.Context;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.UnsupportedEncodingException;
import java.util.Collections;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pactconsumer.app.di.NetworkModule;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.DateHelper;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.mock;

@PactTestFor(
        providerName = "our_provider",
        port = "0" // run stubserver on random port, access port via mockserver
)
@ExtendWith(PactConsumerTestExt.class)
public class ServiceNoContentPactTest {

  static final DateTime DATE_TIME;

  static {
    DATE_TIME = DateTime.now();
  }

  Service service;

  @BeforeEach
  public void setUp(MockServer mockServer) {
    NetworkModule networkModule = new NetworkModule();
    service = new Service(networkModule.getRetrofit(mock(Context.class), "http://localhost:"+mockServer.getPort()).create(Service.Api.class));
  }

  @Pact(provider = "our_provider", consumer = "our_consumer")
  public RequestResponsePact createFragment(PactDslWithProvider builder) throws UnsupportedEncodingException {
    return builder
        .given("data count is == 0")
        .uponReceiving("a request for json data")
        .path("/provider.json")
        .method("GET")
        .query("valid_date=" + DateHelper.encodeDate(DATE_TIME))
        .willRespondWith()
        .status(404)
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "createFragment")
  public void should_process_the_json_payload_from_provider() {
    TestObserver<ServiceResponse> observer = service.fetchResponse(DATE_TIME).test();
    observer.assertNoErrors();
    observer.assertValue(new ServiceResponse(null, Collections.emptyList()));
  }
}

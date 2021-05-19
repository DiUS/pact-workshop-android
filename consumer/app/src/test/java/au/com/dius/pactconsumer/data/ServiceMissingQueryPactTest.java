package au.com.dius.pactconsumer.data;


import android.content.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.UnsupportedEncodingException;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pactconsumer.app.di.NetworkModule;
import au.com.dius.pactconsumer.data.exceptions.BadRequestException;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.mock;

@PactTestFor(
        providerName = "our_provider"
)
@ExtendWith(PactConsumerTestExt.class)
public class ServiceMissingQueryPactTest {

  Service service;

  @BeforeEach
  public void setUp(MockServer mockServer) {
    NetworkModule networkModule = new NetworkModule();
    service = new Service(networkModule.getRetrofit(mock(Context.class), "http://localhost:"+mockServer.getPort()).create(Service.Api.class));
  }

  @Pact(provider = "our_provider", consumer = "our_consumer")
  public RequestResponsePact createFragment(PactDslWithProvider builder) throws UnsupportedEncodingException {
    return builder
        .given("data count is > 0")
        .uponReceiving("a request with an missing date parameter")
        .path("/provider.json")
        .method("GET")
        .willRespondWith()
        .status(400)
        .body("valid_date is required")
        .toPact();
  }

  @Test
  @PactTestFor(pactMethod = "createFragment")
  public void should_process_the_json_payload_from_provider() {
    TestObserver<ServiceResponse> observer = service.fetchResponse(null).test();
    observer.assertError(BadRequestException.class);
  }
}

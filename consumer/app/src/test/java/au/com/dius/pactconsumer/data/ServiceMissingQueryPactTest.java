package au.com.dius.pactconsumer.data;


import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import au.com.dius.pactconsumer.app.di.NetworkModule;
import au.com.dius.pactconsumer.data.exceptions.BadRequestException;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.mock;

public class ServiceMissingQueryPactTest {

  Service service;

  @Before
  public void setUp() {
    NetworkModule networkModule = new NetworkModule();
    service = new Service(networkModule.getRetrofit(mock(Context.class), "http://localhost:9292").create(Service.Api.class));
  }

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("our_provider", "localhost", 9292, this);

  @Pact(provider = "our_provider", consumer = "our_consumer")
  public PactFragment createFragment(PactDslWithProvider builder) throws UnsupportedEncodingException {
    return builder
        .given("data count is > 0")
        .uponReceiving("a request with an missing date parameter")
        .path("/provider.json")
        .method("GET")
        .willRespondWith()
        .status(400)
        .body("valid_date is required")
        .toFragment();
  }

  @Test
  @PactVerification("our_provider")
  public void should_process_the_json_payload_from_provider() {
    TestObserver<ServiceResponse> observer = service.fetchResponse(null).test();
    observer.assertError(BadRequestException.class);
  }
}

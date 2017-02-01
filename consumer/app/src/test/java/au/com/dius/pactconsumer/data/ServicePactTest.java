package au.com.dius.pactconsumer.data;


import android.content.Context;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import au.com.dius.pactconsumer.app.di.NetworkModule;
import au.com.dius.pactconsumer.data.model.Animal;
import au.com.dius.pactconsumer.data.model.ServiceResponse;
import au.com.dius.pactconsumer.util.DateHelper;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.mock;

public class ServicePactTest {

  static final DateTime DATE_TIME;
  static final Map<String, String> HEADERS;

  static {
    DATE_TIME = DateHelper.parse("2017-02-01T20:23:25.275+11:00");

    HEADERS = new HashMap<>();
    HEADERS.put("Content-Type", "application/json");
  }

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
    PactDslJsonBody body = new PactDslJsonBody()
        .stringType("test")
        .stringType("valid_date", DateHelper.toString(DATE_TIME))
        .eachLike("animals", 3)
        .stringType("name", "Doggy")
        .stringType("image", "dog")
        .closeObject()
        .closeArray()
        .asBody();

    return builder
        .given("data count is > 0")
        .uponReceiving("a request for json data")
        .path("/provider.json")
        .method("GET")
        .query("valid_date=" + DateHelper.encodeDate(DATE_TIME))
        .willRespondWith()
        .status(200)
        .headers(HEADERS)
        .body(body)
        .toFragment();
  }

  @Test
  @PactVerification("our_provider")
  public void should_process_the_json_payload_from_provider() {
    TestObserver<ServiceResponse> observer = service.fetchResponse(DATE_TIME).test();
    observer.assertNoErrors();
    observer.assertValue(ServiceResponse.create(DATE_TIME, Arrays.asList(
        Animal.create("Doggy", "dog"),
        Animal.create("Doggy", "dog"),
        Animal.create("Doggy", "dog")
    )));
  }
}

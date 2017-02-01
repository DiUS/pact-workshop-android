package au.com.dius.pactconsumer.data;


import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import au.com.dius.pact.consumer.Pact;
import au.com.dius.pact.consumer.PactProviderRule;
import au.com.dius.pact.consumer.PactVerification;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.model.PactFragment;
import au.com.dius.pactconsumer.app.di.NetworkModule;
import au.com.dius.pactconsumer.util.DateHelper;
import au.com.dius.pactconsumer.util.Serializer;
import io.reactivex.observers.TestObserver;

public class ServicePactTest {

  static final DateTime DATE_TIME;
  static final Map<String, String> HEADERS;
  static final String JSON;

  static {
    String dateTimeJson = "2017-02-01T12:23+11:00";
    DATE_TIME = DateTime.parse(dateTimeJson);

    HEADERS = new HashMap<>();
    HEADERS.put("Content-Type", "application/json");

    JSON =
        "{\n" +
            "      \"test\": \"NO\",\n" +
            "      \"valid_date\": \"" + dateTimeJson + "\",\n" +
            "      \"animals\": [\n" +
            "        {\n" +
            "          \"name\": \"Doggy\",\n" +
            "          \"type\": \"dog\"\n" +
            "        }\n" +
            "      ]\n" +
            "}";
  }

  Service service;

  @Before
  public void setUp() {
    NetworkModule networkModule = new NetworkModule();
    service = new Service(networkModule.getRetrofit("http://localhost:9292").create(Service.Api.class), new Serializer());
  }

  @Rule
  public PactProviderRule mockProvider = new PactProviderRule("Provider", "localhost", 9292, this);

  @Pact(provider = "Provider", consumer = "Consumer")
  public PactFragment createFragment(PactDslWithProvider builder) throws UnsupportedEncodingException {
    return builder
        .given("data count is > 0")
        .uponReceiving("a request for json data")
        .path("/provider.json")
        .method("GET")
        .query("valid_date=" + DateHelper.encodedDate(DATE_TIME))
        .willRespondWith()
        .status(200)
        .headers(HEADERS)
        .body(JSON)
        .toFragment();
  }

  @Test
  @PactVerification("Provider")
  public void should_process_the_json_payload_from_provider() {
    TestObserver observer = service.fetchResponse(DATE_TIME).test();
    observer.assertNoErrors();
  }
}

Example Android Project for Pact Workshop
======================================

When writing a lot of small services, testing the interactions between these becomes a major headache. That's the problem Pact is trying to solve.

Integration tests typically are slow and brittle, requiring each component to have it's own environment to run the tests in. With a micro-service architecture, this becomes even more of a problem. They also have to be 'all-knowing' and this makes them difficult to keep from being fragile.

After J. B. Rainsberger's talk "Integrated Tests Are A Scam" people have been thinking how to get the confidence we need to deploy our software to production without having a tiresome integration test suite that does not give us all the coverage we think it does.

Pact is a testing framework that allows you to define a pact between service consumers and providers. It provides a DSL for service consumers to define the request they will make to a service producer and the response they expect back. This expectation is used in the consumers specs to provide a mock producer, and is also played back in the producer specs to ensure the producer actually does provide the response the consumer expects.

This allows you to test both sides of an integration point using fast unit tests.

## Prerequisites

You will need to have the following items installed for you to run through the workshop.

- Java 1.8
- AndroidStudio (2.2.3)
- Android SDK 25, Emulator Image and Latest build Tools
- Ruby 2.3.0

## Step 1 - Simple customer calling Provider

Given we have a android app that needs to make a HTTP GET request to a sinatra webapp, and requires a response in JSON format. The client would look something like:

Service.java:

```java
public class Service implements Repository {

  public interface Api {
    @GET("provider.json")
    Single<ServiceResponse> loadProviderJson(@Query("valid_date") String validDate);
  }

  private final Api api;

  @Inject
  public Service(@NonNull Api api) {
    this.api = api;
  }

  @NonNull
  @Override
  public Single<ServiceResponse> fetchResponse(@NonNull DateTime dateTime) {
    try {
      return api.loadProviderJson(DateHelper.encodeDate(dateTime));
    } catch (UnsupportedEncodingException e) {
      return Single.error(e);
    }
  }

}
```

and the reponse ServiceResponse.java:

```java
public class ServiceResponse {

  @Json(name = "date")
  private final DateTime validDate;

  @Json(name = "data")
  private final List<Animal> animals;

  public ServiceResponse(@Nullable DateTime validDate,
                         @NonNull List<Animal> animals) {
    this.validDate = validDate;
    this.animals = animals;
  }

  @Nullable
  public DateTime getValidDate() {
    return validDate;
  }

  @NonNull
  public List<Animal> getAnimals() {
    return animals;
  }
...
```

and the provider provider.rb:

```ruby
class Provider < Sinatra::Base

  get '/provider.json', :provides => 'json' do
      valid_time = Time.parse(params[:valid_date])
      JSON.pretty_generate({
        :test => 'NO',
        :valid_date => DateTime.now,
        :animals => ProviderData.animals
      })
  end

end
```

## Step 2 - Client Tested but integration fails

Now lets test the client on the app:

ServiceTest.java:

```java
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
    when(api.loadProviderJson(any())).thenReturn(Single.just(response));

    // when
    TestObserver<ServiceResponse> observer = service.fetchResponse(DateTime.now()).test();

    // then
    observer.assertNoErrors();
    observer.assertValue(response);
  }

}
```

Let's run this test and see it all pass:

```console
$ ./gradlew clean testDebugUnitTest

...
:app:testDebugUnitTest

au.com.dius.pactconsumer.domain.PresenterTest > should_show_empty_when_fetch_returns_nothing PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_error_when_fetch_fails PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_loaded_when_fetch_succeeds PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_loading_when_fetching PASSED

au.com.dius.pactconsumer.data.ServiceTest > should_process_json_payload_from_provider PASSED

au.com.dius.pactconsumer.data.FakeServiceTest > should_return_list_of_animals PASSED

BUILD SUCCESSFUL

Total time: 16.089 secs
```

However, there is a problem with this integration point. The provider returns a different field names, which will blow up when run for real even with the tests all passing. Here is where Pact comes in.

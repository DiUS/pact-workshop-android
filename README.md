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

## Step 3 - Pact to the rescue

Lets setup Pact in the consumer. Pact lets the consumers define the expectations for the integration point.

Lets add a pact for the consumer.

ServicePactTest.java:

```java
public class ServicePactTest {

  static final DateTime DATE_TIME;
  static final Map<String, String> HEADERS;
  static final String JSON;

  static {
    DATE_TIME = DateTime.now();

    HEADERS = new HashMap<>();
    HEADERS.put("Content-Type", "application/json");

    JSON = "{\n" +
        "      \"test\": \"NO\",\n" +
        "      \"date\": \"" + DateHelper.toString(DATE_TIME) + "\",\n" +
        "      \"data\": [\n" +
        "        {\n" +
        "          \"name\": \"Doggy\",\n" +
        "          \"image\": \"dog\"\n" +
        "        }\n" +
        "      ]\n" +
        "}";
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
    return builder
        .given("data count is > 0")
        .uponReceiving("a request for json data")
        .path("/provider.json")
        .method("GET")
        .query("valid_date=" + DateHelper.encodeDate(DATE_TIME))
        .willRespondWith()
        .status(200)
        .headers(HEADERS)
        .body(JSON)
        .toFragment();
  }

  @Test
  @PactVerification("our_provider")
  public void should_process_the_json_payload_from_provider() {
    TestObserver<ServiceResponse> observer = service.fetchResponse(DATE_TIME).test();
    observer.assertNoErrors();
    observer.assertValue(ServiceResponse.create(DATE_TIME, Collections.singletonList(Animal.create("Doggy", "dog"))));
  }
}
```

Running this test still passes, but it creates a pact file which we can use to validate our assumptions on the provider side.

```console
./gradlew clean testDebugUnitTest
...

au.com.dius.pactconsumer.domain.PresenterTest > should_show_empty_when_fetch_returns_nothing PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_error_when_fetch_fails PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_loaded_when_fetch_succeeds PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_loading_when_fetching PASSED

au.com.dius.pactconsumer.data.ServiceTest > should_process_json_payload_from_provider PASSED

au.com.dius.pactconsumer.data.FakeServiceTest > should_return_list_of_animals PASSED

au.com.dius.pactconsumer.data.ServicePactTest > should_process_the_json_payload_from_provider PASSED

BUILD SUCCESSFUL

Total time: 18.835 secs
```

Generated pact file (consumer/app/target/pacts/our_consumer-our_provider.json):

```json
{
    "provider": {
        "name": "our_provider"
    },
    "consumer": {
        "name": "our_consumer"
    },
    "interactions": [
        {
            "description": "a request for json data",
            "request": {
                "method": "GET",
                "path": "/provider.json",
                "query": "valid_date=2017-02-01T19%253A53%253A27.038%252B11%253A00"
            },
            "response": {
                "status": 200,
                "headers": {
                    "Content-Type": "application/json"
                },
                "body": {
                    "data": [
                        {
                            "image": "dog",
                            "name": "Doggy"
                        }
                    ],
                    "date": "2017-02-01T19:53:27.038+11:00",
                    "test": "NO"
                }
            },
            "providerState": "data count is > 0"
        }
    ],
    "metadata": {
        "pact-specification": {
            "version": "2.0.0"
        },
        "pact-jvm": {
            "version": "3.3.6"
        }
    }
}
```
## Step 4 - Verify pact against provider

Pact has a rake task to verify the producer against the generated pact file. It can get the pact file from any URL (like the last successful CI build), but we just going to use the local one. Here is the addition to the Rakefile.

Rakefile:

```ruby
require 'pact/tasks'
```

spec/pact_helper.rb:

```ruby
require 'pact/provider/rspec'

Pact.service_provider "our_provider" do

  honours_pact_with 'our_consumer' do
    pact_uri 'spec/pacts/our_consumer-our_provider.json'
  end

end
```

Now if we copy the pact file from the consumer project and run our pact verification task, it should fail.

```console
$ rake pact:verify

SPEC_OPTS='' /home/theeban/.rvm/rubies/ruby-2.3.0/bin/ruby -S pact verify --pact-helper /home/theeban/Projects/pact-workshop-android/provider/spec/pact_helper.rb
Reading pact at spec/pacts/our_consumer-our_provider.json

Verifying a pact between our_consumer and our_provider
  Given data count is > 0
    a request for json data
      with GET /provider.json?valid_date=2017-02-01T19%253A53%253A27.038%252B11%253A00
        returns a response which
          has status code 200 (FAILED - 1)
          has a matching body (FAILED - 2)
          includes headers
            "Content-Type" with value "application/json" (FAILED - 3)

Failures:

  1) Verifying a pact between our_consumer and our_provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=2017-02-01T19%253A53%253A27.038%252B11%253A00 returns a response which has status code 200
     Got 0 failures and 2 other errors:

     1.1) Failure/Error: set_up_provider_state interaction.provider_state, options[:consumer]
          
          RuntimeError:
            Could not find provider state "data count is > 0" for consumer our_consumer
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `load'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `<main>'

     1.2) Failure/Error: tear_down_provider_state interaction.provider_state, options[:consumer]
          
          RuntimeError:
            Could not find provider state "data count is > 0" for consumer our_consumer
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `load'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `<main>'

  2) Verifying a pact between our_consumer and our_provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=2017-02-01T19%253A53%253A27.038%252B11%253A00 returns a response which has a matching body
     Got 0 failures and 2 other errors:

     2.1) Failure/Error: set_up_provider_state interaction.provider_state, options[:consumer]
          
          RuntimeError:
            Could not find provider state "data count is > 0" for consumer our_consumer
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `load'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `<main>'

     2.2) Failure/Error: tear_down_provider_state interaction.provider_state, options[:consumer]
          
          RuntimeError:
            Could not find provider state "data count is > 0" for consumer our_consumer
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `load'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `<main>'

  3) Verifying a pact between our_consumer and our_provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=2017-02-01T19%253A53%253A27.038%252B11%253A00 returns a response which includes headers "Content-Type" with value "application/json"
     Got 0 failures and 2 other errors:

     3.1) Failure/Error: set_up_provider_state interaction.provider_state, options[:consumer]
          
          RuntimeError:
            Could not find provider state "data count is > 0" for consumer our_consumer
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `load'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `<main>'

     3.2) Failure/Error: tear_down_provider_state interaction.provider_state, options[:consumer]
          
          RuntimeError:
            Could not find provider state "data count is > 0" for consumer our_consumer
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `load'
          # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `<main>'

1 interaction, 1 failure

Failed interactions:

bundle exec rake pact:verify:at[spec/pacts/our_consumer-our_provider.json] PACT_DESCRIPTION="a request for json data" PACT_PROVIDER_STATE="data count is > 0" # A request for json data given data count is > 0

For assistance debugging failures, run `bundle exec rake pact:verify:help`

Could not find one or more provider states.
Have you required the provider states file for this consumer in your pact_helper.rb?
If you have not yet defined these states, here is a template:

Pact.provider_states_for "our_consumer" do

  provider_state "data count is > 0" do
    set_up do
      # Your set up code goes here
    end
  end

end
```

This has failed due to the provider state we defined. Luckily pact has been quite helpful and given us a snippet
of what we need to do to fix it.

## Step 5 - Correct provider states

Add the snippet from the verification failure to the pact helper.

spec/pact_helper.rb:

```ruby
Pact.provider_states_for "our_consumer" do

  provider_state "data count is > 0" do
    set_up do
      # Your set up code goes here
    end
  end

end
```

and then re-run the provider verification.

```console
$ rake pact:verify

SPEC_OPTS='' /home/theeban/.rvm/rubies/ruby-2.3.0/bin/ruby -S pact verify --pact-helper /home/theeban/Projects/pact-workshop-android/provider/spec/pact_helper.rb
Reading pact at spec/pacts/our_consumer-our_provider.json

Verifying a pact between our_consumer and our_provider
  Given data count is > 0
    a request for json data
      with GET /provider.json?valid_date=2017-02-01T19%253A53%253A27.038%252B11%253A00
        returns a response which
          has status code 200
          has a matching body (FAILED - 1)
          includes headers
            "Content-Type" with value "application/json"

Failures:

  1) Verifying a pact between our_consumer and our_provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=2017-02-01T19%253A53%253A27.038%252B11%253A00 returns a response which has a matching body
     Failure/Error: expect(response_body).to match_term expected_response_body, diff_options
     
       Actual: {"test":"NO","valid_date":"2017-02-01T20:14:51+11:00","animals":[{"name":"Buddy","image":"dog"},{"name":"Cathy","image":"cat"},{"name":"Birdy","image":"bird"}]}
     
       @@ -1,10 +1,3 @@
        {
       -  "data": [
       -    {
       -      "image": "dog",
       -      "name": "Doggy"
       -    },
       -  ],
       -  "date": "2017-02-01T19:53:27.038+11:00"
        }
       
       Key: - means "expected, but was not found". 
            + means "actual, should not be found". 
            Values where the expected matches the actual are not shown.
     # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
     # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `load'
     # /home/theeban/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:22:in `<main>'

1 interaction, 1 failure

Failed interactions:

bundle exec rake pact:verify:at[spec/pacts/our_consumer-our_provider.json] PACT_DESCRIPTION="a request for json data" PACT_PROVIDER_STATE="data count is > 0" # A request for json data given data count is > 0

For assistance debugging failures, run `bundle exec rake pact:verify:help`

```

The test has failed for 2 reasons. Firstly, the animals field has a different value to what was expected by the consumer. Secondly, and more importantly, the consumer was expecting a date field.

## Step 6 - Back to the client we go

Let's correct the consumer tests to handle any list of animals and use the correct field for the date.

Then we need to add a type matcher for `animals` and change the field for the date to be `valid_date`. We can also
add a regular expression to make sure the `valid_date` field is a valid date. This is important because we are
parsing it.

The updated consumer test is now:

```java
public class ServicePactTest {

  static final DateTime DATE_TIME;
  static final Map<String, String> HEADERS;

  static {
    DATE_TIME = DateTime.now();

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
  }
}
```

Re-run the tests will now generate an updated pact file.

```console
./gradlew clean testDebugUnitTest
...

au.com.dius.pactconsumer.domain.PresenterTest > should_show_empty_when_fetch_returns_nothing PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_error_when_fetch_fails PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_loaded_when_fetch_succeeds PASSED

au.com.dius.pactconsumer.domain.PresenterTest > should_show_loading_when_fetching PASSED

au.com.dius.pactconsumer.data.ServiceTest > should_process_json_payload_from_provider PASSED

au.com.dius.pactconsumer.data.FakeServiceTest > should_return_list_of_animals PASSED

au.com.dius.pactconsumer.data.ServicePactTest > should_process_the_json_payload_from_provider PASSED

BUILD SUCCESSFUL

Total time: 17.235 secs
```

## Step 7 - Verify the provider again

Copy over the new pact file.

Running the verification against the provider now passes. Yay!

```console
$ rake pact:verify
SPEC_OPTS='' /home/theeban/.rvm/rubies/ruby-2.3.0/bin/ruby -S pact verify --pact-helper /home/theeban/Projects/pact-workshop-android/provider/spec/pact_helper.rb
Reading pact at spec/pacts/our_consumer-our_provider.json
WARN: Ignoring unsupported matching rules {"min"=>0} for path $['body']['animals']

Verifying a pact between our_consumer and our_provider
  Given data count is > 0
    a request for json data
      with GET /provider.json?valid_date=2017-02-01T20%253A30%253A58.233%252B11%253A00
        returns a response which
          has status code 200
          has a matching body
          includes headers
            "Content-Type" with value "application/json"

1 interaction, 0 failures
```

# Step 8 - Test for the missing query parameter

In this step we are going to add a test for the case where the query parameter is missing. We do
this by adding additional expectations.

ServiceMissingQueryPactTest.java:

```java
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
```

After running our tests, the pact file will have a new interaction.

spec/pacts/our_consumer-our_provider.json:

```json
{
	"description": "a request with an missing date parameter",
	"request": {
		"method": "GET",
		"path": "/provider.json"
	},
	"response": {
		"status": 400,
		"body": "valid_date is required"
	},
	"providerState": "data count is > 0"
}
```

Let us run this updated pact file with our provider.

We get a lot of errors because our provider fails with a 500 status and an HTML error page.
Time to update the provider to handle these cases.

## Step 9 - Updated the provider to handle the missing query parameter

lib/provider.rb:

```ruby
class Provider < Sinatra::Base

  get '/provider.json', :provides => 'json' do
    if params[:valid_date].nil?
      [400, '"valid_date is required"']
    else
      valid_time = Time.parse(params[:valid_date])
      JSON.pretty_generate({
        :test => 'NO',
        :valid_date => DateTime.now,
        :animals => ProviderData.animals
      })
    end
  end

end
```

Now the pact verification all passes.

## Step 10 - Provider states

We have one final thing to test for. We need to handle when there is no animals. This is an important bit of information to add to our contract. Let us start with a
consumer test for this.

ServiceNoContentPactTest.java:

```java
public class ServiceNoContentPactTest {

  static final DateTime DATE_TIME;

  static {
    DATE_TIME = DateTime.now();
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
    return builder
        .given("data count is == 0")
        .uponReceiving("a request for json data")
        .path("/provider.json")
        .method("GET")
        .query("valid_date=" + DateHelper.encodeDate(DATE_TIME))
        .willRespondWith()
        .status(404)
        .toFragment();
  }

  @Test
  @PactVerification("our_provider")
  public void should_process_the_json_payload_from_provider() {
    TestObserver<ServiceResponse> observer = service.fetchResponse(DATE_TIME).test();
    observer.assertNoErrors();
    observer.assertValue(new ServiceResponse(null, Collections.emptyList()));
  }
}
```

```json
{
	"description": "a request for json data",
	"request": {
		"method": "GET",
		"path": "/provider.json",
		"query": "valid_date=2017-02-01T20%253A50%253A45.397%252B11%253A00"
	},
	"response": {
		"status": 404
	},
	"providerState": "data count is == 0"
},
```

To be able to verify out provider, we create a data class that the provider can use, and then set the data in
the state change setup callback.

lib/provider.rb:

```ruby
class Provider < Sinatra::Base

  get '/provider.json', :provides => 'json' do
    if params[:valid_date].nil?
      [400, '"valid_date is required"']
    elsif ProviderData.animals.size == 0
      404
    else
	  valid_time = Time.parse(params[:valid_date])
	  JSON.pretty_generate({
		:test => 'NO',
		:valid_date => DateTime.now,
		:animals => ProviderData.animals
	  })
    end
  end

end
```

Now we can set the data count appropriately.

spec/pact_helper.rb:

```ruby
Pact.provider_states_for "our_consumer" do

  provider_state "data count is > 0" do
    set_up do
      ProviderData.animals = ANIMALS_LIST
    end
  end

  provider_state "data count is == 0" do
    set_up do
      ProviderData.animals = []
    end
  end

end
```

Running the provider verification passes. Awesome, we are all done.


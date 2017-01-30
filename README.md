Example Ruby Project for Pact Workshop
======================================

When writing a lot of small services, testing the interactions between these becomes a major headache. That's the problem Pact is trying to solve.

Integration tests typically are slow and brittle, requiring each component to have it's own environment to run the tests in. With a micro-service architecture, this becomes even more of a problem. They also have to be 'all-knowing' and this makes them difficult to keep from being fragile.

After J. B. Rainsberger's talk "Integrated Tests Are A Scam" people have been thinking how to get the confidence we need to deploy our software to production without having a tiresome integration test suite that does not give us all the coverage we think it does.

Pact is a ruby gem that allows you to define a pact between service consumers and providers. It provides a DSL for service consumers to define the request they will make to a service producer and the response they expect back. This expectation is used in the consumers specs to provide a mock producer, and is also played back in the producer specs to ensure the producer actually does provide the response the consumer expects.

This allows you to test both sides of an integration point using fast unit tests.

## Step 1 - Simple customer calling Provider

Given we have a client that needs to make a HTTP GET request to a sinatra webapp, and requires a response in JSON format. The client would look something like:

client.rb:

```ruby
    require 'httparty'
    require 'uri'
    require 'json'

    class Client


      def load_provider_json
        response = HTTParty.get(URI::encode('http://localhost:8081/provider.json?valid_date=' + Time.now.httpdate))
        if response.success?
          JSON.parse(response.body)
        end
      end


    end
```

and the provider:
provider.rb

```ruby
    require 'sinatra/base'
    require 'json'


    class Provider < Sinatra::Base


      get '/provider.json', :provides => 'json' do
        valid_time = Time.parse(params[:valid_date])
        JSON.pretty_generate({
          :test => 'NO',
          :valid_date => DateTime.now,
          :count => 1000
        })
      end

    end
```

This provider expects a valid_date parameter in HTTP date format, and then returns some simple json back.

Running the client with the following rake task against the provider works nicely:

```ruby
    desc 'Run the client'
    task :run_client => :init do
      require 'client'
      require 'ap'
      ap Client.new.load_provider_json
    end
```

    $ rake run_client
    {
              "test" => "NO",
        "valid_date" => "2016-03-20T13:00:11+11:00",
             "count" => 1000
    }

## Step 2 - Client Tested but integration fails

Now lets get the client to use the data it gets back from the provider. Here is the updated client method that uses the returned data:

client.rb

```ruby
      def process_data
        data = load_provider_json
        ap data
        value = 100 / data['count']
        date = Time.parse(data['date'])
        puts value
        puts date
        [value, date]
      end
```

Add a spec to test this client:

client_spec.rb:

```ruby
    require 'spec_helper'
    require 'client'


    describe Client do


      let(:json_data) do
        {
          "test" => "NO",
          "date" => "2013-08-16T15:31:20+10:00",
          "count" => 100
        }
      end
      let(:response) { double('Response', :success? => true, :body => json_data.to_json) }


      it 'can process the json payload from the provider' do
        HTTParty.stub(:get).and_return(response)
        expect(subject.process_data).to eql([1, Time.parse(json_data['date'])])
      end

    end
```

Let's run this spec and see it all pass:

```console
    $ rake spec
    /home/ronald/.rvm/rubies/ruby-2.3.0/bin/ruby -I/home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-core-3.4.3/lib:/home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-support-3.4.1/lib /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-core-3.4.3/exe/rspec --pattern spec/\*\*\{,/\*/\*\*\}/\*_spec.rb

    Client
    {
         "test" => "NO",
         "date" => "2013-08-16T15:31:20+10:00",
        "count" => 100
    }
    1
    2013-08-16 15:31:20 +1000
      can process the json payload from the provider

    Finished in 0.00582 seconds (files took 0.09577 seconds to load)
    1 example, 0 failures
```

However, there is a problem with this integration point. The provider returns a 'valid_date' while the consumer is trying to use 'date', which will blow up when run for real even with the tests all passing. Here is where Pact comes in.

## Step 3 - Pact to the rescue

Lets setup Pact in the consumer. Pact lets the consumers define the expectations for the integration point.

pact_helper.rb:

```ruby
require 'pact/consumer/rspec'

Pact.service_consumer "Our Consumer" do
  has_pact_with "Our Provider" do
    mock_service :our_provider do
      port 1234
    end
  end
end
```

This defines a consumer and a producer that runs on port 1234.

The spec for the client now has a pact section.

client_spec.rb:

```ruby
describe 'Pact with our provider', :pact => true do

  subject { Client.new('localhost:1234') }

  let(:date) { Time.now.httpdate }

  describe "get json data" do

    before do
      our_provider.given("data count is > 0").
        upon_receiving("a request for json data").
        with(method: :get, path: '/provider.json', query: URI::encode('valid_date=' + date)).
        will_respond_with(
          status: 200,
          headers: {'Content-Type' => 'application/json'},
          body: json_data )
    end

    it "can process the json payload from the provider" do
      expect(subject.process_data).to eql([1, Time.parse(json_data['date'])])
    end

  end

end
```

Running this spec still passes, but it creates a pact file which we can use to validate our assumptions on the provider side.

```console
    $ rake spec
    /home/ronald/.rvm/rubies/ruby-2.3.0/bin/ruby -I/home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-core-3.4.3/lib:/home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-support-3.4.1/lib /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-core-3.4.3/exe/rspec --pattern spec/\*\*\{,/\*/\*\*\}/\*_spec.rb

    Client
    {
         "test" => "NO",
         "date" => "2013-08-16T15:31:20+10:00",
        "count" => 100
    }
    1
    2013-08-16 15:31:20 +1000
      can process the json payload from the provider
      Pact with our provider
        get json data
    {
         "test" => "NO",
         "date" => "2013-08-16T15:31:20+10:00",
        "count" => 100
    }
    1
    2013-08-16 15:31:20 +1000
          can process the json payload from the provider

    Finished in 0.12844 seconds (files took 0.17281 seconds to load)
    2 examples, 0 failures
```

Generated pact file (spec/pacts/our_consumer-our_provider.json):

```json
{
  "consumer": {
    "name": "Our Consumer"
  },
  "provider": {
    "name": "Our Provider"
  },
  "interactions": [
    {
      "description": "a request for json data",
      "provider_state": "data count is > 0",
      "request": {
        "method": "get",
        "path": "/provider.json",
        "query": "valid_date=Sun,%2020%20Mar%202016%2002:07:13%20GMT"
      },
      "response": {
        "status": 200,
        "headers": {
          "Content-Type": "application/json"
        },
        "body": {
          "test": "NO",
          "date": "2013-08-16T15:31:20+10:00",
          "count": 100
        }
      }
    }
  ],
  "metadata": {
    "pactSpecificationVersion": "1.0.0"
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

Pact.service_provider "Our Provider" do

  honours_pact_with 'Our Consumer' do
    pact_uri 'spec/pacts/our_consumer-our_provider.json'
  end

end
```

Now if we copy the pact file from the consumer project and run our pact verification task, it should fail.

```console
    $ rake pact:verify
    SPEC_OPTS='' /home/ronald/.rvm/rubies/ruby-2.3.0/bin/ruby -S pact verify --pact-helper /home/ronald/Development/Projects/Pact/pact-workshop-ruby/spec/pact_helper.rb
    Reading pact at spec/pacts/our_consumer-our_provider.json

    Verifying a pact between Our Consumer and Our Provider
      Given data count is > 0
        a request for json data
          with GET /provider.json?valid_date=Sun,%2020%20Mar%202016%2002:07:13%20GMT
            returns a response which
              has status code 200 (FAILED - 1)
              has a matching body (FAILED - 2)
              includes headers
                "Content-Type" with value "application/json" (FAILED - 3)

    Failures:

      1) Verifying a pact between Our Consumer and Our Provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=Sun,%2020%20Mar%202016%2002:07:13%20GMT returns a response which has status code 200
         Got 0 failures and 2 other errors:

         1.1) Failure/Error: set_up_provider_state interaction.provider_state, options[:consumer]

              RuntimeError:
                Could not find provider state "data count is > 0" for consumer Our Consumer
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `load'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `<main>'

         1.2) Failure/Error: tear_down_provider_state interaction.provider_state, options[:consumer]

              RuntimeError:
                Could not find provider state "data count is > 0" for consumer Our Consumer
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `load'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `<main>'

      2) Verifying a pact between Our Consumer and Our Provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=Sun,%2020%20Mar%202016%2002:07:13%20GMT returns a response which has a matching body
         Got 0 failures and 2 other errors:

         2.1) Failure/Error: set_up_provider_state interaction.provider_state, options[:consumer]

              RuntimeError:
                Could not find provider state "data count is > 0" for consumer Our Consumer
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `load'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `<main>'

         2.2) Failure/Error: tear_down_provider_state interaction.provider_state, options[:consumer]

              RuntimeError:
                Could not find provider state "data count is > 0" for consumer Our Consumer
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `load'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `<main>'

      3) Verifying a pact between Our Consumer and Our Provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=Sun,%2020%20Mar%202016%2002:07:13%20GMT returns a response which includes headers "Content-Type" with value "application/json"
         Got 0 failures and 2 other errors:

         3.1) Failure/Error: set_up_provider_state interaction.provider_state, options[:consumer]

              RuntimeError:
                Could not find provider state "data count is > 0" for consumer Our Consumer
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `load'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `<main>'

         3.2) Failure/Error: tear_down_provider_state interaction.provider_state, options[:consumer]

              RuntimeError:
                Could not find provider state "data count is > 0" for consumer Our Consumer
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `load'
              # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `<main>'

    1 interaction, 1 failure

    Failed interactions:

    bundle exec rake pact:verify:at[spec/pacts/our_consumer-our_provider.json] PACT_DESCRIPTION="a request for json data" PACT_PROVIDER_STATE="data count is > 0" # A request for json data given data count is > 0

    For assistance debugging failures, run `bundle exec rake pact:verify:help`

    Could not find one or more provider states.
    Have you required the provider states file for this consumer in your pact_helper.rb?
    If you have not yet defined these states, here is a template:

    Pact.provider_states_for "Our Consumer" do

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
Pact.provider_states_for "Our Consumer" do

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
    SPEC_OPTS='' /home/ronald/.rvm/rubies/ruby-2.3.0/bin/ruby -S pact verify --pact-helper /home/ronald/Development/Projects/Pact/pact-workshop-ruby/spec/pact_helper.rb
    Reading pact at spec/pacts/our_consumer-our_provider.json

    Verifying a pact between Our Consumer and Our Provider
      Given data count is > 0
        a request for json data
          with GET /provider.json?valid_date=Sun,%2020%20Mar%202016%2002:07:13%20GMT
            returns a response which
              has status code 200
              has a matching body (FAILED - 1)
              includes headers
                "Content-Type" with value "application/json"

    Failures:

      1) Verifying a pact between Our Consumer and Our Provider Given data count is > 0 a request for json data with GET /provider.json?valid_date=Sun,%2020%20Mar%202016%2002:07:13%20GMT returns a response which has a matching body
         Failure/Error: expect(response_body).to match_term expected_response_body, diff_options

           Actual: {"test":"NO","valid_date":"2016-03-20T13:36:31+11:00","count":1000}

           @@ -1,5 +1,4 @@
            {
           -  "date": "2013-08-16T15:31:20+10:00",
           -  "count": 100
           +  "count": 1000
            }

           Key: - means "expected, but was not found".
                + means "actual, should not be found".
                Values where the expected matches the actual are not shown.
         # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/pact-1.9.0/bin/pact:4:in `<top (required)>'
         # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `load'
         # /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/bin/pact:23:in `<main>'

    1 interaction, 1 failure

    Failed interactions:

    bundle exec rake pact:verify:at[spec/pacts/our_consumer-our_provider.json] PACT_DESCRIPTION="a request for json data" PACT_PROVIDER_STATE="data count is > 0" # A request for json data given data count is > 0

    For assistance debugging failures, run `bundle exec rake pact:verify:help`
```

The test has failed for 2 reasons. Firstly, the count field has a different value to what was expected by the consumer. Secondly, and more importantly, the consumer was expecting a date field.

## Step 6 - Back to the client we go

Let's correct the consumer tests to handle any integer for count and use the correct field for the date. First,
to use type based matching for the data count, we need to enable v2 pact specification.

spec/pact_helper.rb:

```ruby
require 'pact/consumer/rspec'

Pact.service_consumer "Our Consumer" do
  has_pact_with "Our Provider" do
    mock_service :our_provider do
      port 1234
      pact_specification_version "2.0.0"
    end
  end
end
```

Then we need to add a type matcher for `count` and change the field for the date to be `valid_date`. We can also
add a regular expression to make sure the `valid_date` field is a valid date. This is important because we are
parsing it.

The updated consumer test is now:

```ruby
describe 'Pact with our provider', :pact => true do

  subject { Client.new('localhost:1234') }

  let(:date) { Time.now.httpdate }

  describe "get json data" do

    before do
      our_provider.given("data count is > 0").
        upon_receiving("a request for json data").
        with(method: :get, path: '/provider.json', query: URI::encode('valid_date=' + date)).
        will_respond_with(
          status: 200,
          headers: {'Content-Type' => 'application/json'},
          body: {
            "test" => "NO",
            "valid_date" => Pact.term(
                generate: "2013-08-16T15:31:20+10:00",
                matcher: /\d{4}\-\d{2}\-\d{2}T\d{2}:\d{2}:\d{2}\+\d{2}:\d{2}/),
            "count" => Pact.like(100)
          })
    end

    it "can process the json payload from the provider" do
      expect(subject.process_data).to eql([1, Time.parse(json_data['valid_date'])])
    end

  end

end
```

Re-run the specs will now generate an updated pact file.

```console
$ rake spec
/home/ronald/.rvm/rubies/ruby-2.3.0/bin/ruby -I/home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-core-3.4.4/lib:/home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-support-3.4.1/lib /home/ronald/.rvm/gems/ruby-2.3.0@example_pact/gems/rspec-core-3.4.4/exe/rspec --pattern spec/\*\*\{,/\*/\*\*\}/\*_spec.rb

Client
{
          "test" => "NO",
    "valid_date" => "2013-08-16T15:31:20+10:00",
         "count" => 100
}
1
2013-08-16 15:31:20 +1000
  can process the json payload from the provider
  Pact with our provider
    get json data
{
          "test" => "NO",
    "valid_date" => "2013-08-16T15:31:20+10:00",
         "count" => 100
}
1
2013-08-16 15:31:20 +1000
      can process the json payload from the provider

Finished in 0.13973 seconds (files took 0.1671 seconds to load)
2 examples, 0 failures
```

## Step 7 - Verify the provider again

Running the verification against the provider now passes. Yay!

```console
14:23 $ rake pact:verify
SPEC_OPTS='' /home/ronald/.rvm/rubies/ruby-2.3.0/bin/ruby -S pact verify --pact-helper /home/ronald/Development/Projects/Pact/pact-workshop-ruby/spec/pact_helper.rb
Reading pact at spec/pacts/our_consumer-our_provider.json

Verifying a pact between Our Consumer and Our Provider
  Given data count is > 0
    a request for json data
      with GET /provider.json?valid_date=Sun,%2020%20Mar%202016%2003:21:16%20GMT
        returns a response which
          has status code 200
          has a matching body
          includes headers
            "Content-Type" with value "application/json"

1 interaction, 0 failures
```

# Step 8 - Test for the missing query parameter

In this step we are going to add a test for the case where the query parameter is missing or invalid. We do
this by adding additional expectations.

First, we need to update out client to take the date as a parameter.

lib/client.rb:

```ruby
def load_provider_json(query_date)
  response = HTTParty.get(URI::encode("http://#{base_uri}/provider.json?valid_date=#{query_date}"))
  if response.success?
    JSON.parse(response.body)
  end
end

def process_data(query_date)
  data = load_provider_json(query_date)
  ap data
  if data
    value = 100 / data['count']
    date = Time.parse(data['valid_date'])
    puts value
    puts date
    [value, date]
  else
    [0, nil]
  end
end
```

spec/client_spec.rb:

```ruby
it "handles a missing date parameter" do
  our_provider.given("data count is > 0").
    upon_receiving("a request with a missing date parameter").
    with(method: :get, path: '/provider.json').
    will_respond_with(
      status: 400,
      headers: {'Content-Type' => 'application/json'},
      body: "valid_date is required"
    )
  expect(subject.process_data(nil)).to eql([0, nil])
end

it "handles an invalid date parameter" do
  our_provider.given("data count is > 0").
    upon_receiving("a request with an invalid date parameter").
    with(method: :get, path: '/provider.json', query: 'valid_date=This%20is%20not%20a%20date').
    will_respond_with(
      status: 400,
      headers: {'Content-Type' => 'application/json'},
      body: "'This is not a date' is not a date"
    )
  expect(subject.process_data('This is not a date')).to eql([0, nil])
end
```

After running our specs, the pact file will have 2 new interactions.

spec/pacts/our_consumer-our_provider.json:

```json
{
  "description": "a request with a missing date parameter",
  "provider_state": "data count is > 0",
  "request": {
    "method": "get",
    "path": "/provider.json"
  },
  "response": {
    "status": 400,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "valid_date is required"
  }
},
{
  "description": "a request with an invalid date parameter",
  "provider_state": "data count is > 0",
  "request": {
    "method": "get",
    "path": "/provider.json",
    "query": "valid_date=This%20is%20not%20a%20date"
  },
  "response": {
    "status": 400,
    "headers": {
      "Content-Type": "application/json"
    },
    "body": "'This is not a date' is not a date"
  }
}
```

## Step 9 - Verify the provider with the missing/invalid date query parameter

Let us run this updated pact file with our provider. We get a lot of errors because our provider fails with a 500 status and an HTML error page.
Time to update the provider to handle these cases.

## Step 10 - Updated the provider to handle the missing/invalid query parameters

lib/provider.rb:

```ruby
class Provider < Sinatra::Base

  get '/provider.json', :provides => 'json' do
    if params[:valid_date].nil?
      [400, '"valid_date is required"']
    else
      begin
        valid_time = Time.parse(params[:valid_date])
        JSON.pretty_generate({
          :test => 'NO',
          :valid_date => DateTime.now,
          :count => 1000
        })
      rescue ArgumentError => e
        [400, "\"\'#{params[:valid_date]}\' is not a date\""]
      end
    end
  end

end
```

Now the pact verification all passes.

## Step 11 - Provider states

We have one final thing to test for. If the provider ever returns a count of zero, we will get a division by
zero error in our client. This is an important bit of information to add to our contract. Let us start with a
consumer test for this.

spec/client_spec.rb:

```ruby
describe "when there is no data" do

  it "handles the 404 response" do
    our_provider.given("data count is == 0").
      upon_receiving("a request for json data").
      with(method: :get, path: '/provider.json', query: URI::encode('valid_date=' + date)).
      will_respond_with(status: 404)
    expect(subject.process_data(date)).to eql([0, nil])
  end

end
```

This adds a new interaction to the pact file:

spec/pacts/our_consumer-our_provider.json:

```json
{
  "description": "a request for json data",
  "provider_state": "data count is == 0",
  "request": {
    "method": "get",
    "path": "/provider.json",
    "query": "valid_date=Sun,%2020%20Mar%202016%2004:46:40%20GMT"
  },
  "response": {
    "status": 404,
    "headers": {
    }
  }
}
```

## Step 12 - provider states for the provider

To be able to verify out provider, we create a data class that the provider can use, and then set the data in
the state change setup callback.

lib/provider.rb:

```ruby
class ProviderData
  @@data_count = 1000
  class << self
    attr_accessor :data_count
  end
end

class Provider < Sinatra::Base

  get '/provider.json', :provides => 'json' do
    if params[:valid_date].nil?
      [400, '"valid_date is required"']
    elsif ProviderData.data_count == 0
      404
    else
      begin
        valid_time = Time.parse(params[:valid_date])
        JSON.pretty_generate({
          :test => 'NO',
          :valid_date => DateTime.now,
          :count => ProviderData.data_count
        })
      rescue ArgumentError => e
        [400, "\"\'#{params[:valid_date]}\' is not a date\""]
      end
    end
  end

end
```

Now we can set the data count appropriately.

spec/pact_helper.rb:

```ruby
Pact.provider_states_for "Our Consumer" do

  provider_state "data count is > 0" do
    set_up do
      ProviderData.data_count = 1000
    end
  end

  provider_state "data count is == 0" do
    set_up do
      ProviderData.data_count = 0
    end
  end

end
```

Running the provider verification passes. Awesome, we are all done.

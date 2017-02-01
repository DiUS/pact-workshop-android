require 'pact/provider/rspec'

Pact.service_provider "our_provider" do

  honours_pact_with 'our_consumer' do
    pact_uri 'spec/pacts/our_consumer-our_provider.json'
  end

end


require 'pact/provider/rspec'

Pact.service_provider "our_provider" do

  honours_pact_with 'our_consumer' do
    pact_uri 'spec/pacts/our_consumer-our_provider.json'
  end

end

Pact.provider_states_for "our_consumer" do

  provider_state "data count is > 0" do
    set_up do
      # Your set up code goes here
    end
  end

end

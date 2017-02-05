require 'pact/provider/rspec'

Pact.service_provider "our_provider" do

  honours_pact_with 'our_consumer' do
    pact_uri URI.encode('https://test.pact.dius.com.au/pact/provider/our_provider/consumer/our_consumer/latest')
  end

end

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

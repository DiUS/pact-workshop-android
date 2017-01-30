require 'pact/provider/rspec'

Pact.service_provider "Our Provider" do

  honours_pact_with 'Our Consumer' do
    pact_uri 'spec/pacts/our_consumer-our_provider.json'
  end

end

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

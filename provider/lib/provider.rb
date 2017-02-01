require 'sinatra/base'
require 'json'

ANIMALS_LIST = [
  {
    name: "Buddy",
    image: "dog"
  },
  {
    name: "Cathy",
    image: "cat"
  },
  {
    name: "Birdy",
    image: "bird"
  }
]

class ProviderData
  @animals = ANIMALS_LIST
  class << self
    attr_accessor :animals
  end
end

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

require 'sinatra/base'
require 'json'

class ProviderData
  @data_count = 1000
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

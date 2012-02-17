require 'rest_client'


module Sender

  def Sender.put_to_server(url, payload_json)
    request = RestClient::Request.new(:method => "PUT",
                                      :url=>url,
                                      :user=> USER,
                                      :password=>PASSWORD,
                                      :headers => {:accept => :json, :content_type => :json},
                                      :payload => payload_json
    )

    request.execute()
  end


  # @param res_id [integer]
  def Sender.send_availability(res_id,state="UP")

    report = {:type=>state,
              :resourceId=>res_id,
              :since=>Time.now.to_i}
    report_json = JSON.generate(report)

    url = BASE_URL + 'resource/' + res_id.to_s + '/availability'

    Sender.put_to_server(url, report_json)

  end

  def Sender.send_numeric_metric(schedule_id,value)
    tstamp = Time.now.to_i
    report = {:timeStamp =>tstamp,
              :value=> value,
              :scheduleId  => schedule_id}
    report_json = JSON.generate(report)
    url = BASE_URL + 'metric/data/' + schedule_id.to_s + '/raw/' + tstamp

    Sender.put_to_server(url,report_json)

  end

  # curl -u rhqadmin:rhqadmin http://localhost:7080/rest/1/metric/data/41861/trait -X PUT -HContent-Type:application/json -d '{"value":"ruby snert"}'
  def Sender.send_trait_metric(schedule_id,value)
    tstamp = Time.now.to_i
    report = {
              :value=> value
              }
    report_json = JSON.generate(report)
    url = BASE_URL + 'metric/data/' + schedule_id.to_s + '/trait/'

    Sender.put_to_server(url,report_json)

  end

  # @param platform_name [String] Name of the platform to create
  # @param type [String] ResourceType name of the platform. E.g "Java", "Mac OS X", "Linux" or "Windows"
  def Sender.create_platform(platform_name,type)

    url = BASE_URL + 'foo/cp/' + platform_name
    report = {
              :value=> type
              }
    report_json = JSON.generate(report)

    request = RestClient::Request.new(:method => "POST",
                            :url=>url ,
                            :user=> USER,
                            :password=>PASSWORD,
                            :headers => { :accept => :json, :content_type => :json },
                            :payload => report_json
      )

    response = request.execute()

  end

  def Sender.get_schedules(resource_id)
    url = BASE_URL + 'resource/' + resource_id.to_s + '/schedules'

    request = RestClient::Request.new(:method=> "GET",
                :url=>url,
                :user=>USER,
                :password=>PASSWORD,
                :headers => { :accept => :json, :content_type => :json }
    )

    response = request.execute()

    JSON.parse(response)
  end

end

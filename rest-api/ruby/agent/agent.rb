#
# Prototype of an agent written in Ruby
#

require 'JSON'
require 'socket'
require File.dirname(__FILE__) + "/comm/sender"
require File.dirname(__FILE__) + "/util/platform"

server = 'localhost'
port='7080'
USER = 'rhqadmin'
PASSWORD = 'rhqadmin'
rest_base = '/rest/1'


platform_name = Socket::gethostname + '-ruby'
platform_type = Platform.get_type

BASE_URL = 'http://' + server + ':' + port+rest_base + '/'


def get_schedule_id_for_name(name,schedules)
  schedules.each { |schedule|
  if schedule["scheduleName"]==name
    return schedule["scheduleId"]
  end
  }
  raise "No schedule with name [" + name + "] found"
end


RestClient.log = "stdout"

## Create platform if not yet present and get its id
response = Sender.create_platform(platform_name,platform_type)
data = JSON.parse(response)
platform_id = data["resourceId"]
schedules = Sender.get_schedules(platform_id)

trait_id = get_schedule_id_for_name("Trait.hostname",schedules)
Sender.send_trait_metric(trait_id,platform_name)

trait_id = get_schedule_id_for_name("Trait.osversion",schedules)
Sender.send_trait_metric(trait_id,RUBY_VERSION)

trait_id = get_schedule_id_for_name("Trait.osname",schedules)
Sender.send_trait_metric(trait_id,RUBY_PLATFORM)

trait_id = get_schedule_id_for_name("Trait.sysarch",schedules)
Sender.send_trait_metric(trait_id,RUBY_PLATFORM)

# Send our first availability report
Sender.send_availability(platform_id)

should_finish = false
######## main loop #######
until should_finish

  trap("INT"){should_finish=true}
  sleep(240)
  unless should_finish
    Sender.send_availability(platform_id, "UP")
  end


end

Sender.send_availability(platform_id,"DOWN")



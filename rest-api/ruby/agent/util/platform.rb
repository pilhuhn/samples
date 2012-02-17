require 'rbconfig'

module Platform

  def Platform.get_type

    type = case Config::CONFIG['host_os']
      when /mswin|windows/i
        "Windows"
      when /linux/i
        "Linux"
      when /sunos|solaris/i
        "Solaris"
      when /darwin/i
        "Mac OS X"
      else
        "Java"
    end
    type
  end

  def Platform.free_memory

  end
end

